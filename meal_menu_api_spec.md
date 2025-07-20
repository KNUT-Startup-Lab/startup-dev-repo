# 기숙사 앱 식사 메뉴 API 명세서

| 기능 | HTTP Method | EndPoint | Request | Response | 기타 | 권한 | Status |
|------|-------------|----------|---------|----------|------|------|---------|
| 최신 식사 메뉴 이미지 조회 | GET | `/api/meal-menu/latest` | 없음 | `{ "imageUrl": "string", "updatedAt": "string" }` | 서버에 저장된 가장 최신 이미지 반환 | 없음 | 200: 성공<br>404: 이미지 없음<br>500: 서버 오류 |