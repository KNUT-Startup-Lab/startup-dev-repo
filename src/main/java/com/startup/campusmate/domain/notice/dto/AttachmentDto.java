package com.startup.campusmate.domain.notice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter // Lombok을 사용하여 Getter 메서드를 자동으로 생성합니다.
public class AttachmentDto {

    private final String file_id; // 첨부파일의 고유 ID (서버에서 관리하는 ID)
    private final String filename; // 원본 파일의 이름
    private final String file_url; // 파일을 다운로드할 수 있는 URL

    @Builder // Lombok의 빌더 패턴을 사용하여 객체를 생성합니다.
    public AttachmentDto(String file_id, String filename, String file_url) {
        this.file_id = file_id; // 파일 ID 초기화
        this.filename = filename; // 파일명 초기화
        this.file_url = file_url; // 파일 URL 초기화
    }
}
