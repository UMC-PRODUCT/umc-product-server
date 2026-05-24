# API Catalog

운영 중인 API의 ID, 엔드포인트, 메서드, 역할, deprecated 상태를 도메인별로 정리합니다.

> 소스 기준: `@Operation(operationId = "...")`를 우선 사용하고, 없으면 `summary`의 `[XXX-000]` prefix를 API ID로 읽습니다. 갱신: `./gradlew generateDocumentationCatalogs`

## analytics

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 1 | analytics | DASHBOARD-001 | `/api/v1/admin/dashboard/summary` | GET | 운영진 대시보드 요약 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:44` |
| 2 | analytics | DASHBOARD-002 | `/api/v1/admin/dashboard/action-queue` | GET | 운영진 대시보드 액션 큐 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:56` |
| 3 | analytics | DASHBOARD-003 | `/api/v1/admin/dashboard/risk-challengers` | GET | 운영진 대시보드 위험군 챌린저 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:91` |
| 4 | analytics | DASHBOARD-004 | `/api/v1/admin/dashboard/context` | GET | 운영진 대시보드 권한 컨텍스트 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:68` |
| 5 | analytics | DASHBOARD-005 | `/api/v1/admin/dashboard/operations` | GET | 운영 현황 집계 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:79` |
| 6 | analytics | DASHBOARD-100 | `/api/v1/admin/schools/summary` | GET | 학교별 현황 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminSchoolAnalyticsController.java:30` |

## audit

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 7 | audit | AUDIT-001 | `/api/v1/admin/audit-logs` | GET | 감사 로그 검색 | X | `src/main/java/com/umc/product/audit/adapter/in/web/AuditLogController.java:33` |

