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
import org.springframework.beans.factory.annotation.Value;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final AttachmentRepository attachmentRepository;
    private final MemberRepository memberRepository;

    @Value("${custom.file.upload-dir}")
    private String uploadDir;

    // 공지사항 목록 조회 (기존 로직 유지)
    public RsData<NoticeListRs> getNotices(int page, int size, String category, String searchType, String searchKeyword, LocalDateTime startDate, LocalDateTime endDate, String sort) {
        Sort sortBy = Sort.by("createDate").descending();
        if ("views".equals(sort)) {
            sortBy = Sort.by("views").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sortBy);

        Specification<Notice> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();

            if (category != null && !category.isEmpty()) {
                try {
                    NoticeCategory noticeCategory = NoticeCategory.valueOf(category.toUpperCase());
                    p = cb.and(p, cb.equal(root.get("category"), noticeCategory));
                } catch (IllegalArgumentException e) {
                }
            }

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
                .map(Notice::toDto)
                .collect(Collectors.toList());

        NoticeListRs response = NoticeListRs.builder()
                .notices(noticeDtos)
                .total(noticePage.getTotalElements())
                .page(noticePage.getNumber())
                .size(noticePage.getSize())
                .build();

        return RsData.of("200-S1", "공지사항 목록 조회 성공", response);
    }

    // 공지사항 상세 조회 (기존 로직 유지)
    @Transactional
    public RsData<NoticeDetailRs> getNoticeDetail(Long id) {
        return noticeRepository.findById(id)
                .map(notice -> {
                    notice.setViews(notice.getViews() + 1);
                    return RsData.of("200-S1", "공지사항 상세 조회 성공", notice.toDetailRs());
                })
                .orElse(RsData.of("404-F1", "해당 공지사항을 찾을 수 없습니다."));
    }

    // 크롤링 게시물 원본 URL 조회 (기존 로직 유지)
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

    // 공지사항 생성 (관리자) - isCrawled와 originalUrl 반영
    @Transactional
    public RsData<Long> createNotice(NoticeCreateRq rq, Long adminMemberId) {
        Member admin = memberRepository.findById(adminMemberId)
                .orElse(null);

        if (admin == null || admin.getRole().equals("ROLE_USER")) {
            return RsData.of("403-F1", "관리자 권한이 없습니다.");
        }

        Notice.NoticeBuilder noticeBuilder = Notice.builder()
                .title(rq.getTitle())
                .department(rq.getDepartment())
                .isCrawled(rq.isCrawled()) // isCrawled 값 반영
                .author(admin);

        if (rq.isCrawled()) {
            noticeBuilder
                    .category(NoticeCategory.CRAWLED)
                    .content(null) // 크롤링 게시물은 내용이 없음
                    .originalUrl(rq.getOriginalUrl()); // originalUrl 반영
        } else {
            noticeBuilder
                    .category(NoticeCategory.CUSTOM)
                    .content(rq.getContent()) // 직접 작성 게시물은 내용 있음
                    .originalUrl(null); // 직접 작성 게시물은 originalUrl 없음
        }

        Notice notice = noticeBuilder.build();
        noticeRepository.save(notice);

        // 첨부파일 처리 (기존 로직 유지)
        if (rq.getAttachments() != null && !rq.getAttachments().isEmpty()) {
            for (Long attachmentId : rq.getAttachments()) {
                attachmentRepository.findById(attachmentId).ifPresent(attachment -> {
                    attachment.setNotice(notice);
                });
            }
        }

        return RsData.of("201-S1", "공지사항 생성 성공", notice.getId());
    }

    // 크롤링된 공지사항 저장 (기존 로직 유지)
    @Transactional
    public RsData<Long> createCrawledNotice(String title, String department, String originalUrl, String crawledNoticeNumber) {
        Notice notice = Notice.builder()
                .title(title)
                .content(null)
                .department(department)
                .category(NoticeCategory.CRAWLED)
                .isCrawled(true)
                .originalUrl(originalUrl)
                .author(null)
                .crawledNoticeNumber(crawledNoticeNumber)
                .build();

        noticeRepository.save(notice);

        return RsData.of("201-S1", "크롤링 공지사항 저장 성공", notice.getId());
    }

    // 공지사항 수정 (관리자) - isCrawled와 originalUrl 반영
    @Transactional
    public RsData<String> updateNotice(Long id, NoticeUpdateRq rq, Long adminMemberId) {
        Notice notice = noticeRepository.findById(id)
                .orElse(null);

        if (notice == null) {
            return RsData.of("404-F1", "해당 공지사항을 찾을 수 없습니다.");
        }

        Member admin = memberRepository.findById(adminMemberId)
                .orElse(null);

        if (admin == null || admin.getRole().equals("ROLE_USER")) {
            return RsData.of("403-F1", "관리자 권한이 없습니다.");
        }

        // 크롤링 게시물은 제목, 부서명, originalUrl만 수정 가능
        if (notice.isCrawled()) {
            notice.setTitle(rq.getTitle());
            notice.setDepartment(rq.getDepartment());
            // 크롤링된 게시물은 originalUrl도 수정 가능하도록 (NoticeUpdateRq에 originalUrl 필드 추가 필요)
            // 현재 NoticeUpdateRq에는 originalUrl 필드가 없으므로, 필요하다면 추가해야 합니다.
            // rq.getOriginalUrl()이 있다면 notice.setOriginalUrl(rq.getOriginalUrl());
        } else { // 직접 작성 게시물은 모든 필드 수정 가능
            notice.setTitle(rq.getTitle());
            notice.setContent(rq.getContent());
            notice.setDepartment(rq.getDepartment());

            // 기존 첨부파일 삭제 및 새 첨부파일 추가 로직
            List<Attachment> existingAttachments = notice.getAttachments();
            List<Long> newAttachmentIds = rq.getAttachments() != null ? rq.getAttachments() : List.of();

            List<Attachment> attachmentsToDelete = existingAttachments.stream()
                    .filter(att -> !newAttachmentIds.contains(att.getId()))
                    .collect(Collectors.toList());

            for (Attachment attachment : attachmentsToDelete) {
                deletePhysicalFile(attachment.getFilePath());
                attachmentRepository.delete(attachment);
                notice.getAttachments().remove(attachment);
            }

            List<Long> existingAttachmentIds = existingAttachments.stream()
                    .map(Attachment::getId)
                    .collect(Collectors.toList());

            for (Long newAttachmentId : newAttachmentIds) {
                if (!existingAttachmentIds.contains(newAttachmentId)) {
                    attachmentRepository.findById(newAttachmentId).ifPresent(attachment -> {
                        attachment.setNotice(notice);
                        notice.getAttachments().add(attachment);
                    });
                }
            }
        }

        noticeRepository.save(notice);

        return RsData.of("200-S1", "공지사항 수정 성공");
    }

    // 공지사항 삭제 (관리자) (기존 로직 유지)
    @Transactional
    public RsData<String> deleteNotice(Long id, Long adminMemberId) {
        Notice notice = noticeRepository.findById(id)
                .orElse(null);

        if (notice == null) {
            return RsData.of("404-F1", "해당 공지사항을 찾을 수 없습니다.");
        }

        Member admin = memberRepository.findById(adminMemberId)
                .orElse(null);

        if (admin == null || admin.getRole().equals("ROLE_USER")) {
            return RsData.of("403-F1", "관리자 권한이 없습니다.");
        }

        for (Attachment attachment : notice.getAttachments()) {
            deletePhysicalFile(attachment.getFilePath());
        }
        noticeRepository.delete(notice);

        return RsData.of("200-S1", "공지사항 삭제 성공");
    }

    // 첨부파일 업로드 (기존 로직 유지)
    @Transactional
    public RsData<AttachmentDto> uploadAttachment(MultipartFile file) {
        if (file.isEmpty()) {
            return RsData.of("400-F1", "업로드할 파일이 없습니다.");
        }

        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String storedFileName = UUID.randomUUID().toString() + "_" + originalFilename;
            String filePath = uploadDir + storedFileName;

            File dest = new File(filePath);
            file.transferTo(dest.getAbsoluteFile());

            Attachment attachment = Attachment.builder()
                    .uploadFileName(originalFilename)
                    .storedFileName(storedFileName)
                    .filePath(filePath)
                    .notice(null)
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

    // 첨부파일 다운로드 (기존 로직 유지)
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

    // 물리적 파일 삭제 헬퍼 메서드 (기존 로직 유지)
    private void deletePhysicalFile(String filePath) {
        try {
            java.nio.file.Path fileToDeletePath = java.nio.file.Paths.get(filePath).toAbsolutePath().normalize();
            java.nio.file.Files.deleteIfExists(fileToDeletePath);
        } catch (IOException e) {
            System.err.println("파일 삭제 실패: " + filePath + ", 오류: " + e.getMessage());
        }
    }
}