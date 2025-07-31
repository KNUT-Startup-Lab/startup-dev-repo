package com.startup.campusmate.domain.notice.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // JsonProperty 임포트
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class NoticeDetailRs {

    private final Long id;
    private final String title;
    private final String content;
    private final String department;
    private final String created_at;
    private final String updated_at;
    private final int views;
    private final String category;
    private final List<AttachmentDto> attachments;

    // 새로 추가될 필드
    @JsonProperty("isCrawled") // JSON 직렬화 시 필드명을 "isCrawled"로 명시
    private final boolean isCrawled; // 이 공지사항이 크롤링된 것인지 여부
    private final String originalUrl; // 크롤링된 경우 원본 URL

    @Builder
    public NoticeDetailRs(Long id, String title, String content, String department, String created_at, String updated_at, int views, String category, List<AttachmentDto> attachments, boolean isCrawled, String originalUrl) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.department = department;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.views = views;
        this.category = category;
        this.attachments = attachments;
        this.isCrawled = isCrawled;
        this.originalUrl = originalUrl;
    }
}