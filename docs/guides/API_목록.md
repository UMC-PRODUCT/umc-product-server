# API Catalog

운영 중인 API의 ID, 엔드포인트, 메서드, 역할, deprecated 상태를 도메인별로 정리합니다.

> 소스 기준: `@Operation(operationId = "...")`를 우선 사용하고, 없으면 `summary`의 `[XXX-000]` prefix를 API ID로 읽습니다. 갱신: `./gradlew generateDocumentationCatalogs`

## analytics

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 1 | analytics | DASHBOARD-001 | GET | `/api/v1/admin/dashboard/summary` | 운영진 대시보드 요약 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:44` |
| 2 | analytics | DASHBOARD-002 | GET | `/api/v1/admin/dashboard/action-queue` | 운영진 대시보드 액션 큐 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:56` |
| 3 | analytics | DASHBOARD-003 | GET | `/api/v1/admin/dashboard/risk-challengers` | 운영진 대시보드 위험군 챌린저 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:91` |
| 4 | analytics | DASHBOARD-004 | GET | `/api/v1/admin/dashboard/context` | 운영진 대시보드 권한 컨텍스트 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:68` |
| 5 | analytics | DASHBOARD-005 | GET | `/api/v1/admin/dashboard/operations` | 운영 현황 집계 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:79` |
| 6 | analytics | DASHBOARD-100 | GET | `/api/v1/admin/schools/summary` | 학교별 현황 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminSchoolAnalyticsController.java:30` |

## audit

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 7 | audit | AUDIT-001 | GET | `/api/v1/admin/audit-logs` | 감사 로그 검색 | X | `src/main/java/com/umc/product/audit/adapter/in/web/AuditLogController.java:33` |