## authentication

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 8 | authentication | CREDENTIAL-002 | `/api/v1/auth/credentials` | POST | 비밀번호 자격증명 최초 등록 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:45` |
| 9 | authentication | CREDENTIAL-003 | `/api/v1/auth/password` | PATCH | 비밀번호 변경 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:58` |
| 10 | authentication | CREDENTIAL-005 | `/api/v1/auth/email/availability` | GET | 이메일 사용 가능 여부 조회 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:87` |
| 11 | authentication | CREDENTIAL-007 | `/api/v1/auth/password/reset` | PATCH | 비밀번호 초기화 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:71` |
| 12 | authentication | EMAIL-001 | `/api/v1/auth/email-verification/code` | POST | 6자리 인증코드로 이메일 인증 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/EmailAuthenticationController.java:34` |
| 13 | authentication | EMAIL-002 | `/api/v1/auth/email-verification` | POST | 이메일 인증 코드 발송 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/EmailAuthenticationController.java:60` |
| 14 | authentication | EMAIL-003 | `/api/v1/auth/email-verification/resend` | POST | 이메일 인증 코드 재전송 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/EmailAuthenticationController.java:85` |
| 15 | authentication | LOGIN-001 | `/api/v1/auth/login/google` | POST | Google 로그인 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/swagger/AuthenticationControllerInterface.java:14` |
| 16 | authentication | LOGIN-005 | `/api/v1/auth/login/kakao` | POST | Kakao 로그인 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/swagger/AuthenticationControllerInterface.java:32` |
| 17 | authentication | LOGIN-006 | `/api/v1/auth/login/email` | POST | 이메일/PW 로그인 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:98` |
| 18 | authentication | LOGIN-006 | `/api/v1/auth/login/kakao/code` | POST | Kakao 로그인 (Authorization Code 흐름) | X | `src/main/java/com/umc/product/authentication/adapter/in/web/swagger/AuthenticationControllerInterface.java:50` |
| 19 | authentication | LOGIN-010 | `/api/v1/auth/login/apple` | POST | Apple 로그인 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/swagger/AuthenticationControllerInterface.java:68` |
| 20 | authentication | OAUTH-001 | `/api/v1/member-oauth` | POST | 로그인용 OAuth 수단 추가 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/MemberOAuthController.java:37` |
| 21 | authentication | OAUTH-002 | `/api/v1/member-oauth/{memberOAuthId}` | DELETE | 로그인용 OAuth 수단 제거 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/MemberOAuthController.java:58` |
| 22 | authentication | OAUTH-101 | `/api/v1/member-oauth/me` | GET | 현재 회원 계정과 연동된 OAuth 정보 조회 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/MemberOAuthController.java:81` |
| 23 | authentication | TOKEN-001 | `/api/v1/auth/token/renew` | POST | AccessToken 재발급 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/TokenAuthenticationController.java:23` |

## authorization

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 24 | authorization | PERMISSION-001 | `/api/v1/authorization/resource-permission` | GET | 리소스 권한 조회 | X | `src/main/java/com/umc/product/authorization/adapter/in/web/ResourcePermissionController.java:28` |
| 25 | authorization | STAFF-001 | `/api/v1/authorization/challenger-role` | POST | 운영진 기록 생성 | X | `src/main/java/com/umc/product/authorization/adapter/in/web/ChallengerRoleController.java:45` |
| 26 | authorization | STAFF-002 | `/api/v1/authorization/challenger-role/{challengerRoleId}` | DELETE | 운영진 기록 삭제 | X | `src/main/java/com/umc/product/authorization/adapter/in/web/ChallengerRoleController.java:77` |
| 27 | authorization | STAFF-101 | `/api/v1/authorization/challenger-role/{challengerRoleId}` | GET | 운영진 기록 조회 | O | `src/main/java/com/umc/product/authorization/adapter/in/web/ChallengerRoleController.java:61` |

## challenger

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 28 | challenger | CHALLENGER-001 | `/api/v1/challenger` | POST | 챌린저 생성 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:40` |
| 29 | challenger | CHALLENGER-002 | `/api/v1/challenger/batch` | POST | 챌린저 batch 생성 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:52` |
| 30 | challenger | CHALLENGER-003 | `/api/v1/challenger/{challengerId}/deactivate` | POST | 챌린저 비활성화 (제명/탈부 처리) | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:70` |
| 31 | challenger | CHALLENGER-004 | `/api/v1/challenger/{challengerId}/part` | PATCH | 챌린저 파트 변경 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:83` |
| 32 | challenger | CHALLENGER-005 | `/api/v1/challenger/{challengerId}` | DELETE | [주의] 챌린저 삭제 (Hard Delete) | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:99` |
| 33 | challenger | CHALLENGER-101 | `/api/v1/challenger/{challengerId}` | GET | 챌린저 정보 조회 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerQueryController.java:44` |
| 34 | challenger | CHALLENGER-102 | `/api/v1/challenger/search/cursor` | GET | 챌린저 검색 (Cursor 기반) | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerSearchController.java:29` |
| 35 | challenger | CHALLENGER-103 | `/api/v1/challenger/search/offset` | GET | 챌린저 검색 (Offset 기반) | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerSearchController.java:52` |
| 36 | challenger | CHALLENGER-104 | `/api/v1/challenger/search/global` | GET | deprecated: 챌린저 전체 검색 (Cursor 기반, 일정 생성용) | O | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerSearchController.java:66` |
| 37 | challenger | CHALLENGER-201 | `/api/v2/challenger/search` | GET | 챌린저 검색 v2 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/v2/ChallengerSearchV2Controller.java:31` |
| 38 | challenger | CHALLENGER-RECORD-001 | `/api/v1/challenger-record/member` | POST | 6자리 코드를 이용해서 회원(계정)에 챌린저 기록 추가 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:36` |
| 39 | challenger | CHALLENGER-RECORD-002 | `/api/v1/challenger-record` | POST | [ADMIN] 과거 챌린저 기록을 위한 코드 생성 기능 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:89` |
| 40 | challenger | CHALLENGER-RECORD-003 | `/api/v1/challenger-record/bulk` | POST | [ADMIN] 챌린저 기록용 코드 벌크 추가 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:116` |
| 41 | challenger | CHALLENGER-RECORD-101 | `/api/v1/challenger-record/code/{code}` | GET | 코드로 ChallengerRecord 조회 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:65` |
| 42 | challenger | CHALLENGER-RECORD-102 | `/api/v1/challenger-record/id/{id}` | GET | ID로 ChallengerRecord 조회 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:77` |
| 43 | challenger | POINT-001 | `/api/v1/challenger/{challengerId}/points` | POST | 챌린저 상벌점 부여 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandController.java:38` |
| 44 | challenger | POINT-002 | `/api/v1/challenger/points/{challengerPointId}` | PATCH | 챌린저 상벌점 사유 수정 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandController.java:55` |
| 45 | challenger | POINT-003 | `/api/v1/challenger/points/{challengerPointId}` | DELETE | 챌린저 상벌점 삭제 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandController.java:70` |

## community

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 46 | community | COMMENT-001 | `/api/v1/posts/{postId}/comments` | POST | 댓글 작성 | X | `src/main/java/com/umc/product/community/adapter/in/web/CommentController.java:42` |
| 47 | community | COMMENT-002 | `/api/v1/posts/{postId}/comments/{commentId}` | DELETE | 댓글 삭제 | X | `src/main/java/com/umc/product/community/adapter/in/web/CommentController.java:74` |
| 48 | community | COMMENT-003 | `/api/v1/posts/{postId}/comments/{commentId}/like` | POST | 댓글 좋아요 토글 | X | `src/main/java/com/umc/product/community/adapter/in/web/CommentController.java:86` |
| 49 | community | COMMENT-101 | `/api/v1/posts/{postId}/comments` | GET | 댓글 목록 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/CommentController.java:56` |
| 50 | community | POST-001 | `/api/v1/posts` | POST | 일반 게시글 생성 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:55` |
| 51 | community | POST-002 | `/api/v1/posts/lightning` | POST | 번개글 생성 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:66` |
| 52 | community | POST-003 | `/api/v1/posts/{postId}` | PATCH | 일반 게시글 수정 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:83` |
| 53 | community | POST-004 | `/api/v1/posts/{postId}/lightning` | PATCH | 번개글 수정 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:98` |
| 54 | community | POST-005 | `/api/v1/posts/{postId}` | DELETE | 게시글 삭제 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:113` |
| 55 | community | POST-006 | `/api/v1/posts/{postId}/like` | POST | 게시글 좋아요 토글 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:121` |
| 56 | community | POST-007 | `/api/v1/posts/{postId}/scrap` | POST | 게시글 스크랩 토글 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:132` |
| 57 | community | POST-101 | `/api/v1/posts/{postId}` | GET | 게시글 상세 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:65` |
| 58 | community | POST-102 | `/api/v1/posts` | GET | 게시글 목록 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:75` |
| 59 | community | POST-103 | `/api/v1/posts/search` | GET | 게시글 검색 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:95` |
| 60 | community | POST-104 | `/api/v1/posts/my` | GET | 내가 쓴 글 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:112` |
| 61 | community | POST-105 | `/api/v1/posts/commented` | GET | 댓글 단 글 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:125` |
| 62 | community | POST-106 | `/api/v1/posts/scrapped` | GET | 스크랩한 글 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:138` |
| 63 | community | REPORT-001 | `/api/v1/posts/{postId}/reports` | POST | 게시글 신고 | X | `src/main/java/com/umc/product/community/adapter/in/web/ReportController.java:32` |
| 64 | community | REPORT-002 | `/api/v1/comments/{commentId}/reports` | POST | 댓글 신고 | X | `src/main/java/com/umc/product/community/adapter/in/web/ReportController.java:44` |
| 65 | community | TROPHY-001 | `/api/v1/trophies` | POST | 베스트 워크북 생성 | X | `src/main/java/com/umc/product/community/adapter/in/web/TrophyController.java:28` |
| 66 | community | TROPHY-101 | `/api/v1/trophies` | GET | 상장 목록 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/TrophyQueryController.java:24` |

