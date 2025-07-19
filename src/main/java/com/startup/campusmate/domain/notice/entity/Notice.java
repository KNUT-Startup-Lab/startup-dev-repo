package com.startup.campusmate.domain.notice.entity;

import com.startup.campusmate.domain.member.entity.Member;
import com.startup.campusmate.domain.notice.dto.NoticeDetailRs;
import com.startup.campusmate.domain.notice.dto.NoticeDto;
import com.startup.campusmate.global.jpa.BaseTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity // JPA 엔티티임을 선언합니다.
@Getter // Lombok을 사용하여 Getter 메서드를 자동으로 생성합니다.
@Setter // Lombok을 사용하여 Setter 메서드를 자동으로 생성합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Lombok을 사용하여 파라미터 없는 생성자를 생성하며, 접근 수준을 PROTECTED로 설정합니다.
public class Notice extends BaseTime { // 공통 필드를 가진 BaseEntity를 상속받습니다.

    @Column(nullable = false) // 데이터베이스 컬럼에 매핑하며, null을 허용하지 않습니다.
    private String title; // 공지사항의 제목

    @Lob // 대용량 데이터를 저장할 수 있도록 LOB(Large Object) 타입으로 지정합니다.
    private String content; // 공지사항의 내용 (직접 작성 게시물용)

    @Column(nullable = false) // null을 허용하지 않는 컬럼입니다.
    private int views; // 조회수

    @Column(nullable = false) // null을 허용하지 않는 컬럼입니다.
    private String department; // 공지사항을 게시한 부서명

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 데이터베이스에 저장합니다.
    @Column(nullable = false) // null을 허용하지 않는 컬럼입니다.
    private NoticeCategory category; // 게시물 카테고리 (크롤링 or 직접 작성)

    @Column(nullable = false) // null을 허용하지 않는 컬럼입니다.
    private boolean isCrawled; // 크롤링된 게시물인지 여부를 나타내는 플래그

    private String originalUrl;

    // 새로 추가될 필드: 크롤링된 공지사항의 순번
    private String crawledNoticeNumber; // 학교 홈페이지의 problem_number

    @ManyToOne(fetch = FetchType.LAZY) // Member 엔티티와 다대일(N:1) 관계를 맺습니다. 지연 로딩을 사용합니다.
    @JoinColumn(name = "member_id") // 외래키 컬럼의 이름을 'member_id'로 지정합니다.
    private Member author; // 작성자 정보 (관리자)

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true) // Attachment 엔티티와 일대다(1:N) 관계를 맺습니다. 모든 영속성 작업을 전파하고, 고아 객체를 제거합니다.
    private List<Attachment> attachments = new ArrayList<>(); // 첨부파일 목록

    @Builder // Lombok의 빌더 패턴을 사용하여 객체를 생성합니다.
    public Notice(String title, String content, String department, NoticeCategory category, boolean isCrawled, String originalUrl, Member author, String crawledNoticeNumber) { // crawledNoticeNumber 추가
        this.title = title; // 제목 초기화
        this.content = content; // 내용 초기화
        this.department = department; // 부서명 초기화
        this.category = category; // 카테고리 초기화
        this.isCrawled = isCrawled; // 크롤링 여부 초기화
        this.originalUrl = originalUrl; // 원본 URL 초기화
        this.author = author; // 작성자 초기화
        this.crawledNoticeNumber = crawledNoticeNumber; // 필드 초기화
        this.views = 0; // 조회수는 0으로 초기화
    }

    public NoticeDto toDto() {
        return NoticeDto.builder()
                .id(this.getId())
                .title(this.getTitle())
                .department(this.getDepartment())
                .created_at(this.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .views(this.getViews())
                .category(this.getCategory().getDescription())
                .build();
    }

    public NoticeDetailRs toDetailRs() {
        return NoticeDetailRs.builder()
                .id(this.getId())
                .title(this.getTitle())
                .content(this.getContent())
                .department(this.getDepartment())
                .created_at(this.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .updated_at(this.getModifyDate() != null ? this.getModifyDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null)
                .views(this.getViews())
                .category(this.getCategory().getDescription())
                .isCrawled(this.isCrawled())
                .originalUrl(this.getOriginalUrl())
                .attachments(this.getAttachments().stream().map(Attachment::toDto).collect(Collectors.toList()))
                .build();
    }
}