## authentication

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 8 | authentication | CREDENTIAL-002 | POST | `/api/v1/auth/credentials` | 비밀번호 자격증명 최초 등록 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:47` |
| 9 | authentication | CREDENTIAL-003 | PATCH | `/api/v1/auth/password` | 비밀번호 변경 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:60` |
| 10 | authentication | CREDENTIAL-005 | GET | `/api/v1/auth/email/availability` | 이메일 사용 가능 여부 조회 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:89` |
| 11 | authentication | CREDENTIAL-007 | PATCH | `/api/v1/auth/password/reset` | 비밀번호 초기화 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:73` |
| 12 | authentication | EMAIL-001 | POST | `/api/v1/auth/email-verification/code` | 6자리 인증코드로 이메일 인증 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/EmailAuthenticationController.java:34` |
| 13 | authentication | EMAIL-002 | POST | `/api/v1/auth/email-verification` | 이메일 인증 코드 발송 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/EmailAuthenticationController.java:60` |
| 14 | authentication | EMAIL-003 | POST | `/api/v1/auth/email-verification/resend` | 이메일 인증 코드 재전송 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/EmailAuthenticationController.java:85` |
| 15 | authentication | LOGIN-001 | POST | `/api/v1/auth/login/google` | Google 로그인 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/swagger/AuthenticationControllerInterface.java:14` |
| 16 | authentication | LOGIN-005 | POST | `/api/v1/auth/login/kakao` | Kakao 로그인 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/swagger/AuthenticationControllerInterface.java:32` |
| 17 | authentication | LOGIN-006 | POST | `/api/v1/auth/login/kakao/code` | Kakao 로그인 (Authorization Code 흐름) | X | `src/main/java/com/umc/product/authentication/adapter/in/web/swagger/AuthenticationControllerInterface.java:50` |
| 18 | authentication | LOGIN-010 | POST | `/api/v1/auth/login/apple` | Apple 로그인 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/swagger/AuthenticationControllerInterface.java:68` |
| 19 | authentication | LOGIN-011 | POST | `/api/v1/auth/login/email` | 이메일/PW 로그인 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:100` |
| 20 | authentication | OAUTH-001 | POST | `/api/v1/member-oauth` | 로그인용 OAuth 수단 추가 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/MemberOAuthController.java:37` |
| 21 | authentication | OAUTH-002 | DELETE | `/api/v1/member-oauth/{memberOAuthId}` | 로그인용 OAuth 수단 제거 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/MemberOAuthController.java:58` |
| 22 | authentication | OAUTH-101 | GET | `/api/v1/member-oauth/me` | 현재 회원 계정과 연동된 OAuth 정보 조회 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/MemberOAuthController.java:81` |
| 23 | authentication | TOKEN-001 | POST | `/api/v1/auth/token/renew` | AccessToken 재발급 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/TokenAuthenticationController.java:23` |

## authorization

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 24 | authorization | PERMISSION-001 | GET | `/api/v1/authorization/resource-permission` | 리소스 권한 조회 | X | `src/main/java/com/umc/product/authorization/adapter/in/web/ResourcePermissionController.java:35` |
| 25 | authorization | PERMISSION-002 | POST | `/api/v1/authorization/resource-permissions/batch` | 리소스 권한 배치 조회 | X | `src/main/java/com/umc/product/authorization/adapter/in/web/ResourcePermissionController.java:57` |
| 26 | authorization | STAFF-001 | POST | `/api/v1/authorization/challenger-role` | 운영진 기록 생성 | X | `src/main/java/com/umc/product/authorization/adapter/in/web/ChallengerRoleController.java:45` |
| 27 | authorization | STAFF-002 | DELETE | `/api/v1/authorization/challenger-role/{challengerRoleId}` | 운영진 기록 삭제 | X | `src/main/java/com/umc/product/authorization/adapter/in/web/ChallengerRoleController.java:77` |
| 28 | authorization | STAFF-101 | GET | `/api/v1/authorization/challenger-role/{challengerRoleId}` | 운영진 기록 조회 | O | `src/main/java/com/umc/product/authorization/adapter/in/web/ChallengerRoleController.java:61` |

## challenger

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 29 | challenger | CHALLENGER-001 | POST | `/api/v1/challenger` | 챌린저 생성 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:40` |
| 30 | challenger | CHALLENGER-002 | POST | `/api/v1/challenger/batch` | 챌린저 batch 생성 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:52` |
| 31 | challenger | CHALLENGER-003 | POST | `/api/v1/challenger/{challengerId}/deactivate` | 챌린저 비활성화 (제명/탈부 처리) | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:70` |
| 32 | challenger | CHALLENGER-004 | PATCH | `/api/v1/challenger/{challengerId}/part` | 챌린저 파트 변경 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:83` |
| 33 | challenger | CHALLENGER-005 | DELETE | `/api/v1/challenger/{challengerId}` | [주의] 챌린저 삭제 (Hard Delete) | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:99` |
| 34 | challenger | CHALLENGER-101 | GET | `/api/v1/challenger/{challengerId}` | 챌린저 정보 조회 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerQueryController.java:44` |
| 35 | challenger | CHALLENGER-102 | GET | `/api/v1/challenger/search/cursor` | 챌린저 검색 (Cursor 기반) | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerSearchController.java:29` |
| 36 | challenger | CHALLENGER-103 | GET | `/api/v1/challenger/search/offset` | 챌린저 검색 (Offset 기반) | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerSearchController.java:52` |
| 37 | challenger | CHALLENGER-104 | GET | `/api/v1/challenger/search/global` | deprecated: 챌린저 전체 검색 (Cursor 기반, 일정 생성용) | O | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerSearchController.java:66` |
| 38 | challenger | CHALLENGER-201 | GET | `/api/v2/challenger/search` | 챌린저 검색 v2 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/v2/ChallengerSearchV2Controller.java:31` |
| 39 | challenger | CHALLENGER-RECORD-001 | POST | `/api/v1/challenger-record/member` | 6자리 코드를 이용해서 회원(계정)에 챌린저 기록 추가 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:36` |
| 40 | challenger | CHALLENGER-RECORD-002 | POST | `/api/v1/challenger-record` | [ADMIN] 과거 챌린저 기록을 위한 코드 생성 기능 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:89` |
| 41 | challenger | CHALLENGER-RECORD-003 | POST | `/api/v1/challenger-record/bulk` | [ADMIN] 챌린저 기록용 코드 벌크 추가 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:116` |
| 42 | challenger | CHALLENGER-RECORD-101 | GET | `/api/v1/challenger-record/code/{code}` | 코드로 ChallengerRecord 조회 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:65` |
| 43 | challenger | CHALLENGER-RECORD-102 | GET | `/api/v1/challenger-record/id/{id}` | ID로 ChallengerRecord 조회 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:77` |
| 44 | challenger | POINT-001 | POST | `/api/v1/challenger/{challengerId}/points` | 챌린저 상벌점 부여 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandController.java:38` |
| 45 | challenger | POINT-002 | PATCH | `/api/v1/challenger/points/{challengerPointId}` | 챌린저 상벌점 사유 수정 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandController.java:55` |
| 46 | challenger | POINT-003 | DELETE | `/api/v1/challenger/points/{challengerPointId}` | 챌린저 상벌점 삭제 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandController.java:70` |

## community

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 47 | community | COMMENT-001 | POST | `/api/v1/posts/{postId}/comments` | 댓글 작성 | X | `src/main/java/com/umc/product/community/adapter/in/web/CommentController.java:42` |
| 48 | community | COMMENT-002 | DELETE | `/api/v1/posts/{postId}/comments/{commentId}` | 댓글 삭제 | X | `src/main/java/com/umc/product/community/adapter/in/web/CommentController.java:74` |
| 49 | community | COMMENT-003 | POST | `/api/v1/posts/{postId}/comments/{commentId}/like` | 댓글 좋아요 토글 | X | `src/main/java/com/umc/product/community/adapter/in/web/CommentController.java:86` |
| 50 | community | COMMENT-101 | GET | `/api/v1/posts/{postId}/comments` | 댓글 목록 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/CommentController.java:56` |
| 51 | community | POST-001 | POST | `/api/v1/posts` | 일반 게시글 생성 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:55` |
| 52 | community | POST-002 | POST | `/api/v1/posts/lightning` | 번개글 생성 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:66` |
| 53 | community | POST-003 | PATCH | `/api/v1/posts/{postId}` | 일반 게시글 수정 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:83` |
| 54 | community | POST-004 | PATCH | `/api/v1/posts/{postId}/lightning` | 번개글 수정 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:98` |
| 55 | community | POST-005 | DELETE | `/api/v1/posts/{postId}` | 게시글 삭제 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:113` |
| 56 | community | POST-006 | POST | `/api/v1/posts/{postId}/like` | 게시글 좋아요 토글 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:121` |
| 57 | community | POST-007 | POST | `/api/v1/posts/{postId}/scrap` | 게시글 스크랩 토글 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:132` |
| 58 | community | POST-101 | GET | `/api/v1/posts/{postId}` | 게시글 상세 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:65` |
| 59 | community | POST-102 | GET | `/api/v1/posts` | 게시글 목록 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:75` |
| 60 | community | POST-103 | GET | `/api/v1/posts/search` | 게시글 검색 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:95` |
| 61 | community | POST-104 | GET | `/api/v1/posts/my` | 내가 쓴 글 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:112` |
| 62 | community | POST-105 | GET | `/api/v1/posts/commented` | 댓글 단 글 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:125` |
| 63 | community | POST-106 | GET | `/api/v1/posts/scrapped` | 스크랩한 글 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:138` |
| 64 | community | REPORT-001 | POST | `/api/v1/posts/{postId}/reports` | 게시글 신고 | X | `src/main/java/com/umc/product/community/adapter/in/web/ReportController.java:32` |
| 65 | community | REPORT-002 | POST | `/api/v1/comments/{commentId}/reports` | 댓글 신고 | X | `src/main/java/com/umc/product/community/adapter/in/web/ReportController.java:44` |
| 66 | community | TROPHY-001 | POST | `/api/v1/trophies` | 베스트 워크북 생성 | X | `src/main/java/com/umc/product/community/adapter/in/web/TrophyController.java:28` |
| 67 | community | TROPHY-101 | GET | `/api/v1/trophies` | 상장 목록 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/TrophyQueryController.java:24` |