## curriculum

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 67 | curriculum | CHALLENGER-WORKBOOK-001 | `/api/v2/curriculums/challenger-workbooks/deploy` | POST | 챌린저용: 특정 원본 워크북 배포 요청 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:21` |
| 68 | curriculum | CHALLENGER-WORKBOOK-002 | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}` | PATCH | 챌린저 워크북 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:47` |
| 69 | curriculum | CHALLENGER-WORKBOOK-003 | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}` | DELETE | 챌린저 워크북 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:66` |
| 70 | curriculum | CHALLENGER-WORKBOOK-004 | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}/excuse` | POST | 회장단용: 특정 워크북 인정 처리 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:84` |
| 71 | curriculum | CHALLENGER-WORKBOOK-005 | `/api/v2/curriculums/challenger-workbooks/weekly-best` | POST | 베스트 워크북 선정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:104` |
| 72 | curriculum | CHALLENGER-WORKBOOK-006 | `/api/v2/curriculums/challenger-workbooks/weekly-best/{weeklyBestWorkbookId}` | PATCH | 베스트 워크북 선정 사유 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:126` |
| 73 | curriculum | CHALLENGER-WORKBOOK-007 | `/api/v2/curriculums/challenger-workbooks/weekly-best/{weeklyBestWorkbookId}` | DELETE | 베스트 워크북 선정 철회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:145` |
| 74 | curriculum | CHALLENGER-WORKBOOK-MISSION-001 | `/api/v2/curriculums/challenger-workbooks/missions` | POST | 챌린저용: 워크북 내 미션 제출 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:25` |
| 75 | curriculum | CHALLENGER-WORKBOOK-MISSION-002 | `/api/v2/curriculums/challenger-workbooks/missions/{missionSubmissionId}` | PATCH | 챌린저용: 제출한 워크북 미션 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:53` |
| 76 | curriculum | CHALLENGER-WORKBOOK-MISSION-003 | `/api/v2/curriculums/challenger-workbooks/missions/{missionSubmissionId}` | DELETE | 챌린저용: 제출한 워크북 미션 철회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:76` |
| 77 | curriculum | CHALLENGER-WORKBOOK-MISSION-004 | `/api/v2/curriculums/challenger-workbooks/missions/feedback` | POST | 운영진용: 제출된 미션에 대한 피드백 작성 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:93` |
| 78 | curriculum | CHALLENGER-WORKBOOK-MISSION-005 | `/api/v2/curriculums/challenger-workbooks/missions/feedback/{missionFeedbackId}` | PATCH | 운영진용: 제출된 미션에 대한 피드백 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:117` |
| 79 | curriculum | CHALLENGER-WORKBOOK-MISSION-006 | `/api/v2/curriculums/challenger-workbooks/missions/feedback/{missionFeedbackId}` | DELETE | 운영진용: 제출된 미션에 대한 피드백 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:136` |
| 80 | curriculum | CURRICULUM-001 | `/api/v2/curriculums` | POST | 커리큘럼 생성 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:30` |
| 81 | curriculum | CURRICULUM-002 | `/api/v2/curriculums/{curriculumId}` | PATCH | 커리큘럼 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:49` |
| 82 | curriculum | CURRICULUM-003 | `/api/v2/curriculums/{curriculumId}` | DELETE | 중앙운영사무국 총괄단용: 커리큘럼 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:73` |
| 83 | curriculum | CURRICULUM-004 | `/api/v2/curriculums/weekly` | POST | 각 커리큘럼에 새로운 주차 생성 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:94` |
| 84 | curriculum | CURRICULUM-005 | `/api/v2/curriculums/weekly/{weeklyCurriculumId}` | PATCH | 주차별 커리큘럼 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:115` |
| 85 | curriculum | CURRICULUM-006 | `/api/v2/curriculums/weekly/{weeklyCurriculumId}` | DELETE | 주차별 커리큘럼 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:140` |
| 86 | curriculum | CURRICULUM-101 | `/api/v2/curriculums/overview` | GET | 특정 기수의 파트별 커리큘럼 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumQueryV2Controller.java:28` |
| 87 | curriculum | CURRICULUM-102 | `/api/v2/curriculums/progress/me` | GET | 내 커리큘럼 진행 상황 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumQueryV2Controller.java:55` |
| 88 | curriculum | ORIGINAL-WORKBOOK-001 | `/api/v2/curriculums/original-workbooks` | POST | 중앙파트장용: 원본 워크북 추가 (READY 상태) | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:30` |
| 89 | curriculum | ORIGINAL-WORKBOOK-002 | `/api/v2/curriculums/original-workbooks/draft` | POST | 중앙파트장용: 원본 워크북 임시저장 (DRAFT 상태) | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:51` |
| 90 | curriculum | ORIGINAL-WORKBOOK-003 | `/api/v2/curriculums/original-workbooks/{originalWorkbookId}` | PATCH | 중앙파트장용: 원본 워크북 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:74` |
| 91 | curriculum | ORIGINAL-WORKBOOK-004 | `/api/v2/curriculums/original-workbooks/{originalWorkbookId}` | DELETE | 중앙파트장용: 원본 워크북 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:95` |
| 92 | curriculum | ORIGINAL-WORKBOOK-005 | `/api/v2/curriculums/original-workbooks/status` | PATCH | 중앙파트장용: 원본 워크북 상태 일괄 변경 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:114` |
| 93 | curriculum | ORIGINAL-WORKBOOK-MISSION-001 | `/api/v2/curriculums/original-workbooks/missions` | POST | 중앙파트장용: 원본 워크북에 미션 추가 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookMissionCommandV2Controller.java:25` |
| 94 | curriculum | ORIGINAL-WORKBOOK-MISSION-002 | `/api/v2/curriculums/original-workbooks/missions/{originalWorkbookMissionId}` | PATCH | 중앙파트장용: 원본 워크북의 미션 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookMissionCommandV2Controller.java:42` |
| 95 | curriculum | ORIGINAL-WORKBOOK-MISSION-003 | `/api/v2/curriculums/original-workbooks/missions/{originalWorkbookMissionId}` | DELETE | 중앙파트장용: 원본 워크북의 미션 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookMissionCommandV2Controller.java:61` |
| 96 | curriculum | WORKBOOK-101 | `/api/v2/curriculums/original-workbooks/{originalWorkbookId}` | GET | OriginalWorkbook 상세 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/WorkbookQueryV2Controller.java:25` |
| 97 | curriculum | WORKBOOK-102 | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}` | GET | ChallengerWorkbook 상세 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/WorkbookQueryV2Controller.java:38` |
| 98 | curriculum | WORKBOOK-103 | `/api/v2/curriculums/weekly-best-workbooks` | GET | 베스트 워크북 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/WorkbookQueryV2Controller.java:54` |

