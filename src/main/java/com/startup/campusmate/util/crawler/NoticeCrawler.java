package com.startup.campusmate.util.crawler;

import com.startup.campusmate.domain.notice.entity.NoticeCategory;
import com.startup.campusmate.domain.notice.repository.NoticeRepository;
import com.startup.campusmate.domain.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component // Spring Bean으로 등록하여 스케줄링 및 의존성 주입이 가능하게 합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입을 처리합니다.
public class NoticeCrawler {

    private final NoticeService noticeService; // 공지사항 서비스 주입
    private final NoticeRepository noticeRepository; // 중복 확인을 위해 리포지토리 주입

    // 1시간마다 실행되는 스케줄링 작업 (초 분 시 일 월 요일)
    // 매시 정각에 실행됩니다.
    @Scheduled(cron = "0 0 * * * *")
    @Transactional // 트랜잭션 내에서 동작하도록 설정합니다.
    public void crawlSchoolNotices() {
        String url = "https://www.example.com/school/notices"; // TODO: 실제 학교 공지사항 URL로 변경해야 합니다.
        System.out.println("크롤링 시작: " + url);

        try {
            // Jsoup을 사용하여 웹 페이지를 가져옵니다.
            Document doc = Jsoup.connect(url).get();

            // TODO: 실제 학교 공지사항 페이지의 HTML 구조에 맞춰 선택자를 변경해야 합니다.
            // 예시: 공지사항 목록을 포함하는 테이블 또는 div
            Elements noticeRows = doc.select("table.board-list tr"); // 예시 선택자

            for (Element row : noticeRows) {
                // TODO: 각 공지사항의 제목, 부서명, 원본 URL 등을 추출합니다.
                // 예시: 제목, 링크, 부서명
                String title = row.select("td.title a").text();
                String originalUrl = row.select("td.title a").attr("abs:href");
                String department = row.select("td.department").text();

                // 중복 게시물 확인
                if (noticeRepository.existsByOriginalUrl(originalUrl)) {
                    System.out.println("중복 게시물 건너뛰기: " + title);
                    continue;
                }

                // 크롤링된 공지사항 저장
                // TODO: NoticeService에 크롤링된 공지사항을 저장하는 별도의 메서드를 추가해야 합니다.
                // 현재는 임시로 직접 엔티티를 생성하여 저장합니다.
                // noticeService.createCrawledNotice(title, department, originalUrl);
                System.out.println("새 공지사항 발견: " + title + " - " + department + " - " + originalUrl);
            }
            System.out.println("크롤링 완료.");

        } catch (IOException e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