## curriculum

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 68 | curriculum | CHALLENGER-WORKBOOK-001 | POST | `/api/v2/curriculums/challenger-workbooks/deploy` | 챌린저용: 특정 원본 워크북 배포 요청 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:21` |
| 69 | curriculum | CHALLENGER-WORKBOOK-002 | PATCH | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}` | 챌린저 워크북 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:47` |
| 70 | curriculum | CHALLENGER-WORKBOOK-003 | DELETE | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}` | 챌린저 워크북 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:66` |
| 71 | curriculum | CHALLENGER-WORKBOOK-004 | POST | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}/excuse` | 회장단용: 특정 워크북 인정 처리 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:84` |
| 72 | curriculum | CHALLENGER-WORKBOOK-005 | POST | `/api/v2/curriculums/challenger-workbooks/weekly-best` | 베스트 워크북 선정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:104` |
| 73 | curriculum | CHALLENGER-WORKBOOK-006 | PATCH | `/api/v2/curriculums/challenger-workbooks/weekly-best/{weeklyBestWorkbookId}` | 베스트 워크북 선정 사유 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:126` |
| 74 | curriculum | CHALLENGER-WORKBOOK-007 | DELETE | `/api/v2/curriculums/challenger-workbooks/weekly-best/{weeklyBestWorkbookId}` | 베스트 워크북 선정 철회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:145` |
| 75 | curriculum | CHALLENGER-WORKBOOK-MISSION-001 | POST | `/api/v2/curriculums/challenger-workbooks/missions` | 챌린저용: 워크북 내 미션 제출 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:25` |
| 76 | curriculum | CHALLENGER-WORKBOOK-MISSION-002 | PATCH | `/api/v2/curriculums/challenger-workbooks/missions/{missionSubmissionId}` | 챌린저용: 제출한 워크북 미션 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:53` |
| 77 | curriculum | CHALLENGER-WORKBOOK-MISSION-003 | DELETE | `/api/v2/curriculums/challenger-workbooks/missions/{missionSubmissionId}` | 챌린저용: 제출한 워크북 미션 철회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:76` |
| 78 | curriculum | CHALLENGER-WORKBOOK-MISSION-004 | POST | `/api/v2/curriculums/challenger-workbooks/missions/feedback` | 운영진용: 제출된 미션에 대한 피드백 작성 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:93` |
| 79 | curriculum | CHALLENGER-WORKBOOK-MISSION-005 | PATCH | `/api/v2/curriculums/challenger-workbooks/missions/feedback/{missionFeedbackId}` | 운영진용: 제출된 미션에 대한 피드백 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:117` |
| 80 | curriculum | CHALLENGER-WORKBOOK-MISSION-006 | DELETE | `/api/v2/curriculums/challenger-workbooks/missions/feedback/{missionFeedbackId}` | 운영진용: 제출된 미션에 대한 피드백 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:136` |
| 81 | curriculum | CURRICULUM-001 | POST | `/api/v2/curriculums` | 커리큘럼 생성 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:30` |
| 82 | curriculum | CURRICULUM-002 | PATCH | `/api/v2/curriculums/{curriculumId}` | 커리큘럼 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:49` |
| 83 | curriculum | CURRICULUM-003 | DELETE | `/api/v2/curriculums/{curriculumId}` | 중앙운영사무국 총괄단용: 커리큘럼 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:73` |
| 84 | curriculum | CURRICULUM-004 | POST | `/api/v2/curriculums/weekly` | 각 커리큘럼에 새로운 주차 생성 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:94` |
| 85 | curriculum | CURRICULUM-005 | PATCH | `/api/v2/curriculums/weekly/{weeklyCurriculumId}` | 주차별 커리큘럼 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:115` |
| 86 | curriculum | CURRICULUM-006 | DELETE | `/api/v2/curriculums/weekly/{weeklyCurriculumId}` | 주차별 커리큘럼 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:140` |
| 87 | curriculum | CURRICULUM-101 | GET | `/api/v2/curriculums/overview` | 특정 기수의 파트별 커리큘럼 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumQueryV2Controller.java:28` |
| 88 | curriculum | CURRICULUM-102 | GET | `/api/v2/curriculums/progress/me` | 내 커리큘럼 진행 상황 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumQueryV2Controller.java:55` |
| 89 | curriculum | ORIGINAL-WORKBOOK-001 | POST | `/api/v2/curriculums/original-workbooks` | 중앙파트장용: 원본 워크북 추가 (READY 상태) | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:30` |
| 90 | curriculum | ORIGINAL-WORKBOOK-002 | POST | `/api/v2/curriculums/original-workbooks/draft` | 중앙파트장용: 원본 워크북 임시저장 (DRAFT 상태) | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:51` |
| 91 | curriculum | ORIGINAL-WORKBOOK-003 | PATCH | `/api/v2/curriculums/original-workbooks/{originalWorkbookId}` | 중앙파트장용: 원본 워크북 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:74` |
| 92 | curriculum | ORIGINAL-WORKBOOK-004 | DELETE | `/api/v2/curriculums/original-workbooks/{originalWorkbookId}` | 중앙파트장용: 원본 워크북 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:95` |
| 93 | curriculum | ORIGINAL-WORKBOOK-005 | PATCH | `/api/v2/curriculums/original-workbooks/status` | 중앙파트장용: 원본 워크북 상태 일괄 변경 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:114` |
| 94 | curriculum | ORIGINAL-WORKBOOK-MISSION-001 | POST | `/api/v2/curriculums/original-workbooks/missions` | 중앙파트장용: 원본 워크북에 미션 추가 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookMissionCommandV2Controller.java:25` |
| 95 | curriculum | ORIGINAL-WORKBOOK-MISSION-002 | PATCH | `/api/v2/curriculums/original-workbooks/missions/{originalWorkbookMissionId}` | 중앙파트장용: 원본 워크북의 미션 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookMissionCommandV2Controller.java:42` |
| 96 | curriculum | ORIGINAL-WORKBOOK-MISSION-003 | DELETE | `/api/v2/curriculums/original-workbooks/missions/{originalWorkbookMissionId}` | 중앙파트장용: 원본 워크북의 미션 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookMissionCommandV2Controller.java:61` |
| 97 | curriculum | WORKBOOK-101 | GET | `/api/v2/curriculums/original-workbooks/{originalWorkbookId}` | OriginalWorkbook 상세 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/WorkbookQueryV2Controller.java:25` |
| 98 | curriculum | WORKBOOK-102 | GET | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}` | ChallengerWorkbook 상세 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/WorkbookQueryV2Controller.java:38` |
| 99 | curriculum | WORKBOOK-103 | GET | `/api/v2/curriculums/weekly-best-workbooks` | 베스트 워크북 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/WorkbookQueryV2Controller.java:54` |