## figma

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 99 | figma | FIGMA-001 | `/api/v1/admin/figma/oauth` | GET | Figma OAuth authorize URL 발급 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaOAuthController.java:32` |
| 100 | figma | FIGMA-002 | `/api/v1/admin/figma/oauth/callback` | GET | Figma OAuth 콜백 처리 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaOAuthController.java:47` |
| 101 | figma | FIGMA-003 | `/api/v1/admin/figma/watched-files` | POST | 폴링 대상 파일 등록 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:37` |
| 102 | figma | FIGMA-004 | `/api/v1/admin/figma/watched-files/{watchedFileId}` | DELETE | 폴링 대상 파일 비활성화 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:47` |
| 103 | figma | FIGMA-005 | `/api/v1/admin/figma/watched-files/{watchedFileId}/enable` | POST | 폴링 대상 파일 활성화 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:54` |
| 104 | figma | FIGMA-006 | `/api/v1/admin/figma/sync` | POST | 활성 파일 전체 즉시 동기화 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java:52` |
| 105 | figma | FIGMA-007 | `/api/v1/admin/figma/sync/watched-files/{watchedFileId}` | POST | 특정 파일 즉시 동기화 (enabled 무관) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java:68` |
| 106 | figma | FIGMA-008 | `/api/v1/admin/figma/watched-files` | GET | 폴링 대상 파일 목록 조회 (enabled 필터) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:61` |
| 107 | figma | FIGMA-009 | `/api/v1/admin/figma/watched-files/{watchedFileId}` | GET | 폴링 대상 파일 단건 조회 (sync 상태 포함) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:72` |
| 108 | figma | FIGMA-010 | `/api/v1/admin/figma/preview` | GET | 특정 시간대 미리보기 (Discord 발송 X, dispatch / cursor 비변경) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java:100` |
| 109 | figma | FIGMA-011 | `/api/v1/admin/figma/routing-domains` | POST | 라우팅 도메인 등록 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:46` |
| 110 | figma | FIGMA-012 | `/api/v1/admin/figma/routing-domains/{domainId}` | DELETE | 라우팅 도메인 삭제 (mention 도 cascade) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:67` |
| 111 | figma | FIGMA-013 | `/api/v1/admin/figma/routing-domains/{domainId}/mentions` | POST | 라우팅 도메인에 담당자 mention 추가 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:75` |
| 112 | figma | FIGMA-014 | `/api/v1/admin/figma/routing-domains/mentions/{mentionId}` | DELETE | 담당자 mention 삭제 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:97` |
| 113 | figma | FIGMA-015 | `/api/v1/admin/figma/digest` | POST | 특정 시간대 catch-up | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java:82` |
| 114 | figma | FIGMA-016 | `/api/v1/admin/figma/routing-domains` | GET | 라우팅 도메인 목록 조회 (mention 본문 미포함) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:105` |
| 115 | figma | FIGMA-017 | `/api/v1/admin/figma/routing-domains/{domainId}` | GET | 라우팅 도메인 단건 조회 (mention 포함) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:114` |
| 116 | figma | FIGMA-018 | `/api/v1/admin/figma/routing-domains/{domainId}/mentions` | GET | 라우팅 도메인의 담당자 mention 목록 조회 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:121` |
| 117 | figma | FIGMA-019 | `/api/v1/admin/figma/routing-domains/{domainId}` | PATCH | 라우팅 도메인 수정 (설명 · webhook URL · fallback) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:56` |
| 118 | figma | FIGMA-020 | `/api/v1/admin/figma/routing-domains/mentions/{mentionId}` | PATCH | 담당자 mention 수정 (Discord ID · 라벨) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:86` |

