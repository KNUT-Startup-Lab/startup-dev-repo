package com.startup.campusmate.domain.notice.entity;

import com.startup.campusmate.domain.notice.dto.AttachmentDto;
import com.startup.campusmate.global.jpa.BaseTime;
import jakarta.persistence.*;
import lombok.*;

@Entity // JPA 엔티티임을 선언합니다.
@Getter // Lombok을 사용하여 Getter 메서드를 자동으로 생성합니다.
@Setter // Lombok을 사용하여 Setter 메서드를 자동으로 생성합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Lombok을 사용하여 파라미터 없는 생성자를 생성하며, 접근 수준을 PROTECTED로 설정합니다.
public class Attachment extends BaseTime { // 공통 시간 필드를 가진 BaseTime을 상속받습니다.

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키(PK)이며, 데이터베이스가 자동으로 값을 생성(IDENTITY 전략)하도록 설정합니다.
    private Long id; // 첨부파일의 고유 ID

    @Column(nullable = false) // 데이터베이스 컬럼에 매핑하며, null을 허용하지 않습니다.
    private String uploadFileName; // 사용자가 업로드한 원본 파일의 이름

    @Column(nullable = false) // null을 허용하지 않는 컬럼입니다.
    private String storedFileName; // 서버에 저장될 때의 파일 이름 (중복 방지를 위해 변경될 수 있음)

    @Column(nullable = false) // null을 허용하지 않는 컬럼입니다.
    private String filePath; // 파일이 서버에 저장된 경로

    @ManyToOne(fetch = FetchType.LAZY) // Notice 엔티티와 다대일(N:1) 관계를 맺습니다. 지연 로딩을 사용합니다.
    @JoinColumn(name = "notice_id", nullable = true) // 외래키 컬럼의 이름을 'notice_id'로 지정하며, null을 허용합니다.
    private Notice notice; // 이 첨부파일이 속한 공지사항

    @Builder // Lombok의 빌더 패턴을 사용하여 객체를 생성합니다.
    public Attachment(String uploadFileName, String storedFileName, String filePath, Notice notice) {
        this.uploadFileName = uploadFileName; // 원본 파일명 초기화
        this.storedFileName = storedFileName; // 저장된 파일명 초기화
        this.filePath = filePath; // 파일 경로 초기화
        this.notice = notice; // 연관된 공지사항 초기화
    }

    public AttachmentDto toDto() {
        return AttachmentDto.builder()
                .file_id(this.getId().toString())
                .filename(this.getUploadFileName())
                .file_url("/api/notices/attachments/" + this.getId())
                .build();
    }
}