## figma

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 100 | figma | FIGMA-001 | GET | `/api/v1/admin/figma/oauth` | Figma OAuth authorize URL 발급 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaOAuthController.java:32` |
| 101 | figma | FIGMA-002 | GET | `/api/v1/admin/figma/oauth/callback` | Figma OAuth 콜백 처리 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaOAuthController.java:47` |
| 102 | figma | FIGMA-003 | POST | `/api/v1/admin/figma/watched-files` | 폴링 대상 파일 등록 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:37` |
| 103 | figma | FIGMA-004 | DELETE | `/api/v1/admin/figma/watched-files/{watchedFileId}` | 폴링 대상 파일 비활성화 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:47` |
| 104 | figma | FIGMA-005 | POST | `/api/v1/admin/figma/watched-files/{watchedFileId}/enable` | 폴링 대상 파일 활성화 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:54` |
| 105 | figma | FIGMA-006 | POST | `/api/v1/admin/figma/sync` | 활성 파일 전체 즉시 동기화 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java:52` |
| 106 | figma | FIGMA-007 | POST | `/api/v1/admin/figma/sync/watched-files/{watchedFileId}` | 특정 파일 즉시 동기화 (enabled 무관) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java:68` |
| 107 | figma | FIGMA-008 | GET | `/api/v1/admin/figma/watched-files` | 폴링 대상 파일 목록 조회 (enabled 필터) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:61` |
| 108 | figma | FIGMA-009 | GET | `/api/v1/admin/figma/watched-files/{watchedFileId}` | 폴링 대상 파일 단건 조회 (sync 상태 포함) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:72` |
| 109 | figma | FIGMA-010 | GET | `/api/v1/admin/figma/preview` | 특정 시간대 미리보기 (Discord 발송 X, dispatch / cursor 비변경) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java:100` |
| 110 | figma | FIGMA-011 | POST | `/api/v1/admin/figma/routing-domains` | 라우팅 도메인 등록 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:46` |
| 111 | figma | FIGMA-012 | DELETE | `/api/v1/admin/figma/routing-domains/{domainId}` | 라우팅 도메인 삭제 (mention 도 cascade) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:67` |
| 112 | figma | FIGMA-013 | POST | `/api/v1/admin/figma/routing-domains/{domainId}/mentions` | 라우팅 도메인에 담당자 mention 추가 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:75` |
| 113 | figma | FIGMA-014 | DELETE | `/api/v1/admin/figma/routing-domains/mentions/{mentionId}` | 담당자 mention 삭제 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:97` |
| 114 | figma | FIGMA-015 | POST | `/api/v1/admin/figma/digest` | 특정 시간대 catch-up | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java:82` |
| 115 | figma | FIGMA-016 | GET | `/api/v1/admin/figma/routing-domains` | 라우팅 도메인 목록 조회 (mention 본문 미포함) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:105` |
| 116 | figma | FIGMA-017 | GET | `/api/v1/admin/figma/routing-domains/{domainId}` | 라우팅 도메인 단건 조회 (mention 포함) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:114` |
| 117 | figma | FIGMA-018 | GET | `/api/v1/admin/figma/routing-domains/{domainId}/mentions` | 라우팅 도메인의 담당자 mention 목록 조회 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:121` |
| 118 | figma | FIGMA-019 | PATCH | `/api/v1/admin/figma/routing-domains/{domainId}` | 라우팅 도메인 수정 (설명 · webhook URL · fallback) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:56` |
| 119 | figma | FIGMA-020 | PATCH | `/api/v1/admin/figma/routing-domains/mentions/{mentionId}` | 담당자 mention 수정 (Discord ID · 라벨) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:86` |

## global

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 120 | global | <미지정> | REQUEST | `/error` | <요약 없음> | X | `src/main/java/com/umc/product/global/exception/CustomErrorController.java:27` |