## global

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 119 | global | <미지정> | `/error` | REQUEST | <요약 없음> | X | `src/main/java/com/umc/product/global/exception/CustomErrorController.java:27` |

## maintenance

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 120 | maintenance | MAINT-001 | `/api/v1/admin/maintenance` | POST | 점검 윈도우 생성 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/AdminMaintenanceController.java:36` |
| 121 | maintenance | MAINT-002 | `/api/v1/admin/maintenance/{windowId}/end` | PATCH | 점검 윈도우 강제 종료 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/AdminMaintenanceController.java:50` |
| 122 | maintenance | MAINT-003 | `/api/v1/admin/maintenance` | GET | 점검 윈도우 전체 목록 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/AdminMaintenanceController.java:64` |
| 123 | maintenance | MAINT-004 | `/api/v1/admin/maintenance/{windowId}` | GET | 점검 윈도우 단건 조회 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/AdminMaintenanceController.java:76` |
| 124 | maintenance | SYSTEM-001 | `/api/v1/system/status` | GET | 시스템 점검 상태 조회 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/SystemStatusController.java:23` |

## member

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 125 | member | MEMBER-001 | `/api/v1/member` | PATCH | 내 회원 정보 수정 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:128` |
| 126 | member | MEMBER-002 | `/api/v1/member/profile/links` | PATCH | 내 회원 프로필 링크 수정 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:142` |
| 127 | member | MEMBER-003 | `/api/v1/member` | DELETE | 회원 탈퇴 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:156` |
| 128 | member | MEMBER-004 | `/api/v1/member/{memberId}` | DELETE | 관리자 권한으로 회원 게정 삭제 (Hard Delete) | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:169` |
| 129 | member | MEMBER-101 | `/api/v1/member/profile/{memberId}` | GET | memberId로 회원 정보 조회 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberQueryController.java:32` |
| 130 | member | MEMBER-102 | `/api/v1/member/me` | GET | 내 프로필 조회 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberQueryController.java:44` |
| 131 | member | MEMBER-103 | `/api/v1/member/search` | GET | 회원 검색 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberQueryController.java:50` |
| 132 | member | MEMBER-201 | `/api/v2/member/me` | GET | 내 종합 정보 조회 | X | `src/main/java/com/umc/product/member/adapter/in/web/v2/MemberQueryV2Controller.java:39` |
| 133 | member | MEMBER-202 | `/api/v2/member/search` | GET | 회원 검색 v2 | X | `src/main/java/com/umc/product/member/adapter/in/web/v2/MemberQueryV2Controller.java:62` |
| 134 | member | REGISTER-001 | `/api/v1/member/register` | POST | OAuth 회원가입 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:59` |
| 135 | member | REGISTER-003 | `/api/v1/member/register/email` | POST | 이메일/PW 이용 회원가입 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:101` |

## notice

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 136 | notice | NOTICE-001 | `/api/v1/notices` | GET | 공지사항 전체 조회 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:31` |
| 137 | notice | NOTICE-002 | `/api/v1/notices/search` | GET | 공지사항 검색 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:79` |
| 138 | notice | NOTICE-003 | `/api/v1/notices/{noticeId}` | GET | 공지사항 상세 조회 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:110` |
| 139 | notice | NOTICE-004 | `/api/v1/notices/{noticeId}/read-statics` | GET | 공지사항 읽음 통계 조회 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:134` |
| 140 | notice | NOTICE-005 | `/api/v1/notices/{noticeId}/read-status` | GET | 공지사항 읽음 현황 상세 조회 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:143` |
| 141 | notice | NOTICE-101 | `/api/v1/notices/{noticeId}/images` | POST | 공지사항 이미지 추가 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:23` |
| 142 | notice | NOTICE-102 | `/api/v1/notices/{noticeId}/links` | POST | 첫 공지 생성 시 공지사항 링크를 추가하는 API입니다. | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:36` |
| 143 | notice | NOTICE-103 | `/api/v1/notices/{noticeId}/votes` | POST | 공지사항 투표 추가 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:48` |
| 144 | notice | NOTICE-104 | `/api/v1/notices/{noticeId}/images` | PATCH | 공지사항 이미지 전체 수정 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:61` |
| 145 | notice | NOTICE-105 | `/api/v1/notices/{noticeId}/links` | PATCH | 공지사항 링크 전체 수정 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:74` |
| 146 | notice | NOTICE-106 | `/api/v1/notices/{noticeId}/vote` | DELETE | 공지사항 투표 삭제 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:87` |
| 147 | notice | NOTICE-201 | `/api/v1/notices` | POST | 공지사항 생성 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:21` |
| 148 | notice | NOTICE-202 | `/api/v1/notices/{noticeId}` | DELETE | 공지사항 삭제 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:71` |
| 149 | notice | NOTICE-203 | `/api/v1/notices/{noticeId}` | PATCH | 공지사항 수정 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:93` |
| 150 | notice | NOTICE-204 | `/api/v1/notices/{noticeId}/reminders` | POST | 공지사항 리마인더 발송 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:122` |
| 151 | notice | NOTICE-205 | `/api/v1/notices/{noticeId}/read` | POST | 공지사항 읽음 처리 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:152` |
| 152 | notice | NOTICE-VOTE-001 | `/api/v1/notices/{noticeId}/votes/responses` | POST | 공지사항 투표 응답 제출 | X | `src/main/java/com/umc/product/notice/adapter/in/web/NoticeVoteResponseController.java:30` |
| 153 | notice | NOTICE-VOTE-002 | `/api/v1/notices/{noticeId}/votes/responses` | PUT | 공지사항 투표 응답 수정/취소 | X | `src/main/java/com/umc/product/notice/adapter/in/web/NoticeVoteResponseController.java:50` |

