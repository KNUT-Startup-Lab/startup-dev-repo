# 기숙사 앱 공지사항 API 명세서

## 1. 공지사항 목록 조회

| 기능 | HTTP Method | EndPoint | Request | Response | 기타 | 권한 | Status |
|------|-------------|----------|---------|----------|------|------|---------|
| 공지사항 목록 조회 | GET | `/api/notices` | `page`, `size`, `category`, `search_type`, `search_keyword`, `start_date`, `end_date`, `sort` | `{ "notices": [], "total": number, "page": number, "size": number }` | 페이지네이션, 검색, 정렬 기능 | 학생, 관리자 | 200: 성공<br>400: 잘못된 요청<br>500: 서버 오류 |

## 2. 공지사항 상세 조회

| 기능 | HTTP Method | EndPoint | Request | Response | 기타 | 권한 | Status |
|------|-------------|----------|---------|----------|------|------|---------|
| 직접 작성 게시물 상세 조회 | GET | `/api/notices/{id}` | `id` (path) | `{ "id": number, "title": string, "content": string, "attachments": [], "created_at": string, "updated_at": string, "views": number, "department": string, "category": string }` | 조회수 증가, 첨부파일 포함 | 학생, 관리자 | 200: 성공<br>404: 게시물 없음<br>500: 서버 오류 |
| 크롤링 게시물 URL 조회 | GET | `/api/notices/{id}/url` | `id` (path) | `{ "original_url": string, "title": string }` | 웹뷰어용 원본 URL 반환 | 학생, 관리자 | 200: 성공<br>404: 게시물 없음<br>500: 서버 오류 |

## 3. 공지사항 작성/수정/삭제 (관리자)

| 기능 | HTTP Method | EndPoint | Request | Response | 기타 | 권한 | Status |
|------|-------------|----------|---------|----------|------|------|---------|
| 직접 작성 게시물 생성 | POST | `/api/notices` | `{ "title": string, "content": string, "department": string, "attachments": [] }` | `{ "id": number, "message": string }` | 첨부파일 포함, 푸시 알림 발송 | 관리자 | 201: 생성 성공<br>400: 잘못된 요청<br>401: 권한 없음<br>500: 서버 오류 |
| 직접 작성 게시물 수정 | PUT | `/api/notices/{id}` | `id` (path), `{ "title": string, "content": string, "department": string, "attachments": [] }` | `{ "message": string }` | 첨부파일 수정 가능 | 관리자 | 200: 수정 성공<br>400: 잘못된 요청<br>401: 권한 없음<br>404: 게시물 없음<br>500: 서버 오류 |
| 게시물 삭제 | DELETE | `/api/notices/{id}` | `id` (path) | `{ "message": string }` | 크롤링/직접 작성 모두 삭제 가능 | 관리자 | 200: 삭제 성공<br>401: 권한 없음<br>404: 게시물 없음<br>500: 서버 오류 |

## 4. 첨부파일 관리

| 기능 | HTTP Method | EndPoint | Request | Response | 기타 | 권한 | Status |
|------|-------------|----------|---------|----------|------|------|---------|
| 첨부파일 업로드 | POST | `/api/notices/attachments` | `file` (multipart/form-data) | `{ "file_id": string, "filename": string, "file_url": string }` | 직접 작성 게시물용 | 관리자 | 201: 업로드 성공<br>400: 잘못된 파일<br>401: 권한 없음<br>500: 서버 오류 |
| 첨부파일 다운로드 | GET | `/api/notices/attachments/{file_id}` | `file_id` (path) | 파일 데이터 | 직접 작성 게시물용 | 학생, 관리자 | 200: 다운로드 성공<br>404: 파일 없음<br>500: 서버 오류 |


