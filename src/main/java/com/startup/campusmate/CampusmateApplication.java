package com.startup.campusmate;

import com.startup.campusmate.standard.crawler.NoticeCrawler;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CampusmateApplication {

    public CampusmateApplication(NoticeCrawler noticeCrawler) {
        this.noticeCrawler = noticeCrawler;
    }

    public static void main(String[] args) {
		SpringApplication.run(CampusmateApplication.class, args);
	}

	private final NoticeCrawler noticeCrawler;

	@Bean
	public ApplicationRunner init() {
		return args -> {
			System.out.println("서버 시작 시 초기 크롤링 시작...");
			noticeCrawler.crawlSchoolNotices();
			System.out.println("초기 크롤링 완료.");
		};
	}

}