## notification

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 154 | notification | FCM-001 | `/api/v1/notification/fcm/token` | PUT | FCM 토큰 등록 | X | `src/main/java/com/umc/product/notification/adapter/in/web/swagger/FcmControllerApi.java:15` |
| 155 | notification | FCM-002 | `/api/v1/notification/fcm/topics/legacy` | DELETE | Legacy 토픽 구독 해제 | X | `src/main/java/com/umc/product/notification/adapter/in/web/swagger/FcmControllerApi.java:30` |

## organization

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 156 | organization | <미지정> | `/api/v1/gisu/{gisuId}` | GET | <요약 없음> | X | `src/main/java/com/umc/product/organization/adapter/in/web/GisuQueryController.java:28` |
| 157 | organization | <미지정> | `/api/v1/schools/gisu/{gisuId}` | GET | <요약 없음> | X | `src/main/java/com/umc/product/organization/adapter/in/web/SchoolQueryController.java:36` |
| 158 | organization | CHAPTER-001 | `/api/v1/chapters` | POST | 지부 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminChapterControllerApi.java:16` |
| 159 | organization | CHAPTER-002 | `/api/v1/chapters/bulk` | POST | 지부 일괄 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminChapterControllerApi.java:25` |
| 160 | organization | CHAPTER-003 | `/api/v1/chapters/{chapterId}` | DELETE | 지부 삭제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminChapterControllerApi.java:28` |
| 161 | organization | CHAPTER-101 | `/api/v1/chapters` | GET | 지부 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/ChapterQueryControllerApi.java:18` |
| 162 | organization | CHAPTER-102 | `/api/v1/chapters/with-schools` | GET | 기수별 지부 및 소속 학교 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/ChapterQueryControllerApi.java:29` |
| 163 | organization | CHAPTER-103 | `/api/v1/chapters/{chapterId}` | GET | 지부 ID로 지부 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/ChapterQueryControllerApi.java:41` |
| 164 | organization | GISU-001 | `/api/v1/gisu` | POST | 기수 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuControllerApi.java:13` |
| 165 | organization | GISU-002 | `/api/v1/gisu/{gisuId}` | DELETE | 기수 삭제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuControllerApi.java:20` |
| 166 | organization | GISU-003 | `/api/v1/gisu/{gisuId}/active` | POST | 활성 기수 변경 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuControllerApi.java:28` |
| 167 | organization | GISU-101 | `/api/v1/gisu` | GET | 기수 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuQueryControllerApi.java:25` |
| 168 | organization | GISU-102 | `/api/v1/gisu/all` | GET | 기수 전체 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuQueryControllerApi.java:35` |
| 169 | organization | GISU-103 | `/api/v1/gisu/active` | GET | 활성화된 기수 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuQueryControllerApi.java:46` |
| 170 | organization | SCHOOL-001 | `/api/v1/schools` | POST | 학교 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:17` |
| 171 | organization | SCHOOL-002 | `/api/v1/schools/{schoolId}` | PATCH | 학교 수정 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:25` |
| 172 | organization | SCHOOL-003 | `/api/v1/schools` | DELETE | 학교 삭제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:35` |
| 173 | organization | SCHOOL-004 | `/api/v1/schools/{schoolId}/assign` | PATCH | 학교 지부 배정 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:42` |
| 174 | organization | SCHOOL-005 | `/api/v1/schools/{schoolId}/unassign` | PATCH | 학교 지부 배정 해제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:53` |
| 175 | organization | SCHOOL-101 | `/api/v1/schools/all` | GET | 학교 전체 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/SchoolQueryControllerApi.java:18` |
| 176 | organization | SCHOOL-102 | `/api/v1/schools/{schoolId}` | GET | 학교 상세 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/SchoolQueryControllerApi.java:28` |
| 177 | organization | SCHOOL-103 | `/api/v1/schools/unassigned` | GET | 배정 대기 중인 학교 목록 조회 | O | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/SchoolQueryControllerApi.java:41` |
| 178 | organization | SCHOOL-104 | `/api/v1/schools/link/{schoolId}` | GET | 학교 링크 조회 | O | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/SchoolQueryControllerApi.java:53` |
| 179 | organization | STUDY-GROUP-001 | `/api/v1/study-groups` | POST | 스터디 그룹 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:15` |
| 180 | organization | STUDY-GROUP-002 | `/api/v1/study-groups/{studyGroupId}` | PATCH | 스터디 그룹 수정 (이름만 가능) | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:29` |
| 181 | organization | STUDY-GROUP-003 | `/api/v1/study-groups/{studyGroupId}/members/{memberId}` | PATCH | 스터디 그룹에 스터디원 추가 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:38` |
| 182 | organization | STUDY-GROUP-004 | `/api/v1/study-groups/{studyGroupId}/mentors/{mentorId}` | PATCH | 스터디 그룹에 담당 파트장 추가 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:44` |
| 183 | organization | STUDY-GROUP-005 | `/api/v1/study-groups/{studyGroupId}/members/{memberId}` | DELETE | 스터디 그룹에 스터디원 제거 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:50` |
| 184 | organization | STUDY-GROUP-006 | `/api/v1/study-groups/{studyGroupId}/mentors/{mentorId}` | DELETE | 스터디 그룹에 담당 파트장 제거 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:56` |
| 185 | organization | STUDY-GROUP-007 | `/api/v1/study-groups/{studyGroupId}` | DELETE | 스터디 그룹 삭제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:62` |
| 186 | organization | STUDY-GROUP-101 | `/api/v1/study-groups/managed` | GET | 내가 관리하는 스터디 그룹 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupQueryControllerApi.java:34` |
| 187 | organization | STUDY-GROUP-102 | `/api/v1/study-groups/{studyGroupId}` | GET | 스터디 그룹 정보 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupQueryControllerApi.java:43` |
| 188 | organization | STUDY-GROUP-SCHEDULE-001 | `/api/v1/study-groups/schedules` | POST | 스터디 그룹 일정 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupScheduleControllerApi.java:12` |

## project

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 189 | project | APPLY-001 | `/api/v1/projects/{projectId}/applications` | POST | 챌린저 지원서 Draft 생성 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:47` |
| 190 | project | APPLY-002 | `/api/v1/projects/{projectId}/applications/me` | PUT | 챌린저 지원서 임시저장 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:70` |
| 191 | project | APPLY-003 | `/api/v1/projects/{projectId}/applications/me/submit` | POST | 챌린저 지원서 최종 제출 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:93` |
| 192 | project | APPLY-004 | `/api/v1/projects/me/applications` | GET | 본인 지원 내역 목록 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationQueryController.java:37` |
| 193 | project | APPLY-005 | `/api/v1/projects/{projectId}/applications/{applicationId}` | DELETE | 챌린저 지원서 철회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:147` |
| 194 | project | APPLY-101 | `/api/v1/projects/{projectId}/applications` | GET | PM/운영진 단일 프로젝트 지원자 목록 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationQueryController.java:76` |
| 195 | project | APPLY-102 | `/api/v1/projects/{projectId}/applications/{applicationId}` | GET | 지원서 단건 상세 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationQueryController.java:127` |
| 196 | project | APPLY-103 | `/api/v1/projects/{projectId}/applications/{applicationId}/decision` | PATCH | 지원서 합/불 결정 (단일 PATCH) | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:118` |
| 197 | project | PROJECT-001 | `/api/v1/projects` | GET | 프로젝트 목록 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:43` |
| 198 | project | PROJECT-002 | `/api/v1/projects/{projectId}` | GET | 프로젝트 상세 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:62` |
| 199 | project | PROJECT-003 | `/api/v1/projects/{projectId}/members` | GET | 프로젝트 팀원 구성 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:80` |
| 200 | project | PROJECT-004 | `/api/v1/projects/members` | GET | 프로젝트 팀원 구성 일괄 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:98` |
| 201 | project | PROJECT-004 | `/api/v1/projects/{projectId}/members` | POST | 프로젝트 팀원 추가 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:136` |
| 202 | project | PROJECT-005 | `/api/v1/projects/{projectId}/members/{memberId}` | DELETE | 프로젝트 팀원 제거 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:198` |
| 203 | project | PROJECT-006 | `/api/v1/projects/me/managed` | GET | 내가 관리하는 프로젝트 목록 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:115` |
| 204 | project | PROJECT-101 | `/api/v1/projects` | POST | 프로젝트 Draft 생성 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:56` |
| 205 | project | PROJECT-102 | `/api/v1/projects/{projectId}` | PATCH | 프로젝트 기본정보 수정 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:75` |
| 206 | project | PROJECT-103 | `/api/v1/projects/me/draft` | GET | 내 Draft 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:134` |
| 207 | project | PROJECT-104 | `/api/v1/projects/{projectId}/transfer-ownership` | POST | 프로젝트 소유권 양도 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:116` |
| 208 | project | PROJECT-105 | `/api/v1/projects/{projectId}/part-quotas` | PUT | 파트별 정원 일괄 갱신 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:178` |
| 209 | project | PROJECT-106 | `/api/v1/projects/{projectId}/application-form` | PUT | 지원 폼 저장 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationFormController.java:35` |
| 210 | project | PROJECT-106-GET | `/api/v1/projects/{projectId}/application-form` | GET | 지원 폼 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationFormController.java:56` |
| 211 | project | PROJECT-107 | `/api/v1/projects/{projectId}/submit` | POST | 프로젝트 제출 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:96` |
| 212 | project | PROJECT-108 | `/api/v1/projects/{projectId}/publish` | POST | 프로젝트 공개 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:156` |
| 213 | project | PROJECT-MATCHING-001 | `/api/v1/project/matching-rounds` | GET | 매칭 차수 목록 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:44` |
| 214 | project | PROJECT-MATCHING-101 | `/api/v1/project/matching-rounds` | POST | 매칭 차수 생성 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:66` |
| 215 | project | PROJECT-MATCHING-102 | `/api/v1/project/matching-rounds/{matchingRoundId}` | PATCH | 매칭 차수 수정 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:87` |
| 216 | project | PROJECT-MATCHING-103 | `/api/v1/project/matching-rounds/{matchingRoundId}` | DELETE | 매칭 차수 삭제 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:110` |
| 217 | project | PROJECT-MATCHING-201 | `/api/v1/project/matching-rounds/{matchingRoundId}/auto-decide` | POST | 매칭 차수 자동 선발 실행 (운영진 수동 트리거) | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:127` |
| 218 | project | PROJECT-STAT-001 | `/api/v1/projects/{projectId}/statistics` | GET | 단건 프로젝트 지원/매칭 현황 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectStatisticsQueryController.java:30` |
| 219 | project | PROJECT-STAT-002 | `/api/v1/projects/statistics` | GET | 지부 전체 프로젝트 지원/매칭 현황 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectStatisticsQueryController.java:56` |

