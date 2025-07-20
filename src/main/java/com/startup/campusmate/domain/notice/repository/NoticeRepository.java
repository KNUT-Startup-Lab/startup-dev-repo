package com.startup.campusmate.domain.notice.repository;

import com.startup.campusmate.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

// Notice 엔티티에 대한 데이터베이스 작업을 처리하는 JpaRepository 인터페이스입니다.
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NoticeRepository extends JpaRepository<Notice, Long>, JpaSpecificationExecutor<Notice> { // JpaRepository<엔티티 클래스, ID 타입>

    /**
     * 원본 URL을 기준으로 게시물이 존재하는지 확인합니다.
     * 크롤링 시 중복된 게시물을 확인하는 데 사용됩니다.
     * @param url 확인할 원본 게시물의 URL
     * @return 해당 URL을 가진 게시물이 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByOriginalUrl(String url); // 메서드 이름으로 쿼리를 자동 생성합니다 (SELECT COUNT(*) > 0 FROM notice WHERE original_url = ?).

    // 새로 추가될 메서드: crawledNoticeNumber를 기준으로 게시물이 존재하는지 확인
    boolean existsByCrawledNoticeNumber(String crawledNoticeNumber);
}
