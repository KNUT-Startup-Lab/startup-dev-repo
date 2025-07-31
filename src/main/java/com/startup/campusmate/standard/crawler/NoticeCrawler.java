package com.startup.campusmate.standard.crawler;

import com.startup.campusmate.domain.notice.repository.NoticeRepository;
import com.startup.campusmate.domain.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component // Spring Bean으로 등록하여 스케줄링 및 의존성 주입이 가능하게 합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입을 처리합니다.
public class NoticeCrawler {

    private final NoticeService noticeService; // 공지사항 서비스 주입
    private final NoticeRepository noticeRepository; // 중복 확인을 위해 리포지토리 주입

    @Value("${custom.crawler.notice-url}")
    private String crawlUrl;

    // 1시간마다 실행되는 스케줄링 작업 (초 분 시 일 월 요일)
    // 매시 정각에 실행됩니다.
    @Scheduled(cron = "0 0 * * * *")
    @Transactional // 트랜잭션 내에서 동작하도록 설정합니다.
    public void crawlSchoolNotices() {
        System.out.println("크롤링 시작: " + crawlUrl);

        try {
            Document doc = Jsoup.connect(crawlUrl).get();

            Elements noticeRows = doc.select("table.basic_table.center > tbody > tr");

            for (Element row : noticeRows) {
                String title;
                String originalUrl;
                String department = row.select("td.problem_name").text();

                // problem_number 추출
                String problemNumber = row.select("td.problem_number").text(); // <td class="problem_number">199</td> 에서 199 추출

                // 상단 고정 공지사항인지 확인
                Element pinnedNoticeTitleElement = row.select("td.left div.list_subject span.link a b").first();

                if (pinnedNoticeTitleElement != null) {
                    // 상단 고정 공지사항
                    title = pinnedNoticeTitleElement.text();
                    originalUrl = row.select("td.left div.list_subject span.link a").attr("abs:href");
                } else {
                    // 일반 공지사항
                    Element formElement = row.select("td.left form[name=subForm]").first();
                    if (formElement == null) {
                        continue; // 유효한 공지사항 행이 아니면 건너뛰기
                    }
                    title = formElement.select("input[type=submit]").attr("value");
                    String formAction = formElement.attr("action");
                    String nttId = formElement.select("input[name=nttId]").attr("value");
                    originalUrl = "https://www.ut.ac.kr" + formAction + "?nttId=" + nttId;
                }

                // 중복 게시물 확인 (problem_number로 먼저 확인)
                if (noticeRepository.existsByCrawledNoticeNumber(problemNumber)) {
                    System.out.println("중복 게시물 (순번) 건너뛰기: " + title + " (순번: " + problemNumber + ")");
                    continue;
                }

                // 기존 originalUrl 중복 확인 로직은 유지 (혹시 problem_number가 유일하지 않을 경우 대비)
                if (noticeRepository.existsByOriginalUrl(originalUrl)) {
                    System.out.println("중복 게시물 (URL) 건너뛰기: " + title);
                    continue;
                }

                // 크롤링된 공지사항 저장
                noticeService.createCrawledNotice(title, department, originalUrl, problemNumber); // problemNumber 전달
                System.out.println("새 공지사항 발견: " + title + " - " + department + " - " + originalUrl + " (순번: " + problemNumber + ")");
            }
            System.out.println("크롤링 완료.");

        } catch (IOException e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