## maintenance

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 121 | maintenance | MAINT-001 | POST | `/api/v1/admin/maintenance` | 점검 윈도우 생성 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/AdminMaintenanceController.java:36` |
| 122 | maintenance | MAINT-002 | PATCH | `/api/v1/admin/maintenance/{windowId}/end` | 점검 윈도우 강제 종료 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/AdminMaintenanceController.java:50` |
| 123 | maintenance | MAINT-003 | GET | `/api/v1/admin/maintenance` | 점검 윈도우 전체 목록 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/AdminMaintenanceController.java:64` |
| 124 | maintenance | MAINT-004 | GET | `/api/v1/admin/maintenance/{windowId}` | 점검 윈도우 단건 조회 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/AdminMaintenanceController.java:76` |
| 125 | maintenance | SYSTEM-001 | GET | `/api/v1/system/status` | 시스템 점검 상태 조회 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/SystemStatusController.java:23` |

## member

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 126 | member | MEMBER-001 | PATCH | `/api/v1/member` | 내 회원 정보 수정 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:130` |
| 127 | member | MEMBER-002 | PATCH | `/api/v1/member/profile/links` | 내 회원 프로필 링크 수정 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:144` |
| 128 | member | MEMBER-003 | DELETE | `/api/v1/member` | 회원 탈퇴 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:158` |
| 129 | member | MEMBER-004 | DELETE | `/api/v1/member/{memberId}` | 관리자 권한으로 회원 게정 삭제 (Hard Delete) | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:171` |
| 130 | member | MEMBER-101 | GET | `/api/v1/member/profile/{memberId}` | memberId로 회원 정보 조회 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberQueryController.java:32` |
| 131 | member | MEMBER-102 | GET | `/api/v1/member/me` | 내 프로필 조회 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberQueryController.java:44` |
| 132 | member | MEMBER-103 | GET | `/api/v1/member/search` | 회원 검색 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberQueryController.java:50` |
| 133 | member | MEMBER-201 | GET | `/api/v2/member/me` | 내 종합 정보 조회 | X | `src/main/java/com/umc/product/member/adapter/in/web/v2/MemberQueryV2Controller.java:39` |
| 134 | member | MEMBER-202 | GET | `/api/v2/member/search` | 회원 검색 v2 | X | `src/main/java/com/umc/product/member/adapter/in/web/v2/MemberQueryV2Controller.java:62` |
| 135 | member | REGISTER-001 | POST | `/api/v1/member/register` | OAuth 회원가입 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:61` |
| 136 | member | REGISTER-003 | POST | `/api/v1/member/register/email` | 이메일/PW 이용 회원가입 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:103` |

## notice

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 137 | notice | NOTICE-001 | GET | `/api/v1/notices` | 공지사항 전체 조회 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:31` |
| 138 | notice | NOTICE-002 | GET | `/api/v1/notices/search` | 공지사항 검색 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:79` |
| 139 | notice | NOTICE-003 | GET | `/api/v1/notices/{noticeId}` | 공지사항 상세 조회 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:110` |
| 140 | notice | NOTICE-004 | GET | `/api/v1/notices/{noticeId}/read-statics` | 공지사항 읽음 통계 조회 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:134` |
| 141 | notice | NOTICE-005 | GET | `/api/v1/notices/{noticeId}/read-status` | 공지사항 읽음 현황 상세 조회 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:143` |
| 142 | notice | NOTICE-101 | POST | `/api/v1/notices/{noticeId}/images` | 공지사항 이미지 추가 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:23` |
| 143 | notice | NOTICE-102 | POST | `/api/v1/notices/{noticeId}/links` | 첫 공지 생성 시 공지사항 링크를 추가하는 API입니다. | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:36` |
| 144 | notice | NOTICE-103 | POST | `/api/v1/notices/{noticeId}/votes` | 공지사항 투표 추가 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:48` |
| 145 | notice | NOTICE-104 | PATCH | `/api/v1/notices/{noticeId}/images` | 공지사항 이미지 전체 수정 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:61` |
| 146 | notice | NOTICE-105 | PATCH | `/api/v1/notices/{noticeId}/links` | 공지사항 링크 전체 수정 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:74` |
| 147 | notice | NOTICE-106 | DELETE | `/api/v1/notices/{noticeId}/vote` | 공지사항 투표 삭제 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:87` |
| 148 | notice | NOTICE-201 | POST | `/api/v1/notices` | 공지사항 생성 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:21` |
| 149 | notice | NOTICE-202 | DELETE | `/api/v1/notices/{noticeId}` | 공지사항 삭제 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:71` |
| 150 | notice | NOTICE-203 | PATCH | `/api/v1/notices/{noticeId}` | 공지사항 수정 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:93` |
| 151 | notice | NOTICE-204 | POST | `/api/v1/notices/{noticeId}/reminders` | 공지사항 리마인더 발송 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:122` |
| 152 | notice | NOTICE-205 | POST | `/api/v1/notices/{noticeId}/read` | 공지사항 읽음 처리 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:152` |
| 153 | notice | NOTICE-VOTE-001 | POST | `/api/v1/notices/{noticeId}/votes/responses` | 공지사항 투표 응답 제출 | X | `src/main/java/com/umc/product/notice/adapter/in/web/NoticeVoteResponseController.java:30` |
| 154 | notice | NOTICE-VOTE-002 | PUT | `/api/v1/notices/{noticeId}/votes/responses` | 공지사항 투표 응답 수정/취소 | X | `src/main/java/com/umc/product/notice/adapter/in/web/NoticeVoteResponseController.java:50` |

## notification

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 155 | notification | FCM-001 | PUT | `/api/v1/notification/fcm/token` | FCM 토큰 등록 | X | `src/main/java/com/umc/product/notification/adapter/in/web/swagger/FcmControllerApi.java:15` |
| 156 | notification | FCM-002 | DELETE | `/api/v1/notification/fcm/topics/legacy` | Legacy 토픽 구독 해제 | X | `src/main/java/com/umc/product/notification/adapter/in/web/swagger/FcmControllerApi.java:30` |

