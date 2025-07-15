package com.startup.campusmate.domain.notice.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import com.startup.campusmate.global.security.MemberContext;

import com.startup.campusmate.domain.notice.dto.NoticeCreateRq;
import com.startup.campusmate.domain.notice.dto.NoticeListRs;
import com.startup.campusmate.domain.notice.dto.NoticeUpdateRq;
import com.startup.campusmate.domain.notice.service.NoticeService;
import com.startup.campusmate.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 공지사항 목록 조회
    @GetMapping
    public RsData<NoticeListRs> getNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "search_type") String searchType,
            @RequestParam(required = false, name = "search_keyword") String searchKeyword,
            @RequestParam(required = false, name = "start_date") String startDateStr,
            @RequestParam(required = false, name = "end_date") String endDateStr,
            @RequestParam(defaultValue = "createDate") String sort) {

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (startDateStr != null) {
            startDate = LocalDateTime.parse(startDateStr + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (endDateStr != null) {
            endDate = LocalDateTime.parse(endDateStr + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        return noticeService.getNotices(page, size, category, searchType, searchKeyword, startDate, endDate, sort);
    }

    // 공지사항 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<RsData<?>> getNoticeDetail(@PathVariable Long id) {
        RsData<?> rsData = noticeService.getNoticeDetail(id);
        if (rsData.isFail()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(rsData);
        }
        return ResponseEntity.ok(rsData);
    }

    // 크롤링 게시물 URL 조회
    @GetMapping("/{id}/url")
    public ResponseEntity<RsData<?>> getOriginalUrl(@PathVariable Long id) {
        RsData<?> rsData = noticeService.getOriginalUrl(id);
        if (rsData.isFail()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(rsData);
        }
        return ResponseEntity.ok(rsData);
    }

    // 직접 작성 게시물 생성 (관리자)
    @PostMapping
    public ResponseEntity<RsData<?>> createNotice(@RequestBody NoticeCreateRq rq) {
        Long adminMemberId = ((MemberContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        RsData<?> rsData = noticeService.createNotice(rq, adminMemberId);
        if (rsData.isFail()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(rsData);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(rsData);
    }

    // 직접 작성 게시물 수정 (관리자)
    @PutMapping("/{id}")
    public ResponseEntity<RsData<?>> updateNotice(@PathVariable Long id, @RequestBody NoticeUpdateRq rq) {
        Long adminMemberId = ((MemberContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        RsData<?> rsData = noticeService.updateNotice(id, rq, adminMemberId);
        if (rsData.isFail()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(rsData);
        }
        return ResponseEntity.ok(rsData);
    }

    // 게시물 삭제 (관리자)
    @DeleteMapping("/{id}")
    public ResponseEntity<RsData<?>> deleteNotice(@PathVariable Long id) {
        Long adminMemberId = ((MemberContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        RsData<?> rsData = noticeService.deleteNotice(id, adminMemberId);
        if (rsData.isFail()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(rsData);
        }
        return ResponseEntity.ok(rsData);
    }

    // 첨부파일 업로드 (관리자)
    @PostMapping("/attachments")
    public ResponseEntity<RsData<?>> uploadAttachment(@RequestParam("file") MultipartFile file) {
        RsData<?> rsData = noticeService.uploadAttachment(file);
        if (rsData.isFail()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(rsData);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(rsData);
    }

    // 첨부파일 다운로드
    @GetMapping("/attachments/{fileId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long fileId) {
        RsData<File> rsData = noticeService.downloadAttachment(fileId);

        if (rsData.isFail()) {
            // 파일이 없거나 유효하지 않은 경우 404 또는 500 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            File file = rsData.getData();
            Resource resource = new UrlResource(file.toURI());

            String contentType = Files.probeContentType(Paths.get(file.getAbsolutePath()));
            if (contentType == null) {
                contentType = "application/octet-stream"; // 기본값
            }

            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}