package com.startup.campusmate.domain.notice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NoticeCreateRq {

    private String title;
    private String content;
    private String department;
    private List<Long> attachments;

    // 새로 추가될 필드
    private boolean isCrawled; // 이 공지사항이 크롤링된 것인지 여부
    private String originalUrl; // 크롤링된 경우 원본 URL
}