## organization

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 157 | organization | <미지정> | GET | `/api/v1/gisu/{gisuId}` | <요약 없음> | X | `src/main/java/com/umc/product/organization/adapter/in/web/GisuQueryController.java:28` |
| 158 | organization | <미지정> | GET | `/api/v1/schools/gisu/{gisuId}` | <요약 없음> | X | `src/main/java/com/umc/product/organization/adapter/in/web/SchoolQueryController.java:36` |
| 159 | organization | CHAPTER-001 | POST | `/api/v1/chapters` | 지부 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminChapterControllerApi.java:16` |
| 160 | organization | CHAPTER-002 | POST | `/api/v1/chapters/bulk` | 지부 일괄 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminChapterControllerApi.java:25` |
| 161 | organization | CHAPTER-003 | DELETE | `/api/v1/chapters/{chapterId}` | 지부 삭제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminChapterControllerApi.java:28` |
| 162 | organization | CHAPTER-101 | GET | `/api/v1/chapters` | 지부 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/ChapterQueryControllerApi.java:18` |
| 163 | organization | CHAPTER-102 | GET | `/api/v1/chapters/with-schools` | 기수별 지부 및 소속 학교 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/ChapterQueryControllerApi.java:29` |
| 164 | organization | CHAPTER-103 | GET | `/api/v1/chapters/{chapterId}` | 지부 ID로 지부 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/ChapterQueryControllerApi.java:41` |
| 165 | organization | GISU-001 | POST | `/api/v1/gisu` | 기수 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuControllerApi.java:13` |
| 166 | organization | GISU-002 | DELETE | `/api/v1/gisu/{gisuId}` | 기수 삭제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuControllerApi.java:20` |
| 167 | organization | GISU-003 | POST | `/api/v1/gisu/{gisuId}/active` | 활성 기수 변경 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuControllerApi.java:28` |
| 168 | organization | GISU-101 | GET | `/api/v1/gisu` | 기수 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuQueryControllerApi.java:25` |
| 169 | organization | GISU-102 | GET | `/api/v1/gisu/all` | 기수 전체 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuQueryControllerApi.java:35` |
| 170 | organization | GISU-103 | GET | `/api/v1/gisu/active` | 활성화된 기수 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuQueryControllerApi.java:46` |
| 171 | organization | SCHOOL-001 | POST | `/api/v1/schools` | 학교 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:17` |
| 172 | organization | SCHOOL-002 | PATCH | `/api/v1/schools/{schoolId}` | 학교 수정 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:25` |
| 173 | organization | SCHOOL-003 | DELETE | `/api/v1/schools` | 학교 삭제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:35` |
| 174 | organization | SCHOOL-004 | PATCH | `/api/v1/schools/{schoolId}/assign` | 학교 지부 배정 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:42` |
| 175 | organization | SCHOOL-005 | PATCH | `/api/v1/schools/{schoolId}/unassign` | 학교 지부 배정 해제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:53` |
| 176 | organization | SCHOOL-101 | GET | `/api/v1/schools/all` | 학교 전체 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/SchoolQueryControllerApi.java:18` |
| 177 | organization | SCHOOL-102 | GET | `/api/v1/schools/{schoolId}` | 학교 상세 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/SchoolQueryControllerApi.java:28` |
| 178 | organization | SCHOOL-103 | GET | `/api/v1/schools/unassigned` | 배정 대기 중인 학교 목록 조회 | O | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/SchoolQueryControllerApi.java:41` |
| 179 | organization | SCHOOL-104 | GET | `/api/v1/schools/link/{schoolId}` | 학교 링크 조회 | O | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/SchoolQueryControllerApi.java:53` |
| 180 | organization | STUDY-GROUP-001 | POST | `/api/v1/study-groups` | 스터디 그룹 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:15` |
| 181 | organization | STUDY-GROUP-002 | PATCH | `/api/v1/study-groups/{studyGroupId}` | 스터디 그룹 수정 (이름만 가능) | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:29` |
| 182 | organization | STUDY-GROUP-003 | PATCH | `/api/v1/study-groups/{studyGroupId}/members/{memberId}` | 스터디 그룹에 스터디원 추가 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:38` |
| 183 | organization | STUDY-GROUP-004 | PATCH | `/api/v1/study-groups/{studyGroupId}/mentors/{mentorId}` | 스터디 그룹에 담당 파트장 추가 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:44` |
| 184 | organization | STUDY-GROUP-005 | DELETE | `/api/v1/study-groups/{studyGroupId}/members/{memberId}` | 스터디 그룹에 스터디원 제거 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:50` |
| 185 | organization | STUDY-GROUP-006 | DELETE | `/api/v1/study-groups/{studyGroupId}/mentors/{mentorId}` | 스터디 그룹에 담당 파트장 제거 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:56` |
| 186 | organization | STUDY-GROUP-007 | DELETE | `/api/v1/study-groups/{studyGroupId}` | 스터디 그룹 삭제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:62` |
| 187 | organization | STUDY-GROUP-101 | GET | `/api/v1/study-groups/managed` | 내가 관리하는 스터디 그룹 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupQueryControllerApi.java:34` |
| 188 | organization | STUDY-GROUP-102 | GET | `/api/v1/study-groups/{studyGroupId}` | 스터디 그룹 정보 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupQueryControllerApi.java:43` |
| 189 | organization | STUDY-GROUP-SCHEDULE-001 | POST | `/api/v1/study-groups/schedules` | 스터디 그룹 일정 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupScheduleControllerApi.java:12` |

## project

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 190 | project | APPLY-001 | POST | `/api/v1/projects/{projectId}/applications` | 챌린저 지원서 Draft 생성 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:47` |
| 191 | project | APPLY-002 | PUT | `/api/v1/projects/{projectId}/applications/me` | 챌린저 지원서 임시저장 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:70` |
| 192 | project | APPLY-003 | POST | `/api/v1/projects/{projectId}/applications/me/submit` | 챌린저 지원서 최종 제출 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:93` |
| 193 | project | APPLY-004 | GET | `/api/v1/projects/me/applications` | 본인 지원 내역 목록 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationQueryController.java:37` |
| 194 | project | APPLY-005 | DELETE | `/api/v1/projects/{projectId}/applications/{applicationId}` | 챌린저 지원서 철회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:147` |
| 195 | project | APPLY-101 | GET | `/api/v1/projects/{projectId}/applications` | PM/운영진 단일 프로젝트 지원자 목록 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationQueryController.java:76` |
| 196 | project | APPLY-102 | GET | `/api/v1/projects/{projectId}/applications/{applicationId}` | 지원서 단건 상세 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationQueryController.java:127` |
| 197 | project | APPLY-103 | PATCH | `/api/v1/projects/{projectId}/applications/{applicationId}/decision` | 지원서 합/불 결정 (단일 PATCH) | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:118` |
| 198 | project | PROJECT-001 | GET | `/api/v1/projects` | 프로젝트 목록 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:46` |
| 199 | project | PROJECT-002 | GET | `/api/v1/projects/{projectId}` | 프로젝트 상세 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:65` |
| 200 | project | PROJECT-003 | GET | `/api/v1/projects/{projectId}/members` | 프로젝트 팀원 구성 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:83` |
| 201 | project | PROJECT-004 | POST | `/api/v1/projects/{projectId}/members` | 프로젝트 팀원 추가 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:142` |
| 202 | project | PROJECT-005 | DELETE | `/api/v1/projects/{projectId}/members/{memberId}` | 프로젝트 팀원 제거 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:244` |
| 203 | project | PROJECT-006 | GET | `/api/v1/projects/me/managed` | 내가 관리하는 프로젝트 목록 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:118` |
| 204 | project | PROJECT-007 | GET | `/api/v1/projects/members` | 프로젝트 팀원 구성 일괄 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:101` |
| 205 | project | PROJECT-101 | POST | `/api/v1/projects` | 프로젝트 Draft 생성 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:62` |
| 206 | project | PROJECT-102 | PATCH | `/api/v1/projects/{projectId}` | 프로젝트 기본정보 수정 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:81` |
| 207 | project | PROJECT-103 | GET | `/api/v1/projects/me/draft` | 내 Draft 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:137` |
| 208 | project | PROJECT-104 | POST | `/api/v1/projects/{projectId}/transfer-ownership` | 프로젝트 소유권 양도 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:122` |
| 209 | project | PROJECT-105 | PUT | `/api/v1/projects/{projectId}/part-quotas` | 파트별 정원 일괄 갱신 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:184` |
| 210 | project | PROJECT-106 | PUT | `/api/v1/projects/{projectId}/application-form` | 지원 폼 저장 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationFormController.java:35` |
| 211 | project | PROJECT-106-GET | GET | `/api/v1/projects/{projectId}/application-form` | 지원 폼 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationFormController.java:56` |
| 212 | project | PROJECT-107 | POST | `/api/v1/projects/{projectId}/submit` | 프로젝트 제출 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:102` |
| 213 | project | PROJECT-108 | POST | `/api/v1/projects/{projectId}/publish` | 프로젝트 공개 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:162` |
| 214 | project | PROJECT-109 | DELETE | `/api/v1/projects/{projectId}` | 프로젝트 삭제 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:204` |
| 215 | project | PROJECT-110 | POST | `/api/v1/projects/{projectId}/abort` | 프로젝트 중단 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:225` |
| 216 | project | PROJECT-MATCHING-001 | GET | `/api/v1/project/matching-rounds` | 매칭 차수 목록 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:44` |
| 217 | project | PROJECT-MATCHING-101 | POST | `/api/v1/project/matching-rounds` | 매칭 차수 생성 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:66` |
| 218 | project | PROJECT-MATCHING-102 | PATCH | `/api/v1/project/matching-rounds/{matchingRoundId}` | 매칭 차수 수정 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:87` |
| 219 | project | PROJECT-MATCHING-103 | DELETE | `/api/v1/project/matching-rounds/{matchingRoundId}` | 매칭 차수 삭제 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:110` |
| 220 | project | PROJECT-MATCHING-201 | POST | `/api/v1/project/matching-rounds/{matchingRoundId}/auto-decide` | 매칭 차수 자동 선발 실행 (운영진 수동 트리거) | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:127` |
| 221 | project | PROJECT-STAT-001 | GET | `/api/v1/projects/{projectId}/statistics` | 단건 프로젝트 지원/매칭 현황 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectStatisticsQueryController.java:30` |
| 222 | project | PROJECT-STAT-002 | GET | `/api/v1/projects/statistics` | 지부 전체 프로젝트 지원/매칭 현황 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectStatisticsQueryController.java:56` |