## schedule

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 220 | schedule | SCHEDULE-C001 | `/api/v2/schedules` | POST | 일정 생성 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:59` |
| 221 | schedule | SCHEDULE-C002 | `/api/v2/schedules/{scheduleId}` | PATCH | 일정 수정 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:125` |
| 222 | schedule | SCHEDULE-C003 | `/api/v2/schedules/{scheduleId}/attendances/request` | POST | 출석 요청하기 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:263` |
| 223 | schedule | SCHEDULE-C004 | `/api/v2/schedules/{scheduleId}/attendances/excuse` | POST | 출석 요청이 불가능한 경우, 사유 제출하기 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:312` |
| 224 | schedule | SCHEDULE-C005 | `/api/v2/schedules/{scheduleId}/attendances/decide` | POST | [운영진용] 출석 요청 승인/거절 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:359` |
| 225 | schedule | SCHEDULE-C006 | `/api/v2/schedules/{scheduleId}` | DELETE | 일정 삭제 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:186` |
| 226 | schedule | SCHEDULE-C007 | `/api/v2/schedules/{scheduleId}/force` | DELETE | 일정 강제 삭제 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:227` |
| 227 | schedule | SCHEDULE-Q001 | `/api/v2/schedules/capabilities` | GET | 일정 생성, 수정 관련 권한 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:42` |
| 228 | schedule | SCHEDULE-Q002 | `/api/v2/schedules/me` | GET | 내 일정 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:66` |
| 229 | schedule | SCHEDULE-Q003 | `/api/v2/schedules/{scheduleId}` | GET | 일정 상세 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:103` |
| 230 | schedule | SCHEDULE-Q004 | `/api/v2/schedules/attendance` | GET | [운영진용] 일정들의 출석 현황 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:143` |
| 231 | schedule | SCHEDULE-Q005 | `/api/v2/schedules/{scheduleId}/attendance` | GET | [운영진용] 단일 일정 출석 현황 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:206` |

