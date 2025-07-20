package com.startup.campusmate.domain.notice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter // Lombok을 사용하여 Getter 메서드를 자동으로 생성합니다.
@Setter // Lombok을 사용하여 Setter 메서드를 자동으로 생성합니다.
public class NoticeUpdateRq {

    private String title; // 수정할 공지사항의 제목
    private String content; // 수정할 공지사항의 내용
    private String department; // 수정할 공지사항의 부서
    private List<Long> attachments; // 수정 후의 첨부파일 ID 목록
}