## schedule

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 223 | schedule | SCHEDULE-C001 | POST | `/api/v2/schedules` | 일정 생성 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:59` |
| 224 | schedule | SCHEDULE-C002 | PATCH | `/api/v2/schedules/{scheduleId}` | 일정 수정 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:125` |
| 225 | schedule | SCHEDULE-C003 | POST | `/api/v2/schedules/{scheduleId}/attendances/request` | 출석 요청하기 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:263` |
| 226 | schedule | SCHEDULE-C004 | POST | `/api/v2/schedules/{scheduleId}/attendances/excuse` | 출석 요청이 불가능한 경우, 사유 제출하기 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:312` |
| 227 | schedule | SCHEDULE-C005 | POST | `/api/v2/schedules/{scheduleId}/attendances/decide` | [운영진용] 출석 요청 승인/거절 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:359` |
| 228 | schedule | SCHEDULE-C006 | DELETE | `/api/v2/schedules/{scheduleId}` | 일정 삭제 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:186` |
| 229 | schedule | SCHEDULE-C007 | DELETE | `/api/v2/schedules/{scheduleId}/force` | 일정 강제 삭제 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:227` |
| 230 | schedule | SCHEDULE-Q001 | GET | `/api/v2/schedules/capabilities` | 일정 생성, 수정 관련 권한 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:42` |
| 231 | schedule | SCHEDULE-Q002 | GET | `/api/v2/schedules/me` | 내 일정 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:66` |
| 232 | schedule | SCHEDULE-Q003 | GET | `/api/v2/schedules/{scheduleId}` | 일정 상세 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:103` |
| 233 | schedule | SCHEDULE-Q004 | GET | `/api/v2/schedules/attendance` | [운영진용] 일정들의 출석 현황 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:143` |
| 234 | schedule | SCHEDULE-Q005 | GET | `/api/v2/schedules/{scheduleId}/attendance` | [운영진용] 단일 일정 출석 현황 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:206` |