## storage

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 232 | storage | STORAGE-001 | `/api/v1/storage/prepare-upload` | POST | 파일 업로드를 위한 Signed URL을 생성합니다. | X | `src/main/java/com/umc/product/storage/adapter/in/web/StorageController.java:35` |
| 233 | storage | STORAGE-002 | `/api/v1/storage/{fileId}/confirm` | POST | 파일 업로드 완료 처리 | X | `src/main/java/com/umc/product/storage/adapter/in/web/StorageController.java:57` |
| 234 | storage | STORAGE-003 | `/api/v1/storage/{fileId}` | DELETE | 파일 삭제 | X | `src/main/java/com/umc/product/storage/adapter/in/web/StorageController.java:67` |

## term

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 235 | term | TERM-001 | `/api/v1/terms` | POST | 약관 생성 | X | `src/main/java/com/umc/product/term/adapter/in/web/TermController.java:48` |
| 236 | term | TERM-101 | `/api/v1/terms/type/{termType}` | GET | 약관 유형으로 약관 조회 | X | `src/main/java/com/umc/product/term/adapter/in/web/TermController.java:35` |
| 237 | term | TERM-102 | `/api/v1/terms/{termsId}` | GET | 약관 ID로 약관 조회 | X | `src/main/java/com/umc/product/term/adapter/in/web/TermController.java:42` |

## test

| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 238 | test | <미지정> | `/test/email/send-test` | POST | <요약 없음> | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:82` |
| 239 | test | <미지정> | `/test/log-test` | GET | <요약 없음> | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:209` |
| 240 | test | SEED-001 | `/test/seed/members` | POST | 더미 멤버 시딩 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:53` |
| 241 | test | SEED-002 | `/test/seed/challengers` | POST | 챌린저 분포 시딩 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:69` |
| 242 | test | SEED-003 | `/test/seed/projects` | POST | 프로젝트 시딩 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:84` |
| 243 | test | SEED-004 | `/test/seed/curriculum` | POST | Curriculum 시딩 (Curriculum · WeeklyCurriculum · OriginalWorkbook · Mission) | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:100` |
| 244 | test | SEED-005 | `/test/seed/notice` | POST | Notice 시딩 (지부 · 학교 · 파트 분포) | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:115` |
| 245 | test | TEST-001 | `/test/file/{fileId}` | GET | [개발용] 파일 ID를 기반으로 접근 가능한 URL을 조회합니다. | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:62` |
| 246 | test | TEST-002 | `/test/fcm/test-send` | POST | FCM 푸시 알림 테스트 전송 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:74` |
| 247 | test | TEST-003 | `/test/webhook/aop-test` | GET | AOP로 전송하는 알람 테스트 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:101` |
| 248 | test | TEST-004 | `/test/webhook/alarm` | POST | 웹훅 알람 전송 테스트 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:114` |
| 249 | test | TEST-005 | `/test/webhook/alarm/buffer` | POST | 웹훅 알람 버퍼 전송 테스트 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:130` |
| 250 | test | TEST-006 | `/test/apple-client-secret` | GET | Apple Client Secret 생성 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:151` |
| 251 | test | TEST-007 | `/test/token/access` | GET | AccessToken 발급 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:157` |
| 252 | test | TEST-008 | `/test/token/refresh` | GET | RefreshToken 발급 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:170` |
| 253 | test | TEST-009 | `/test/token/email` | GET | EmailVerificationToken 발급 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:177` |
| 254 | test | TEST-010 | `/test/token/oauth` | GET | oAuthVerificationToken 발급 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:187` |
| 255 | test | TEST-011 | `/test/health-check` | GET | 헬스 체크 API | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:195` |
| 256 | test | TEST-012 | `/test/check-authenticated` | GET | 인증된 사용자인지 여부를 확인합니다. | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:202` |

