package com.startup.campusmate.domain.notice.service;

import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.domain.member.repository.MemberRepository;
import com.startup.campusmate.domain.notice.dto.*;
import com.startup.campusmate.domain.notice.entity.Attachment;
import com.startup.campusmate.domain.notice.entity.Notice;
import com.startup.campusmate.domain.notice.entity.NoticeCategory;
import com.startup.campusmate.domain.notice.repository.AttachmentRepository;
import com.startup.campusmate.domain.notice.repository.NoticeRepository;
import com.startup.campusmate.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final AttachmentRepository attachmentRepository;
    private final MemberRepository memberRepository; // 작성자(관리자) 정보 조회를 위해 추가

    // 공지사항 목록 조회
    public RsData<NoticeListRs> getNotices(int page, int size, String category, String searchType, String searchKeyword, LocalDateTime startDate, LocalDateTime endDate, String sort) {
        // 정렬 기준 설정 (기본 최신순)
        Sort sortBy = Sort.by("createDate").descending();
        if ("views".equals(sort)) {
            sortBy = Sort.by("views").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sortBy);

        // TODO: 검색 조건에 따른 동적 쿼리 구현 (QueryDSL 또는 Specification 사용 예정)
        Page<Notice> noticePage = noticeRepository.findAll(pageable);

        List<NoticeDto> noticeDtos = noticePage.getContent().stream()
                .map(notice -> NoticeDto.builder()
                        .id(notice.getId())
                        .title(notice.getTitle())
                        .department(notice.getDepartment())
                        .created_at(notice.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                        .views(notice.getViews())
                        .category(notice.getCategory().getDescription())
                        .build())
                .collect(Collectors.toList());

        NoticeListRs response = NoticeListRs.builder()
                .notices(noticeDtos)
                .total(noticePage.getTotalElements())
                .page(noticePage.getNumber())
                .size(noticePage.getSize())
                .build();

        return RsData.of("S-1", "공지사항 목록 조회 성공", response);
    }

    // 공지사항 상세 조회
    @Transactional
    public RsData<NoticeDetailRs> getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElse(null);

        if (notice == null) {
            return RsData.of("F-1", "해당 공지사항을 찾을 수 없습니다.");
        }

        // 조회수 증가
        notice.setViews(notice.getViews() + 1);
        noticeRepository.save(notice); // 변경된 조회수 저장

        List<AttachmentDto> attachmentDtos = notice.getAttachments().stream()
                .map(attachment -> AttachmentDto.builder()
                        .file_id(attachment.getId().toString())
                        .filename(attachment.getUploadFileName())
                        .file_url("/api/notices/attachments/" + attachment.getId()) // 다운로드 URL
                        .build())
                .collect(Collectors.toList());

        NoticeDetailRs response = NoticeDetailRs.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .department(notice.getDepartment())
                .created_at(notice.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .updated_at(notice.getModifyDate() != null ? notice.getModifyDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null)
                .views(notice.getViews())
                .category(notice.getCategory().getDescription())
                .attachments(attachmentDtos)
                .build();

        return RsData.of("S-1", "공지사항 상세 조회 성공", response);
    }

    // 크롤링 게시물 원본 URL 조회
    public RsData<String> getOriginalUrl(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElse(null);

        if (notice == null) {
            return RsData.of("F-1", "해당 공지사항을 찾을 수 없습니다.");
        }

        if (!notice.isCrawled()) {
            return RsData.of("F-2", "이 게시물은 크롤링된 게시물이 아닙니다.");
        }

        return RsData.of("S-1", "원본 URL 조회 성공", notice.getOriginalUrl());
    }

    // 공지사항 생성 (관리자)
    @Transactional
    public RsData<Long> createNotice(NoticeCreateRq rq, Long adminMemberId) {
        Member admin = memberRepository.findById(adminMemberId)
                .orElse(null);

        if (admin == null || !admin.is_isAdmin()) {
            return RsData.of("F-1", "관리자 권한이 없습니다.");
        }

        Notice notice = Notice.builder()
                .title(rq.getTitle())
                .content(rq.getContent())
                .department(rq.getDepartment())
                .category(NoticeCategory.CUSTOM) // 직접 작성 게시물
                .isCrawled(false)
                .author(admin)
                .build();

        noticeRepository.save(notice);

        // 첨부파일 처리
        if (rq.getAttachments() != null && !rq.getAttachments().isEmpty()) {
            for (String fileId : rq.getAttachments()) {
                // TODO: 실제 파일 저장 로직 및 Attachment 엔티티 생성 로직 필요
                // 현재는 DTO에 파일 ID만 받으므로, 실제 파일 업로드는 별도의 API에서 처리 후 ID를 넘겨받는 방식 가정
                // 임시로 Attachment 엔티티를 생성하여 Notice와 연결
                Attachment attachment = Attachment.builder()
                        .uploadFileName("temp_file_name") // 임시 파일명
                        .storedFileName(fileId) // fileId를 저장된 파일명으로 사용 (실제로는 UUID 등)
                        .filePath("temp/path/") // 임시 경로
                        .notice(notice)
                        .build();
                attachmentRepository.save(attachment);
            }
        }

        return RsData.of("S-1", "공지사항 생성 성공", notice.getId());
    }

    // 크롤링된 공지사항 저장
    @Transactional
    public RsData<Long> createCrawledNotice(String title, String department, String originalUrl) {
        Notice notice = Notice.builder()
                .title(title)
                .content(null) // 크롤링 게시물은 내용이 없음
                .department(department)
                .category(NoticeCategory.CRAWLED) // 크롤링 게시물
                .isCrawled(true)
                .originalUrl(originalUrl)
                .author(null) // 크롤링 게시물은 작성자가 없음
                .build();

        noticeRepository.save(notice);

        return RsData.of("S-1", "크롤링 공지사항 저장 성공", notice.getId());
    }

    // 공지사항 수정 (관리자)
    @Transactional
    public RsData<String> updateNotice(Long id, NoticeUpdateRq rq, Long adminMemberId) {
        Notice notice = noticeRepository.findById(id)
                .orElse(null);

        if (notice == null) {
            return RsData.of("F-1", "해당 공지사항을 찾을 수 없습니다.");
        }

        Member admin = memberRepository.findById(adminMemberId)
                .orElse(null);

        if (admin == null || !admin.is_isAdmin()) {
            return RsData.of("F-2", "관리자 권한이 없습니다.");
        }

        // 크롤링 게시물은 제목, 부서명만 수정 가능
        if (notice.isCrawled()) {
            notice.setTitle(rq.getTitle());
            notice.setDepartment(rq.getDepartment());
        } else { // 직접 작성 게시물은 모든 필드 수정 가능
            notice.setTitle(rq.getTitle());
            notice.setContent(rq.getContent());
            notice.setDepartment(rq.getDepartment());
            // TODO: 첨부파일 수정 로직 (기존 파일 삭제 및 새 파일 추가)
        }

        noticeRepository.save(notice);

        return RsData.of("S-1", "공지사항 수정 성공");
    }

    // 공지사항 삭제 (관리자)
    @Transactional
    public RsData<String> deleteNotice(Long id, Long adminMemberId) {
        Notice notice = noticeRepository.findById(id)
                .orElse(null);

        if (notice == null) {
            return RsData.of("F-1", "해당 공지사항을 찾을 수 없습니다.");
        }

        Member admin = memberRepository.findById(adminMemberId)
                .orElse(null);

        if (admin == null || !admin.is_isAdmin()) {
            return RsData.of("F-2", "관리자 권한이 없습니다.");
        }

        // TODO: 연관된 첨부파일 실제 파일 삭제 로직 추가
        noticeRepository.delete(notice);

        return RsData.of("S-1", "공지사항 삭제 성공");
    }

    // 첨부파일 업로드 (별도 API에서 호출될 예정)
    @Transactional
    public RsData<AttachmentDto> uploadAttachment(MultipartFile file) {
        if (file.isEmpty()) {
            return RsData.of("F-1", "업로드할 파일이 없습니다.");
        }

        try {
            String uploadDir = "uploads/attachments/"; // 파일 저장 디렉토리
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs(); // 디렉토리가 없으면 생성
            }

            String originalFilename = file.getOriginalFilename();
            String storedFileName = UUID.randomUUID().toString() + "_" + originalFilename; // 고유한 파일명 생성
            String filePath = uploadDir + storedFileName;

            File dest = new File(filePath);
            file.transferTo(dest); // 파일 저장

            // Attachment 엔티티는 Notice와 연결되어야 하므로, 여기서는 임시로 DTO만 반환
            // 실제로는 Notice 생성/수정 시 이 Attachment 엔티티가 생성되어야 함
            return RsData.of("S-1", "파일 업로드 성공", AttachmentDto.builder()
                    .file_id(storedFileName) // 임시로 저장된 파일명을 ID로 사용
                    .filename(originalFilename)
                    .file_url("/api/notices/attachments/" + storedFileName) // 임시 URL
                    .build());

        } catch (IOException e) {
            return RsData.of("F-2", "파일 업로드 실패: " + e.getMessage());
        }
    }

    // 첨부파일 다운로드
    public RsData<File> downloadAttachment(Long fileId) {
        Attachment attachment = attachmentRepository.findById(fileId)
                .orElse(null);

        if (attachment == null) {
            return RsData.of("F-1", "해당 첨부파일을 찾을 수 없습니다.");
        }

        File file = new File(attachment.getFilePath());
        if (!file.exists() || !file.isFile()) {
            return RsData.of("F-2", "파일이 존재하지 않거나 유효하지 않습니다.");
        }

        return RsData.of("S-1", "파일 다운로드 준비 완료", file);
    }
}
