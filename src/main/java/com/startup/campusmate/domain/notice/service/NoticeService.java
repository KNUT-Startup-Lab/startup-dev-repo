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
import jakarta.persistence.criteria.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

        Specification<Notice> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction(); // 모든 조건을 AND로 연결

            // 카테고리 검색
            if (category != null && !category.isEmpty()) {
                try {
                    NoticeCategory noticeCategory = NoticeCategory.valueOf(category.toUpperCase());
                    p = cb.and(p, cb.equal(root.get("category"), noticeCategory));
                } catch (IllegalArgumentException e) {
                    // 유효하지 않은 카테고리 값은 무시
                }
            }

            // 검색 키워드 및 타입
            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                switch (searchType) {
                    case "title":
                        p = cb.and(p, cb.like(root.get("title"), "%" + searchKeyword + "%"));
                        break;
                    case "content":
                        p = cb.and(p, cb.like(root.get("content"), "%" + searchKeyword + "%"));
                        break;
                    case "title_content":
                        Predicate titleLike = cb.like(root.get("title"), "%" + searchKeyword + "%");
                        Predicate contentLike = cb.like(root.get("content"), "%" + searchKeyword + "%");
                        p = cb.and(p, cb.or(titleLike, contentLike));
                        break;
                }
            }

            // 날짜 범위 검색
            if (startDate != null) {
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("createDate"), startDate));
            }
            if (endDate != null) {
                p = cb.and(p, cb.lessThanOrEqualTo(root.get("createDate"), endDate));
            }

            return p;
        };

        Page<Notice> noticePage = noticeRepository.findAll(spec, pageable);

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

        return RsData.of("200-S1", "공지사항 목록 조회 성공", response);
    }

    // 공지사항 상세 조회
    @Transactional
    public RsData<NoticeDetailRs> getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElse(null);

        if (notice == null) {
            return RsData.of("404-F1", "해당 공지사항을 찾을 수 없습니다.");
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

        return RsData.of("200-S1", "공지사항 상세 조회 성공", response);
    }

    // 크롤링 게시물 원본 URL 조회
    public RsData<String> getOriginalUrl(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElse(null);

        if (notice == null) {
            return RsData.of("404-F1", "해당 공지사항을 찾을 수 없습니다.");
        }

        if (!notice.isCrawled()) {
            return RsData.of("400-F2", "이 게시물은 크롤링된 게시물이 아닙니다.");
        }

        return RsData.of("200-S1", "원본 URL 조회 성공", notice.getOriginalUrl());
    }

    // 공지사항 생성 (관리자)
    @Transactional
    public RsData<Long> createNotice(NoticeCreateRq rq, Long adminMemberId) {
        Member admin = memberRepository.findById(adminMemberId)
                .orElse(null);

        if (admin == null || !admin.is_isAdmin()) {
            return RsData.of("403-F1", "관리자 권한이 없습니다.");
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
            for (Long attachmentId : rq.getAttachments()) {
                attachmentRepository.findById(attachmentId).ifPresent(attachment -> {
                    attachment.setNotice(notice); // Attachment 엔티티에 Notice 연결
                    // attachmentRepository.save(attachment); // Notice 엔티티의 cascade 설정에 따라 필요 없을 수 있음
                });
            }
        }

        return RsData.of("201-S1", "공지사항 생성 성공", notice.getId());
    }

    // 크롤링된 공지사항 저장 (crawledNoticeNumber 파라미터 추가)
    @Transactional
    public RsData<Long> createCrawledNotice(String title, String department, String originalUrl, String crawledNoticeNumber) { // crawledNoticeNumber 추가
        Notice notice = Notice.builder()
                .title(title)
                .content(null)
                .department(department)
                .category(NoticeCategory.CRAWLED)
                .isCrawled(true)
                .originalUrl(originalUrl)
                .author(null)
                .crawledNoticeNumber(crawledNoticeNumber) // crawledNoticeNumber 저장
                .build();

        noticeRepository.save(notice);

        return RsData.of("201-S1", "크롤링 공지사항 저장 성공", notice.getId());
    }

    // 공지사항 수정 (관리자)
    @Transactional
    public RsData<String> updateNotice(Long id, NoticeUpdateRq rq, Long adminMemberId) {
        Notice notice = noticeRepository.findById(id)
                .orElse(null);

        if (notice == null) {
            return RsData.of("404-F1", "해당 공지사항을 찾을 수 없습니다.");
        }

        Member admin = memberRepository.findById(adminMemberId)
                .orElse(null);

        if (admin == null || !admin.is_isAdmin()) {
            return RsData.of("403-F1", "관리자 권한이 없습니다.");
        }

        // 크롤링 게시물은 제목, 부서명만 수정 가능
        if (notice.isCrawled()) {
            notice.setTitle(rq.getTitle());
            notice.setDepartment(rq.getDepartment());
        } else { // 직접 작성 게시물은 모든 필드 수정 가능
            notice.setTitle(rq.getTitle());
            notice.setContent(rq.getContent());
            notice.setDepartment(rq.getDepartment());

            // 기존 첨부파일 삭제 및 새 첨부파일 추가 로직
            List<Attachment> existingAttachments = notice.getAttachments();
            List<Long> newAttachmentIds = rq.getAttachments() != null ? rq.getAttachments() : List.of();

            // 삭제할 첨부파일 찾기
            List<Attachment> attachmentsToDelete = existingAttachments.stream()
                    .filter(att -> !newAttachmentIds.contains(att.getId()))
                    .collect(Collectors.toList());

            // 삭제
            for (Attachment attachment : attachmentsToDelete) {
                deletePhysicalFile(attachment.getFilePath()); // 물리적 파일 삭제
                attachmentRepository.delete(attachment); // DB에서 삭제
                notice.getAttachments().remove(attachment); // Notice 엔티티에서 제거
            }

            // 새로 추가할 첨부파일 찾기
            List<Long> existingAttachmentIds = existingAttachments.stream()
                    .map(Attachment::getId)
                    .collect(Collectors.toList());

            for (Long newAttachmentId : newAttachmentIds) {
                if (!existingAttachmentIds.contains(newAttachmentId)) {
                    attachmentRepository.findById(newAttachmentId).ifPresent(attachment -> {
                        attachment.setNotice(notice); // Notice와 연결
                        notice.getAttachments().add(attachment); // Notice 엔티티에 추가
                    });
                }
            }
        }

        noticeRepository.save(notice);

        return RsData.of("200-S1", "공지사항 수정 성공");
    }

    // 공지사항 삭제 (관리자)
    @Transactional
    public RsData<String> deleteNotice(Long id, Long adminMemberId) {
        Notice notice = noticeRepository.findById(id)
                .orElse(null);

        if (notice == null) {
            return RsData.of("404-F1", "해당 공지사항을 찾을 수 없습니다.");
        }

        Member admin = memberRepository.findById(adminMemberId)
                .orElse(null);

        if (admin == null || !admin.is_isAdmin()) {
            return RsData.of("403-F1", "관리자 권한이 없습니다.");
        }

        // 연관된 첨부파일 실제 파일 삭제 로직 추가
        for (Attachment attachment : notice.getAttachments()) {
            deletePhysicalFile(attachment.getFilePath());
        }
        noticeRepository.delete(notice);

        return RsData.of("200-S1", "공지사항 삭제 성공");
    }

    // 첨부파일 업로드 (별도 API에서 호출될 예정)
    @Transactional
    public RsData<AttachmentDto> uploadAttachment(MultipartFile file) {
        if (file.isEmpty()) {
            return RsData.of("400-F1", "업로드할 파일이 없습니다.");
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

            Attachment attachment = Attachment.builder()
                    .uploadFileName(originalFilename)
                    .storedFileName(storedFileName)
                    .filePath(filePath)
                    .notice(null) // Notice와 연결은 createNotice/updateNotice에서 진행
                    .build();
            attachmentRepository.save(attachment);

            return RsData.of("201-S1", "파일 업로드 성공", AttachmentDto.builder()
                    .file_id(attachment.getId().toString())
                    .filename(attachment.getUploadFileName())
                    .file_url("/api/notices/attachments/" + attachment.getId())
                    .build());

        } catch (IOException e) {
            return RsData.of("500-F2", "파일 업로드 실패: " + e.getMessage());
        }
    }

    // 첨부파일 다운로드
    public RsData<File> downloadAttachment(Long fileId) {
        Attachment attachment = attachmentRepository.findById(fileId)
                .orElse(null);

        if (attachment == null) {
            return RsData.of("404-F1", "해당 첨부파일을 찾을 수 없습니다.");
        }

        File file = new File(attachment.getFilePath());
        if (!file.exists() || !file.isFile()) {
            return RsData.of("404-F2", "파일이 존재하지 않거나 유효하지 않습니다.");
        }

        return RsData.of("200-S1", "파일 다운로드 준비 완료", file);
    }

    // 물리적 파일 삭제 헬퍼 메서드
    private void deletePhysicalFile(String filePath) {
        try {
            java.nio.file.Path fileToDeletePath = java.nio.file.Paths.get(filePath).toAbsolutePath().normalize();
            java.nio.file.Files.deleteIfExists(fileToDeletePath);
        } catch (IOException e) {
            System.err.println("파일 삭제 실패: " + filePath + ", 오류: " + e.getMessage());
        }
    }
}
