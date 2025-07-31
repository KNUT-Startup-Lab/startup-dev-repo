package com.startup.campusmate.domain.notice.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter // Lombok을 사용하여 Getter 메서드를 자동으로 생성합니다.
public class NoticeListRs {

    private final List<NoticeDto> notices; // 조회된 공지사항 목록
    private final long total; // 전체 공지사항 개수
    private final int page; // 현재 페이지 번호
    private final int size; // 페이지 당 항목 수

    @Builder // Lombok의 빌더 패턴을 사용하여 객체를 생성합니다.
    public NoticeListRs(List<NoticeDto> notices, long total, int page, int size) {
        this.notices = notices; // 공지사항 목록 초기화
        this.total = total; // 전체 개수 초기화
        this.page = page; // 페이지 번호 초기화
        this.size = size; // 페이지 크기 초기화
    }
}