## storage

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 235 | storage | STORAGE-001 | POST | `/api/v1/storage/prepare-upload` | 파일 업로드를 위한 Signed URL을 생성합니다. | X | `src/main/java/com/umc/product/storage/adapter/in/web/StorageController.java:35` |
| 236 | storage | STORAGE-002 | POST | `/api/v1/storage/{fileId}/confirm` | 파일 업로드 완료 처리 | X | `src/main/java/com/umc/product/storage/adapter/in/web/StorageController.java:57` |
| 237 | storage | STORAGE-003 | DELETE | `/api/v1/storage/{fileId}` | 파일 삭제 | X | `src/main/java/com/umc/product/storage/adapter/in/web/StorageController.java:67` |

## techblog

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 239 | techblog | TECH-BLOG-001 | GET | `/api/v1/tech-blog/contents/{type}/{slug}/like` | 콘텐츠 좋아요 상태 조회 | X | `src/main/java/com/umc/product/techblog/adapter/in/web/TechBlogInteractionController.java:60` |
| 240 | techblog | TECH-BLOG-002 | POST | `/api/v1/tech-blog/contents/{type}/{slug}/like` | 콘텐츠 좋아요 토글 | X | `src/main/java/com/umc/product/techblog/adapter/in/web/TechBlogInteractionController.java:73` |
| 241 | techblog | TECH-BLOG-003 | GET | `/api/v1/tech-blog/contents/{type}/{slug}/comments` | 댓글 목록 조회 | X | `src/main/java/com/umc/product/techblog/adapter/in/web/TechBlogInteractionController.java:86` |
| 242 | techblog | TECH-BLOG-004 | POST | `/api/v1/tech-blog/contents/{type}/{slug}/comments` | 댓글 작성 | X | `src/main/java/com/umc/product/techblog/adapter/in/web/TechBlogInteractionController.java:107` |
| 243 | techblog | TECH-BLOG-005 | PATCH | `/api/v1/tech-blog/contents/{type}/{slug}/comments/{commentId}` | 댓글 수정 | X | `src/main/java/com/umc/product/techblog/adapter/in/web/TechBlogInteractionController.java:126` |
| 244 | techblog | TECH-BLOG-006 | DELETE | `/api/v1/tech-blog/contents/{type}/{slug}/comments/{commentId}` | 댓글 삭제 | X | `src/main/java/com/umc/product/techblog/adapter/in/web/TechBlogInteractionController.java:146` |
| 245 | techblog | TECH-BLOG-007 | POST | `/api/v1/tech-blog/contents/{type}/{slug}/comments/{commentId}/like` | 댓글 좋아요 토글 | X | `src/main/java/com/umc/product/techblog/adapter/in/web/TechBlogInteractionController.java:159` |

## term

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 246 | term | TERM-001 | POST | `/api/v1/terms` | 약관 생성 | X | `src/main/java/com/umc/product/term/adapter/in/web/TermController.java:63` |
| 247 | term | TERM-101 | GET | `/api/v1/terms/type/{termType}` | 약관 유형으로 약관 조회 | X | `src/main/java/com/umc/product/term/adapter/in/web/TermController.java:42` |
| 248 | term | TERM-102 | GET | `/api/v1/terms/{termsId}` | 약관 ID로 약관 조회 | X | `src/main/java/com/umc/product/term/adapter/in/web/TermController.java:49` |
| 249 | term | TERM-103 | GET | `/api/v1/terms/consent-status/me` | 내 필수 약관 재동의 상태 조회 | X | `src/main/java/com/umc/product/term/adapter/in/web/TermController.java:55` |

## test

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 250 | test | <미지정> | POST | `/test/email/send-test` | <요약 없음> | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:85` |
| 251 | test | <미지정> | GET | `/test/log-test` | <요약 없음> | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:212` |
| 252 | test | SEED-001 | POST | `/test/seed/members` | 더미 멤버 시딩 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:63` |
| 253 | test | SEED-002 | POST | `/test/seed/challengers` | 챌린저 분포 시딩 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:79` |
| 254 | test | SEED-003 | POST | `/test/seed/projects` | 프로젝트 시딩 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:94` |
| 255 | test | SEED-003-S | POST | `/test/seed/projects/scenarios` | 프로젝트 시나리오 시딩 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:110` |
| 256 | test | SEED-004 | POST | `/test/seed/curriculum` | Curriculum 시딩 (Curriculum · WeeklyCurriculum · OriginalWorkbook · Mission) | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:132` |
| 257 | test | SEED-005 | POST | `/test/seed/notice` | Notice 시딩 (지부 · 학교 · 파트 분포) | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:147` |
| 258 | test | SEED-006 | POST | `/test/seed/project-applications` | 지원서 시나리오 시딩 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:166` |
| 259 | test | TEST-001 | GET | `/test/file/{fileId}` | [개발용] 파일 ID를 기반으로 접근 가능한 URL을 조회합니다. | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:65` |
| 260 | test | TEST-002 | POST | `/test/fcm/test-send` | FCM 푸시 알림 테스트 전송 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:77` |
| 261 | test | TEST-003 | GET | `/test/webhook/aop-test` | AOP로 전송하는 알람 테스트 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:104` |
| 262 | test | TEST-004 | POST | `/test/webhook/alarm` | 웹훅 알람 전송 테스트 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:117` |
| 263 | test | TEST-005 | POST | `/test/webhook/alarm/buffer` | 웹훅 알람 버퍼 전송 테스트 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:133` |
| 264 | test | TEST-006 | GET | `/test/apple-client-secret` | Apple Client Secret 생성 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:154` |
| 265 | test | TEST-007 | GET | `/test/token/access` | AccessToken 발급 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:160` |
| 266 | test | TEST-008 | GET | `/test/token/refresh` | RefreshToken 발급 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:173` |
| 267 | test | TEST-009 | GET | `/test/token/email` | EmailVerificationToken 발급 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:180` |
| 268 | test | TEST-010 | GET | `/test/token/oauth` | oAuthVerificationToken 발급 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:190` |
| 269 | test | TEST-011 | GET | `/test/health-check` | 헬스 체크 API | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:198` |
| 270 | test | TEST-012 | GET | `/test/check-authenticated` | 인증된 사용자인지 여부를 확인합니다. | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:205` |
