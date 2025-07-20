package com.startup.campusmate.domain.notice.entity;

import lombok.Getter;

// 게시물의 카테고리를 정의하는 Enum 클래스
@Getter
public enum NoticeCategory {
    CRAWLED("크롤링"), // 학교 홈페이지에서 수집한 공지
    CUSTOM("직접 작성"); // 관리자가 직접 작성한 공지

    private final String description; // 카테고리에 대한 설명

    // 생성자를 통해 각 Enum 상수에 설명을 부여
    NoticeCategory(String description) {
        this.description = description;
    }
}
