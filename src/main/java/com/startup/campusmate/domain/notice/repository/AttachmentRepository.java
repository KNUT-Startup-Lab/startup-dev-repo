package com.startup.campusmate.domain.notice.repository;

import com.startup.campusmate.domain.notice.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

// Attachment 엔티티에 대한 데이터베이스 작업을 처리하는 JpaRepository 인터페이스입니다.
public interface AttachmentRepository extends JpaRepository<Attachment, Long> { // JpaRepository<엔티티 클래스, ID 타입>
    // 기본적인 CRUD(Create, Read, Update, Delete) 메서드가 자동으로 제공됩니다.
    // 추가적인 쿼리 메서드가 필요할 경우 여기에 정의할 수 있습니다.
}
