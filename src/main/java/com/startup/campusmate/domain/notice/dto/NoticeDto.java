package com.startup.campusmate.domain.notice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter // Lombok을 사용하여 Getter 메서드를 자동으로 생성합니다.
public class NoticeDto {

    private final Long id; // 공지사항의 고유 ID
    private final String title; // 제목
    private final String department; // 부서명
    private final String created_at; // 등록일
    private final int views; // 조회수
    private final String category; // 카테고리 (크롤링 or 직접 작성)

    @Builder // Lombok의 빌더 패턴을 사용하여 객체를 생성합니다.
    public NoticeDto(Long id, String title, String department, String created_at, int views, String category) {
        this.id = id; // ID 초기화
        this.title = title; // 제목 초기화
        this.department = department; // 부서명 초기화
        this.created_at = created_at; // 등록일 초기화
        this.views = views; // 조회수 초기화
        this.category = category; // 카테고리 초기화
    }
}
