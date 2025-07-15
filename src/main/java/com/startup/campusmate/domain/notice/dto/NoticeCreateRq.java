package com.startup.campusmate.domain.notice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter // Lombok을 사용하여 Getter 메서드를 자동으로 생성합니다.
@Setter // Lombok을 사용하여 Setter 메서드를 자동으로 생성합니다.
public class NoticeCreateRq {

    private String title; // 생성할 공지사항의 제목
    private String content; // 생성할 공지사항의 내용
    private String department; // 공지사항을 게시하는 부서
    private List<Long> attachments; // 첨부된 파일들의 ID 목록
}
