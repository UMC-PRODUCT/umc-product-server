# API Catalog

운영 중인 API의 ID, 엔드포인트, 메서드, 역할, deprecated 상태를 도메인별로 정리합니다.

> 소스 기준: `@Operation(operationId = "...")`를 우선 사용하고, 없으면 `summary`의 `[XXX-000]` prefix를 API ID로 읽습니다. 갱신: `./gradlew generateDocumentationCatalogs`

## analytics

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 1 | analytics | DASHBOARD-001 | GET | `/api/v1/admin/dashboard/summary` | 운영진 대시보드 요약 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:66` |
| 2 | analytics | DASHBOARD-002 | GET | `/api/v1/admin/dashboard/action-queue` | 운영진 대시보드 액션 큐 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:78` |
| 3 | analytics | DASHBOARD-003 | GET | `/api/v1/admin/dashboard/risk-challengers` | 운영진 대시보드 위험군 챌린저 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:174` |
| 4 | analytics | DASHBOARD-004 | GET | `/api/v1/admin/dashboard/context` | 운영진 대시보드 권한 컨텍스트 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:90` |
| 5 | analytics | DASHBOARD-005 | GET | `/api/v1/admin/dashboard/operations` | 운영 현황 집계 조회 | O | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:101` |
| 6 | analytics | DASHBOARD-006 | GET | `/api/v1/admin/dashboard/operations/schools` | 운영 현황 - 지부별 학교/챌린저 현황 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:114` |
| 7 | analytics | DASHBOARD-007 | GET | `/api/v1/admin/dashboard/operations/points` | 운영 현황 - 지부 내 파트별 상벌점 부여 현황 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:126` |
| 8 | analytics | DASHBOARD-008 | GET | `/api/v1/admin/dashboard/operations/attendance` | 운영 현황 - 일정 및 출석 생성 현황 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:138` |
| 9 | analytics | DASHBOARD-009 | GET | `/api/v1/admin/dashboard/operations/study-groups` | 운영 현황 - 스터디 그룹 및 일정 생성 현황 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:150` |
| 10 | analytics | DASHBOARD-010 | GET | `/api/v1/admin/dashboard/operations/signups` | 운영 현황 - 기간별 신규 가입자 현황 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java:162` |
| 11 | analytics | DASHBOARD-100 | GET | `/api/v1/admin/schools/summary` | 학교별 현황 조회 | X | `src/main/java/com/umc/product/analytics/adapter/in/web/AdminSchoolAnalyticsController.java:30` |

## audit

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 12 | audit | AUDIT-001 | GET | `/api/v1/admin/audit-logs` | 감사 로그 검색 | X | `src/main/java/com/umc/product/audit/adapter/in/web/AuditLogController.java:33` |

## authentication

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 13 | authentication | CREDENTIAL-002 | POST | `/api/v1/auth/credentials` | 비밀번호 자격증명 최초 등록 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:47` |
| 14 | authentication | CREDENTIAL-003 | PATCH | `/api/v1/auth/password` | 비밀번호 변경 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:60` |
| 15 | authentication | CREDENTIAL-005 | GET | `/api/v1/auth/email/availability` | 이메일 사용 가능 여부 조회 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:89` |
| 16 | authentication | CREDENTIAL-007 | PATCH | `/api/v1/auth/password/reset` | 비밀번호 초기화 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:73` |
| 17 | authentication | EMAIL-001 | POST | `/api/v1/auth/email-verification/code` | 6자리 인증코드로 이메일 인증 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/EmailAuthenticationController.java:34` |
| 18 | authentication | EMAIL-002 | POST | `/api/v1/auth/email-verification` | 이메일 인증 코드 발송 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/EmailAuthenticationController.java:60` |
| 19 | authentication | EMAIL-003 | POST | `/api/v1/auth/email-verification/resend` | 이메일 인증 코드 재전송 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/EmailAuthenticationController.java:85` |
| 20 | authentication | LOGIN-001 | POST | `/api/v1/auth/login/google` | Google 로그인 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/swagger/AuthenticationControllerInterface.java:14` |
| 21 | authentication | LOGIN-005 | POST | `/api/v1/auth/login/kakao` | Kakao 로그인 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/swagger/AuthenticationControllerInterface.java:32` |
| 22 | authentication | LOGIN-006 | POST | `/api/v1/auth/login/kakao/code` | Kakao 로그인 (Authorization Code 흐름) | X | `src/main/java/com/umc/product/authentication/adapter/in/web/swagger/AuthenticationControllerInterface.java:50` |
| 23 | authentication | LOGIN-010 | POST | `/api/v1/auth/login/apple` | Apple 로그인 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/swagger/AuthenticationControllerInterface.java:68` |
| 24 | authentication | LOGIN-011 | POST | `/api/v1/auth/login/email` | 이메일/PW 로그인 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/CredentialAuthenticationController.java:100` |
| 25 | authentication | OAUTH-001 | POST | `/api/v1/member-oauth` | 로그인용 OAuth 수단 추가 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/MemberOAuthController.java:37` |
| 26 | authentication | OAUTH-002 | DELETE | `/api/v1/member-oauth/{memberOAuthId}` | 로그인용 OAuth 수단 제거 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/MemberOAuthController.java:58` |
| 27 | authentication | OAUTH-101 | GET | `/api/v1/member-oauth/me` | 현재 회원 계정과 연동된 OAuth 정보 조회 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/MemberOAuthController.java:81` |
| 28 | authentication | TOKEN-001 | POST | `/api/v1/auth/token/renew` | AccessToken 재발급 | X | `src/main/java/com/umc/product/authentication/adapter/in/web/TokenAuthenticationController.java:23` |

## authorization

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 29 | authorization | PERMISSION-001 | GET | `/api/v1/authorization/resource-permission` | 리소스 권한 조회 | X | `src/main/java/com/umc/product/authorization/adapter/in/web/ResourcePermissionController.java:35` |
| 30 | authorization | PERMISSION-002 | POST | `/api/v1/authorization/resource-permissions/batch` | 리소스 권한 배치 조회 | X | `src/main/java/com/umc/product/authorization/adapter/in/web/ResourcePermissionController.java:57` |
| 31 | authorization | STAFF-001 | POST | `/api/v1/authorization/challenger-role` | 운영진 기록 생성 | X | `src/main/java/com/umc/product/authorization/adapter/in/web/ChallengerRoleController.java:45` |
| 32 | authorization | STAFF-002 | DELETE | `/api/v1/authorization/challenger-role/{challengerRoleId}` | 운영진 기록 삭제 | X | `src/main/java/com/umc/product/authorization/adapter/in/web/ChallengerRoleController.java:77` |
| 33 | authorization | STAFF-101 | GET | `/api/v1/authorization/challenger-role/{challengerRoleId}` | 운영진 기록 조회 | O | `src/main/java/com/umc/product/authorization/adapter/in/web/ChallengerRoleController.java:61` |

## challenger

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 34 | challenger | CHALLENGER-001 | POST | `/api/v1/challenger` | 챌린저 생성 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:46` |
| 35 | challenger | CHALLENGER-002 | POST | `/api/v1/challenger/batch` | 챌린저 batch 생성 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:58` |
| 36 | challenger | CHALLENGER-003 | POST | `/api/v1/challenger/{challengerId}/deactivate` | 챌린저 비활성화 (제명/탈부 처리) | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:76` |
| 37 | challenger | CHALLENGER-004 | PATCH | `/api/v1/challenger/{challengerId}/part` | 챌린저 파트 변경 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:89` |
| 38 | challenger | CHALLENGER-005 | DELETE | `/api/v1/challenger/{challengerId}` | [주의] 챌린저 삭제 (Hard Delete) | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java:105` |
| 39 | challenger | CHALLENGER-101 | GET | `/api/v1/challenger/{challengerId}` | 챌린저 정보 조회 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerQueryController.java:44` |
| 40 | challenger | CHALLENGER-102 | GET | `/api/v1/challenger/search/cursor` | 챌린저 검색 (Cursor 기반) | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerSearchController.java:29` |
| 41 | challenger | CHALLENGER-103 | GET | `/api/v1/challenger/search/offset` | 챌린저 검색 (Offset 기반) | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerSearchController.java:52` |
| 42 | challenger | CHALLENGER-104 | GET | `/api/v1/challenger/search/global` | deprecated: 챌린저 전체 검색 (Cursor 기반, 일정 생성용) | O | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerSearchController.java:66` |
| 43 | challenger | CHALLENGER-201 | GET | `/api/v2/challenger/search` | 챌린저 검색 v2 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/v2/ChallengerSearchV2Controller.java:31` |
| 44 | challenger | CHALLENGER-RECORD-001 | POST | `/api/v1/challenger-record/member` | 6자리 코드를 이용해서 회원(계정)에 챌린저 기록 추가 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:42` |
| 45 | challenger | CHALLENGER-RECORD-002 | POST | `/api/v1/challenger-record` | [ADMIN] 과거 챌린저 기록을 위한 코드 생성 기능 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:95` |
| 46 | challenger | CHALLENGER-RECORD-003 | POST | `/api/v1/challenger-record/bulk` | [ADMIN] 챌린저 기록용 코드 벌크 추가 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:122` |
| 47 | challenger | CHALLENGER-RECORD-101 | GET | `/api/v1/challenger-record/code/{code}` | 코드로 ChallengerRecord 조회 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:71` |
| 48 | challenger | CHALLENGER-RECORD-102 | GET | `/api/v1/challenger-record/id/{id}` | ID로 ChallengerRecord 조회 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java:83` |
| 49 | challenger | POINT-001 | POST | `/api/v1/challenger/{challengerId}/points` | 챌린저 상벌점 부여 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandController.java:41` |
| 50 | challenger | POINT-002 | PATCH | `/api/v1/challenger/points/{challengerPointId}` | 챌린저 상벌점 사유 수정 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandController.java:58` |
| 51 | challenger | POINT-003 | DELETE | `/api/v1/challenger/points/{challengerPointId}` | 챌린저 상벌점 삭제 | X | `src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandController.java:73` |

## community

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 52 | community | COMMENT-001 | POST | `/api/v1/posts/{postId}/comments` | 댓글 작성 | X | `src/main/java/com/umc/product/community/adapter/in/web/CommentController.java:42` |
| 53 | community | COMMENT-002 | DELETE | `/api/v1/posts/{postId}/comments/{commentId}` | 댓글 삭제 | X | `src/main/java/com/umc/product/community/adapter/in/web/CommentController.java:74` |
| 54 | community | COMMENT-003 | POST | `/api/v1/posts/{postId}/comments/{commentId}/like` | 댓글 좋아요 토글 | X | `src/main/java/com/umc/product/community/adapter/in/web/CommentController.java:86` |
| 55 | community | COMMENT-101 | GET | `/api/v1/posts/{postId}/comments` | 댓글 목록 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/CommentController.java:56` |
| 56 | community | POST-001 | POST | `/api/v1/posts` | 일반 게시글 생성 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:55` |
| 57 | community | POST-002 | POST | `/api/v1/posts/lightning` | 번개글 생성 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:66` |
| 58 | community | POST-003 | PATCH | `/api/v1/posts/{postId}` | 일반 게시글 수정 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:83` |
| 59 | community | POST-004 | PATCH | `/api/v1/posts/{postId}/lightning` | 번개글 수정 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:98` |
| 60 | community | POST-005 | DELETE | `/api/v1/posts/{postId}` | 게시글 삭제 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:113` |
| 61 | community | POST-006 | POST | `/api/v1/posts/{postId}/like` | 게시글 좋아요 토글 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:121` |
| 62 | community | POST-007 | POST | `/api/v1/posts/{postId}/scrap` | 게시글 스크랩 토글 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostController.java:132` |
| 63 | community | POST-101 | GET | `/api/v1/posts/{postId}` | 게시글 상세 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:65` |
| 64 | community | POST-102 | GET | `/api/v1/posts` | 게시글 목록 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:75` |
| 65 | community | POST-103 | GET | `/api/v1/posts/search` | 게시글 검색 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:95` |
| 66 | community | POST-104 | GET | `/api/v1/posts/my` | 내가 쓴 글 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:112` |
| 67 | community | POST-105 | GET | `/api/v1/posts/commented` | 댓글 단 글 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:125` |
| 68 | community | POST-106 | GET | `/api/v1/posts/scrapped` | 스크랩한 글 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/PostQueryController.java:138` |
| 69 | community | REPORT-001 | POST | `/api/v1/posts/{postId}/reports` | 게시글 신고 | X | `src/main/java/com/umc/product/community/adapter/in/web/ReportController.java:32` |
| 70 | community | REPORT-002 | POST | `/api/v1/comments/{commentId}/reports` | 댓글 신고 | X | `src/main/java/com/umc/product/community/adapter/in/web/ReportController.java:44` |
| 71 | community | TROPHY-001 | POST | `/api/v1/trophies` | 베스트 워크북 생성 | X | `src/main/java/com/umc/product/community/adapter/in/web/TrophyController.java:28` |
| 72 | community | TROPHY-101 | GET | `/api/v1/trophies` | 상장 목록 조회 | X | `src/main/java/com/umc/product/community/adapter/in/web/TrophyQueryController.java:24` |

## curriculum

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 73 | curriculum | CHALLENGER-WORKBOOK-001 | POST | `/api/v2/curriculums/challenger-workbooks/deploy` | 챌린저용: 특정 원본 워크북 배포 요청 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:21` |
| 74 | curriculum | CHALLENGER-WORKBOOK-002 | PATCH | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}` | 챌린저 워크북 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:47` |
| 75 | curriculum | CHALLENGER-WORKBOOK-003 | DELETE | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}` | 챌린저 워크북 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:66` |
| 76 | curriculum | CHALLENGER-WORKBOOK-004 | POST | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}/excuse` | 회장단용: 특정 워크북 인정 처리 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:84` |
| 77 | curriculum | CHALLENGER-WORKBOOK-005 | POST | `/api/v2/curriculums/challenger-workbooks/weekly-best` | 베스트 워크북 선정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:104` |
| 78 | curriculum | CHALLENGER-WORKBOOK-006 | PATCH | `/api/v2/curriculums/challenger-workbooks/weekly-best/{weeklyBestWorkbookId}` | 베스트 워크북 선정 사유 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:126` |
| 79 | curriculum | CHALLENGER-WORKBOOK-007 | DELETE | `/api/v2/curriculums/challenger-workbooks/weekly-best/{weeklyBestWorkbookId}` | 베스트 워크북 선정 철회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookCommandV2Controller.java:145` |
| 80 | curriculum | CHALLENGER-WORKBOOK-MISSION-001 | POST | `/api/v2/curriculums/challenger-workbooks/missions` | 챌린저용: 워크북 내 미션 제출 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:25` |
| 81 | curriculum | CHALLENGER-WORKBOOK-MISSION-002 | PATCH | `/api/v2/curriculums/challenger-workbooks/missions/{missionSubmissionId}` | 챌린저용: 제출한 워크북 미션 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:53` |
| 82 | curriculum | CHALLENGER-WORKBOOK-MISSION-003 | DELETE | `/api/v2/curriculums/challenger-workbooks/missions/{missionSubmissionId}` | 챌린저용: 제출한 워크북 미션 철회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:76` |
| 83 | curriculum | CHALLENGER-WORKBOOK-MISSION-004 | POST | `/api/v2/curriculums/challenger-workbooks/missions/feedback` | 운영진용: 제출된 미션에 대한 피드백 작성 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:93` |
| 84 | curriculum | CHALLENGER-WORKBOOK-MISSION-005 | PATCH | `/api/v2/curriculums/challenger-workbooks/missions/feedback/{missionFeedbackId}` | 운영진용: 제출된 미션에 대한 피드백 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:117` |
| 85 | curriculum | CHALLENGER-WORKBOOK-MISSION-006 | DELETE | `/api/v2/curriculums/challenger-workbooks/missions/feedback/{missionFeedbackId}` | 운영진용: 제출된 미션에 대한 피드백 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/ChallengerWorkbookMissionCommandV2Controller.java:136` |
| 86 | curriculum | CURRICULUM-001 | POST | `/api/v2/curriculums` | 커리큘럼 생성 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:30` |
| 87 | curriculum | CURRICULUM-002 | PATCH | `/api/v2/curriculums/{curriculumId}` | 커리큘럼 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:49` |
| 88 | curriculum | CURRICULUM-003 | DELETE | `/api/v2/curriculums/{curriculumId}` | 중앙운영사무국 총괄단용: 커리큘럼 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:73` |
| 89 | curriculum | CURRICULUM-004 | POST | `/api/v2/curriculums/weekly` | 각 커리큘럼에 새로운 주차 생성 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:94` |
| 90 | curriculum | CURRICULUM-005 | PATCH | `/api/v2/curriculums/weekly/{weeklyCurriculumId}` | 주차별 커리큘럼 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:115` |
| 91 | curriculum | CURRICULUM-006 | DELETE | `/api/v2/curriculums/weekly/{weeklyCurriculumId}` | 주차별 커리큘럼 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java:140` |
| 92 | curriculum | CURRICULUM-101 | GET | `/api/v2/curriculums/overview` | 특정 기수의 파트별 커리큘럼 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumQueryV2Controller.java:28` |
| 93 | curriculum | CURRICULUM-102 | GET | `/api/v2/curriculums/progress/me` | 내 커리큘럼 진행 상황 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumQueryV2Controller.java:55` |
| 94 | curriculum | ORIGINAL-WORKBOOK-001 | POST | `/api/v2/curriculums/original-workbooks` | 중앙파트장용: 원본 워크북 추가 (READY 상태) | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:30` |
| 95 | curriculum | ORIGINAL-WORKBOOK-002 | POST | `/api/v2/curriculums/original-workbooks/draft` | 중앙파트장용: 원본 워크북 임시저장 (DRAFT 상태) | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:51` |
| 96 | curriculum | ORIGINAL-WORKBOOK-003 | PATCH | `/api/v2/curriculums/original-workbooks/{originalWorkbookId}` | 중앙파트장용: 원본 워크북 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:74` |
| 97 | curriculum | ORIGINAL-WORKBOOK-004 | DELETE | `/api/v2/curriculums/original-workbooks/{originalWorkbookId}` | 중앙파트장용: 원본 워크북 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:95` |
| 98 | curriculum | ORIGINAL-WORKBOOK-005 | PATCH | `/api/v2/curriculums/original-workbooks/status` | 중앙파트장용: 원본 워크북 상태 일괄 변경 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java:114` |
| 99 | curriculum | ORIGINAL-WORKBOOK-MISSION-001 | POST | `/api/v2/curriculums/original-workbooks/missions` | 중앙파트장용: 원본 워크북에 미션 추가 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookMissionCommandV2Controller.java:25` |
| 100 | curriculum | ORIGINAL-WORKBOOK-MISSION-002 | PATCH | `/api/v2/curriculums/original-workbooks/missions/{originalWorkbookMissionId}` | 중앙파트장용: 원본 워크북의 미션 수정 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookMissionCommandV2Controller.java:42` |
| 101 | curriculum | ORIGINAL-WORKBOOK-MISSION-003 | DELETE | `/api/v2/curriculums/original-workbooks/missions/{originalWorkbookMissionId}` | 중앙파트장용: 원본 워크북의 미션 삭제 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookMissionCommandV2Controller.java:61` |
| 102 | curriculum | WORKBOOK-101 | GET | `/api/v2/curriculums/original-workbooks/{originalWorkbookId}` | OriginalWorkbook 상세 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/WorkbookQueryV2Controller.java:25` |
| 103 | curriculum | WORKBOOK-102 | GET | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}` | ChallengerWorkbook 상세 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/WorkbookQueryV2Controller.java:38` |
| 104 | curriculum | WORKBOOK-103 | GET | `/api/v2/curriculums/weekly-best-workbooks` | 베스트 워크북 조회 | X | `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/WorkbookQueryV2Controller.java:54` |

## feedback

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 105 | feedback | <미지정> | POST | `/api/v1/user-feedbacks/responses` | 사용자 피드백 응답 제출 | X | `src/main/java/com/umc/product/feedback/adapter/in/web/UserFeedbackController.java:58` |
| 106 | feedback | <미지정> | GET | `/api/v1/user-feedbacks/templates` | 사용자 피드백 템플릿 조회 | X | `src/main/java/com/umc/product/feedback/adapter/in/web/UserFeedbackController.java:34` |

## figma

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 107 | figma | FIGMA-001 | GET | `/api/v1/admin/figma/oauth` | Figma OAuth authorize URL 발급 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaOAuthController.java:32` |
| 108 | figma | FIGMA-002 | GET | `/api/v1/admin/figma/oauth/callback` | Figma OAuth 콜백 처리 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaOAuthController.java:47` |
| 109 | figma | FIGMA-003 | POST | `/api/v1/admin/figma/watched-files` | 폴링 대상 파일 등록 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:37` |
| 110 | figma | FIGMA-004 | DELETE | `/api/v1/admin/figma/watched-files/{watchedFileId}` | 폴링 대상 파일 비활성화 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:47` |
| 111 | figma | FIGMA-005 | POST | `/api/v1/admin/figma/watched-files/{watchedFileId}/enable` | 폴링 대상 파일 활성화 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:54` |
| 112 | figma | FIGMA-006 | POST | `/api/v1/admin/figma/sync` | 활성 파일 전체 즉시 동기화 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java:52` |
| 113 | figma | FIGMA-007 | POST | `/api/v1/admin/figma/sync/watched-files/{watchedFileId}` | 특정 파일 즉시 동기화 (enabled 무관) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java:68` |
| 114 | figma | FIGMA-008 | GET | `/api/v1/admin/figma/watched-files` | 폴링 대상 파일 목록 조회 (enabled 필터) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:61` |
| 115 | figma | FIGMA-009 | GET | `/api/v1/admin/figma/watched-files/{watchedFileId}` | 폴링 대상 파일 단건 조회 (sync 상태 포함) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java:72` |
| 116 | figma | FIGMA-010 | GET | `/api/v1/admin/figma/preview` | 특정 시간대 미리보기 (Discord 발송 X, dispatch / cursor 비변경) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java:100` |
| 117 | figma | FIGMA-011 | POST | `/api/v1/admin/figma/routing-domains` | 라우팅 도메인 등록 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:46` |
| 118 | figma | FIGMA-012 | DELETE | `/api/v1/admin/figma/routing-domains/{domainId}` | 라우팅 도메인 삭제 (mention 도 cascade) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:67` |
| 119 | figma | FIGMA-013 | POST | `/api/v1/admin/figma/routing-domains/{domainId}/mentions` | 라우팅 도메인에 담당자 mention 추가 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:75` |
| 120 | figma | FIGMA-014 | DELETE | `/api/v1/admin/figma/routing-domains/mentions/{mentionId}` | 담당자 mention 삭제 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:97` |
| 121 | figma | FIGMA-015 | POST | `/api/v1/admin/figma/digest` | 특정 시간대 catch-up | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java:82` |
| 122 | figma | FIGMA-016 | GET | `/api/v1/admin/figma/routing-domains` | 라우팅 도메인 목록 조회 (mention 본문 미포함) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:105` |
| 123 | figma | FIGMA-017 | GET | `/api/v1/admin/figma/routing-domains/{domainId}` | 라우팅 도메인 단건 조회 (mention 포함) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:114` |
| 124 | figma | FIGMA-018 | GET | `/api/v1/admin/figma/routing-domains/{domainId}/mentions` | 라우팅 도메인의 담당자 mention 목록 조회 | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:121` |
| 125 | figma | FIGMA-019 | PATCH | `/api/v1/admin/figma/routing-domains/{domainId}` | 라우팅 도메인 수정 (설명 · webhook URL · fallback) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:56` |
| 126 | figma | FIGMA-020 | PATCH | `/api/v1/admin/figma/routing-domains/mentions/{mentionId}` | 담당자 mention 수정 (Discord ID · 라벨) | X | `src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java:86` |

## global

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 127 | global | <미지정> | REQUEST | `/error` | <요약 없음> | X | `src/main/java/com/umc/product/global/exception/CustomErrorController.java:27` |

## maintenance

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 128 | maintenance | MAINT-001 | POST | `/api/v1/admin/maintenance` | 점검 윈도우 생성 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/AdminMaintenanceController.java:36` |
| 129 | maintenance | MAINT-002 | PATCH | `/api/v1/admin/maintenance/{windowId}/end` | 점검 윈도우 강제 종료 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/AdminMaintenanceController.java:50` |
| 130 | maintenance | MAINT-003 | GET | `/api/v1/admin/maintenance` | 점검 윈도우 전체 목록 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/AdminMaintenanceController.java:64` |
| 131 | maintenance | MAINT-004 | GET | `/api/v1/admin/maintenance/{windowId}` | 점검 윈도우 단건 조회 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/AdminMaintenanceController.java:76` |
| 132 | maintenance | SYSTEM-001 | GET | `/api/v1/system/status` | 시스템 점검 상태 조회 | X | `src/main/java/com/umc/product/maintenance/adapter/in/web/SystemStatusController.java:23` |

## member

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 133 | member | MEMBER-001 | PATCH | `/api/v1/member` | 내 회원 정보 수정 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:130` |
| 134 | member | MEMBER-002 | PATCH | `/api/v1/member/profile/links` | 내 회원 프로필 링크 수정 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:144` |
| 135 | member | MEMBER-003 | DELETE | `/api/v1/member` | 회원 탈퇴 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:158` |
| 136 | member | MEMBER-004 | DELETE | `/api/v1/member/{memberId}` | 관리자 권한으로 회원 게정 삭제 (Hard Delete) | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:171` |
| 137 | member | MEMBER-101 | GET | `/api/v1/member/profile/{memberId}` | memberId로 회원 정보 조회 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberQueryController.java:32` |
| 138 | member | MEMBER-102 | GET | `/api/v1/member/me` | 내 프로필 조회 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberQueryController.java:44` |
| 139 | member | MEMBER-103 | GET | `/api/v1/member/search` | 회원 검색 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberQueryController.java:50` |
| 140 | member | MEMBER-201 | GET | `/api/v2/member/me` | 내 종합 정보 조회 | X | `src/main/java/com/umc/product/member/adapter/in/web/v2/MemberQueryV2Controller.java:39` |
| 141 | member | MEMBER-202 | GET | `/api/v2/member/search` | 회원 검색 v2 | X | `src/main/java/com/umc/product/member/adapter/in/web/v2/MemberQueryV2Controller.java:62` |
| 142 | member | REGISTER-001 | POST | `/api/v1/member/register` | OAuth 회원가입 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:61` |
| 143 | member | REGISTER-003 | POST | `/api/v1/member/register/email` | 이메일/PW 이용 회원가입 | X | `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java:103` |

## notice

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 144 | notice | NOTICE-001 | GET | `/api/v1/notices` | 공지사항 전체 조회 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:31` |
| 145 | notice | NOTICE-002 | GET | `/api/v1/notices/search` | 공지사항 검색 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:79` |
| 146 | notice | NOTICE-003 | GET | `/api/v1/notices/{noticeId}` | 공지사항 상세 조회 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:110` |
| 147 | notice | NOTICE-004 | GET | `/api/v1/notices/{noticeId}/read-statics` | 공지사항 읽음 통계 조회 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:134` |
| 148 | notice | NOTICE-005 | GET | `/api/v1/notices/{noticeId}/read-status` | 공지사항 읽음 현황 상세 조회 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeQueryApi.java:143` |
| 149 | notice | NOTICE-101 | POST | `/api/v1/notices/{noticeId}/images` | 공지사항 이미지 추가 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:23` |
| 150 | notice | NOTICE-102 | POST | `/api/v1/notices/{noticeId}/links` | 첫 공지 생성 시 공지사항 링크를 추가하는 API입니다. | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:36` |
| 151 | notice | NOTICE-103 | POST | `/api/v1/notices/{noticeId}/votes` | 공지사항 투표 추가 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:48` |
| 152 | notice | NOTICE-104 | PATCH | `/api/v1/notices/{noticeId}/images` | 공지사항 이미지 전체 수정 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:61` |
| 153 | notice | NOTICE-105 | PATCH | `/api/v1/notices/{noticeId}/links` | 공지사항 링크 전체 수정 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:74` |
| 154 | notice | NOTICE-106 | DELETE | `/api/v1/notices/{noticeId}/vote` | 공지사항 투표 삭제 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeContentApi.java:87` |
| 155 | notice | NOTICE-201 | POST | `/api/v1/notices` | 공지사항 생성 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:21` |
| 156 | notice | NOTICE-202 | DELETE | `/api/v1/notices/{noticeId}` | 공지사항 삭제 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:71` |
| 157 | notice | NOTICE-203 | PATCH | `/api/v1/notices/{noticeId}` | 공지사항 수정 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:93` |
| 158 | notice | NOTICE-204 | POST | `/api/v1/notices/{noticeId}/reminders` | 공지사항 리마인더 발송 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:122` |
| 159 | notice | NOTICE-205 | POST | `/api/v1/notices/{noticeId}/read` | 공지사항 읽음 처리 | X | `src/main/java/com/umc/product/notice/adapter/in/web/swagger/NoticeCommandControllerApi.java:152` |
| 160 | notice | NOTICE-VOTE-001 | POST | `/api/v1/notices/{noticeId}/votes/responses` | 공지사항 투표 응답 제출 | X | `src/main/java/com/umc/product/notice/adapter/in/web/NoticeVoteResponseController.java:30` |
| 161 | notice | NOTICE-VOTE-002 | PUT | `/api/v1/notices/{noticeId}/votes/responses` | 공지사항 투표 응답 수정/취소 | X | `src/main/java/com/umc/product/notice/adapter/in/web/NoticeVoteResponseController.java:50` |

## notification

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 162 | notification | FCM-001 | PUT | `/api/v1/notification/fcm/token` | FCM 토큰 등록 | X | `src/main/java/com/umc/product/notification/adapter/in/web/swagger/FcmControllerApi.java:15` |
| 163 | notification | FCM-002 | DELETE | `/api/v1/notification/fcm/topics/legacy` | Legacy 토픽 구독 해제 | X | `src/main/java/com/umc/product/notification/adapter/in/web/swagger/FcmControllerApi.java:30` |

## organization

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 164 | organization | <미지정> | GET | `/api/v1/gisu/{gisuId}` | <요약 없음> | X | `src/main/java/com/umc/product/organization/adapter/in/web/GisuQueryController.java:28` |
| 165 | organization | <미지정> | GET | `/api/v1/schools/gisu/{gisuId}` | <요약 없음> | X | `src/main/java/com/umc/product/organization/adapter/in/web/SchoolQueryController.java:36` |
| 166 | organization | CHAPTER-001 | POST | `/api/v1/chapters` | 지부 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminChapterControllerApi.java:16` |
| 167 | organization | CHAPTER-002 | POST | `/api/v1/chapters/bulk` | 지부 일괄 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminChapterControllerApi.java:25` |
| 168 | organization | CHAPTER-003 | DELETE | `/api/v1/chapters/{chapterId}` | 지부 삭제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminChapterControllerApi.java:28` |
| 169 | organization | CHAPTER-101 | GET | `/api/v1/chapters` | 지부 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/ChapterQueryControllerApi.java:18` |
| 170 | organization | CHAPTER-102 | GET | `/api/v1/chapters/with-schools` | 기수별 지부 및 소속 학교 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/ChapterQueryControllerApi.java:29` |
| 171 | organization | CHAPTER-103 | GET | `/api/v1/chapters/{chapterId}` | 지부 ID로 지부 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/ChapterQueryControllerApi.java:41` |
| 172 | organization | GISU-001 | POST | `/api/v1/gisu` | 기수 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuControllerApi.java:13` |
| 173 | organization | GISU-002 | DELETE | `/api/v1/gisu/{gisuId}` | 기수 삭제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuControllerApi.java:20` |
| 174 | organization | GISU-003 | POST | `/api/v1/gisu/{gisuId}/active` | 활성 기수 변경 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuControllerApi.java:28` |
| 175 | organization | GISU-101 | GET | `/api/v1/gisu` | 기수 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuQueryControllerApi.java:25` |
| 176 | organization | GISU-102 | GET | `/api/v1/gisu/all` | 기수 전체 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuQueryControllerApi.java:35` |
| 177 | organization | GISU-103 | GET | `/api/v1/gisu/active` | 활성화된 기수 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminGisuQueryControllerApi.java:46` |
| 178 | organization | SCHOOL-001 | POST | `/api/v1/schools` | 학교 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:17` |
| 179 | organization | SCHOOL-002 | PATCH | `/api/v1/schools/{schoolId}` | 학교 수정 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:25` |
| 180 | organization | SCHOOL-003 | DELETE | `/api/v1/schools` | 학교 삭제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:35` |
| 181 | organization | SCHOOL-004 | PATCH | `/api/v1/schools/{schoolId}/assign` | 학교 지부 배정 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:42` |
| 182 | organization | SCHOOL-005 | PATCH | `/api/v1/schools/{schoolId}/unassign` | 학교 지부 배정 해제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/AdminSchoolControllerApi.java:53` |
| 183 | organization | SCHOOL-101 | GET | `/api/v1/schools/all` | 학교 전체 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/SchoolQueryControllerApi.java:18` |
| 184 | organization | SCHOOL-102 | GET | `/api/v1/schools/{schoolId}` | 학교 상세 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/SchoolQueryControllerApi.java:28` |
| 185 | organization | SCHOOL-103 | GET | `/api/v1/schools/unassigned` | 배정 대기 중인 학교 목록 조회 | O | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/SchoolQueryControllerApi.java:41` |
| 186 | organization | SCHOOL-104 | GET | `/api/v1/schools/link/{schoolId}` | 학교 링크 조회 | O | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/SchoolQueryControllerApi.java:53` |
| 187 | organization | STUDY-GROUP-001 | POST | `/api/v1/study-groups` | 스터디 그룹 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:15` |
| 188 | organization | STUDY-GROUP-002 | PATCH | `/api/v1/study-groups/{studyGroupId}` | 스터디 그룹 수정 (이름만 가능) | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:29` |
| 189 | organization | STUDY-GROUP-003 | PATCH | `/api/v1/study-groups/{studyGroupId}/members/{memberId}` | 스터디 그룹에 스터디원 추가 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:38` |
| 190 | organization | STUDY-GROUP-004 | PATCH | `/api/v1/study-groups/{studyGroupId}/mentors/{mentorId}` | 스터디 그룹에 담당 파트장 추가 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:44` |
| 191 | organization | STUDY-GROUP-005 | DELETE | `/api/v1/study-groups/{studyGroupId}/members/{memberId}` | 스터디 그룹에 스터디원 제거 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:50` |
| 192 | organization | STUDY-GROUP-006 | DELETE | `/api/v1/study-groups/{studyGroupId}/mentors/{mentorId}` | 스터디 그룹에 담당 파트장 제거 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:56` |
| 193 | organization | STUDY-GROUP-007 | DELETE | `/api/v1/study-groups/{studyGroupId}` | 스터디 그룹 삭제 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupCommandControllerApi.java:62` |
| 194 | organization | STUDY-GROUP-101 | GET | `/api/v1/study-groups/managed` | 내가 관리하는 스터디 그룹 목록 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupQueryControllerApi.java:34` |
| 195 | organization | STUDY-GROUP-102 | GET | `/api/v1/study-groups/{studyGroupId}` | 스터디 그룹 정보 조회 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupQueryControllerApi.java:43` |
| 196 | organization | STUDY-GROUP-SCHEDULE-001 | POST | `/api/v1/study-groups/schedules` | 스터디 그룹 일정 생성 | X | `src/main/java/com/umc/product/organization/adapter/in/web/swagger/StudyGroupScheduleControllerApi.java:12` |

## project

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 197 | project | APPLY-001 | POST | `/api/v1/projects/{projectId}/applications` | 챌린저 지원서 Draft 생성 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:47` |
| 198 | project | APPLY-002 | PUT | `/api/v1/projects/{projectId}/applications/me` | 챌린저 지원서 임시저장 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:70` |
| 199 | project | APPLY-003 | POST | `/api/v1/projects/{projectId}/applications/me/submit` | 챌린저 지원서 최종 제출 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:93` |
| 200 | project | APPLY-004 | GET | `/api/v1/projects/me/applications` | 본인 지원 내역 목록 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationQueryController.java:37` |
| 201 | project | APPLY-005 | DELETE | `/api/v1/projects/{projectId}/applications/{applicationId}` | 챌린저 지원서 철회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:147` |
| 202 | project | APPLY-101 | GET | `/api/v1/projects/{projectId}/applications` | PM/운영진 단일 프로젝트 지원자 목록 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationQueryController.java:76` |
| 203 | project | APPLY-102 | GET | `/api/v1/projects/{projectId}/applications/{applicationId}` | 지원서 단건 상세 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationQueryController.java:127` |
| 204 | project | APPLY-103 | PATCH | `/api/v1/projects/{projectId}/applications/{applicationId}/decision` | 지원서 합/불 결정 (단일 PATCH) | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationController.java:118` |
| 205 | project | PROJECT-001 | GET | `/api/v1/projects` | 프로젝트 목록 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:46` |
| 206 | project | PROJECT-002 | GET | `/api/v1/projects/{projectId}` | 프로젝트 상세 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:65` |
| 207 | project | PROJECT-003 | GET | `/api/v1/projects/{projectId}/members` | 프로젝트 팀원 구성 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:83` |
| 208 | project | PROJECT-004 | POST | `/api/v1/projects/{projectId}/members` | 프로젝트 팀원 추가 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:142` |
| 209 | project | PROJECT-005 | DELETE | `/api/v1/projects/{projectId}/members/{memberId}` | 프로젝트 팀원 제거 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:244` |
| 210 | project | PROJECT-006 | GET | `/api/v1/projects/me/managed` | 내가 관리하는 프로젝트 목록 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:118` |
| 211 | project | PROJECT-007 | GET | `/api/v1/projects/members` | 프로젝트 팀원 구성 일괄 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:101` |
| 212 | project | PROJECT-101 | POST | `/api/v1/projects` | 프로젝트 Draft 생성 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:62` |
| 213 | project | PROJECT-102 | PATCH | `/api/v1/projects/{projectId}` | 프로젝트 기본정보 수정 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:81` |
| 214 | project | PROJECT-103 | GET | `/api/v1/projects/me/draft` | 내 Draft 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectQueryController.java:137` |
| 215 | project | PROJECT-104 | POST | `/api/v1/projects/{projectId}/transfer-ownership` | 프로젝트 소유권 양도 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:122` |
| 216 | project | PROJECT-105 | PUT | `/api/v1/projects/{projectId}/part-quotas` | 파트별 정원 일괄 갱신 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:184` |
| 217 | project | PROJECT-106 | PUT | `/api/v1/projects/{projectId}/application-form` | 지원 폼 저장 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationFormController.java:35` |
| 218 | project | PROJECT-106-GET | GET | `/api/v1/projects/{projectId}/application-form` | 지원 폼 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationFormController.java:56` |
| 219 | project | PROJECT-107 | POST | `/api/v1/projects/{projectId}/submit` | 프로젝트 제출 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:102` |
| 220 | project | PROJECT-108 | POST | `/api/v1/projects/{projectId}/publish` | 프로젝트 공개 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:162` |
| 221 | project | PROJECT-109 | DELETE | `/api/v1/projects/{projectId}` | 프로젝트 삭제 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:204` |
| 222 | project | PROJECT-110 | POST | `/api/v1/projects/{projectId}/abort` | 프로젝트 중단 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectCommandController.java:225` |
| 223 | project | PROJECT-MATCHING-001 | GET | `/api/v1/project/matching-rounds` | 매칭 차수 목록 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:44` |
| 224 | project | PROJECT-MATCHING-101 | POST | `/api/v1/project/matching-rounds` | 매칭 차수 생성 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:66` |
| 225 | project | PROJECT-MATCHING-102 | PATCH | `/api/v1/project/matching-rounds/{matchingRoundId}` | 매칭 차수 수정 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:87` |
| 226 | project | PROJECT-MATCHING-103 | DELETE | `/api/v1/project/matching-rounds/{matchingRoundId}` | 매칭 차수 삭제 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:110` |
| 227 | project | PROJECT-MATCHING-201 | POST | `/api/v1/project/matching-rounds/{matchingRoundId}/auto-decide` | 매칭 차수 자동 선발 실행 (운영진 수동 트리거) | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundController.java:127` |
| 228 | project | PROJECT-STAT-001 | GET | `/api/v1/projects/{projectId}/statistics` | 단건 프로젝트 지원/매칭 현황 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectStatisticsQueryController.java:30` |
| 229 | project | PROJECT-STAT-002 | GET | `/api/v1/projects/statistics` | 지부 전체 프로젝트 지원/매칭 현황 조회 | X | `src/main/java/com/umc/product/project/adapter/in/web/ProjectStatisticsQueryController.java:56` |

## schedule

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 230 | schedule | SCHEDULE-C001 | POST | `/api/v2/schedules` | 일정 생성 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:59` |
| 231 | schedule | SCHEDULE-C002 | PATCH | `/api/v2/schedules/{scheduleId}` | 일정 수정 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:125` |
| 232 | schedule | SCHEDULE-C003 | POST | `/api/v2/schedules/{scheduleId}/attendances/request` | 출석 요청하기 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:263` |
| 233 | schedule | SCHEDULE-C004 | POST | `/api/v2/schedules/{scheduleId}/attendances/excuse` | 출석 요청이 불가능한 경우, 사유 제출하기 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:312` |
| 234 | schedule | SCHEDULE-C005 | POST | `/api/v2/schedules/{scheduleId}/attendances/decide` | [운영진용] 출석 요청 승인/거절 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:359` |
| 235 | schedule | SCHEDULE-C006 | DELETE | `/api/v2/schedules/{scheduleId}` | 일정 삭제 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:186` |
| 236 | schedule | SCHEDULE-C007 | DELETE | `/api/v2/schedules/{scheduleId}/force` | 일정 강제 삭제 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleCommandV2Controller.java:227` |
| 237 | schedule | SCHEDULE-Q001 | GET | `/api/v2/schedules/capabilities` | 일정 생성, 수정 관련 권한 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:42` |
| 238 | schedule | SCHEDULE-Q002 | GET | `/api/v2/schedules/me` | 내 일정 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:66` |
| 239 | schedule | SCHEDULE-Q003 | GET | `/api/v2/schedules/{scheduleId}` | 일정 상세 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:103` |
| 240 | schedule | SCHEDULE-Q004 | GET | `/api/v2/schedules/attendance` | [운영진용] 일정들의 출석 현황 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:143` |
| 241 | schedule | SCHEDULE-Q005 | GET | `/api/v2/schedules/{scheduleId}/attendance` | [운영진용] 단일 일정 출석 현황 조회 | X | `src/main/java/com/umc/product/schedule/adapter/in/web/v2/ScheduleQueryV2Controller.java:206` |

## storage

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 242 | storage | STORAGE-001 | POST | `/api/v1/storage/prepare-upload` | 파일 업로드를 위한 Signed URL을 생성합니다. | X | `src/main/java/com/umc/product/storage/adapter/in/web/StorageController.java:38` |
| 243 | storage | STORAGE-002 | POST | `/api/v1/storage/{fileId}/confirm` | 파일 업로드 완료 처리 | X | `src/main/java/com/umc/product/storage/adapter/in/web/StorageController.java:60` |
| 244 | storage | STORAGE-003 | DELETE | `/api/v1/storage/{fileId}` | 파일 삭제 | X | `src/main/java/com/umc/product/storage/adapter/in/web/StorageController.java:70` |

## term

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 245 | term | TERM-001 | POST | `/api/v1/terms` | 약관 생성 | X | `src/main/java/com/umc/product/term/adapter/in/web/TermController.java:71` |
| 246 | term | TERM-101 | GET | `/api/v1/terms/type/{termType}` | 약관 유형으로 약관 조회 | X | `src/main/java/com/umc/product/term/adapter/in/web/TermController.java:50` |
| 247 | term | TERM-102 | GET | `/api/v1/terms/{termsId}` | 약관 ID로 약관 조회 | X | `src/main/java/com/umc/product/term/adapter/in/web/TermController.java:57` |
| 248 | term | TERM-103 | GET | `/api/v1/terms/consent-status/me` | 내 필수 약관 재동의 상태 조회 | X | `src/main/java/com/umc/product/term/adapter/in/web/TermController.java:63` |
| 249 | term | TERM-104 | GET | `/api/v1/terms` | 활성 약관 전체 조회 | X | `src/main/java/com/umc/product/term/adapter/in/web/TermController.java:43` |

## test

| 순번 | 도메인 | API ID | HTTP Method | Endpoint | 역할 | Deprecated | Source |
|---:|---|---|---|---|---|:---:|---|
| 250 | test | <미지정> | POST | `/test/email/send-test` | <요약 없음> | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:85` |
| 251 | test | <미지정> | GET | `/test/log-test` | <요약 없음> | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:212` |
| 252 | test | SEED-001 | POST | `/test/seed/members` | 더미 멤버 시딩 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:81` |
| 253 | test | SEED-001-M | POST | `/test/seed/member` | 테스트 멤버 단건 생성 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:97` |
| 254 | test | SEED-002 | POST | `/test/seed/challengers` | 챌린저 분포 시딩 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:111` |
| 255 | test | SEED-002-C | POST | `/test/seed/challenger` | 테스트 챌린저 단건 생성 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:126` |
| 256 | test | SEED-002-R | POST | `/test/seed/challenger-role` | 테스트 챌린저 역할 단건 생성 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:138` |
| 257 | test | SEED-003 | POST | `/test/seed/projects` | 프로젝트 시딩 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:155` |
| 258 | test | SEED-003-D | DELETE | `/test/seed/projects` | 프로젝트 시딩 데이터 삭제 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:171` |
| 259 | test | SEED-003-S | POST | `/test/seed/projects/scenarios` | 프로젝트 시나리오 시딩 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:193` |
| 260 | test | SEED-004 | POST | `/test/seed/curriculum` | Curriculum 시딩 (Curriculum · WeeklyCurriculum · OriginalWorkbook · Mission) | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:215` |
| 261 | test | SEED-005 | POST | `/test/seed/notice` | Notice 시딩 (지부 · 학교 · 파트 분포) | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:230` |
| 262 | test | SEED-006 | POST | `/test/seed/project-applications` | 지원서 시나리오 시딩 | X | `src/main/java/com/umc/product/test/adapter/in/web/SeedController.java:249` |
| 263 | test | TEST-001 | GET | `/test/file/{fileId}` | [개발용] 파일 ID를 기반으로 접근 가능한 URL을 조회합니다. | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:65` |
| 264 | test | TEST-002 | POST | `/test/fcm/test-send` | FCM 푸시 알림 테스트 전송 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:77` |
| 265 | test | TEST-003 | GET | `/test/webhook/aop-test` | AOP로 전송하는 알람 테스트 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:104` |
| 266 | test | TEST-004 | POST | `/test/webhook/alarm` | 웹훅 알람 전송 테스트 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:117` |
| 267 | test | TEST-005 | POST | `/test/webhook/alarm/buffer` | 웹훅 알람 버퍼 전송 테스트 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:133` |
| 268 | test | TEST-006 | GET | `/test/apple-client-secret` | Apple Client Secret 생성 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:154` |
| 269 | test | TEST-007 | GET | `/test/token/access` | AccessToken 발급 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:160` |
| 270 | test | TEST-008 | GET | `/test/token/refresh` | RefreshToken 발급 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:173` |
| 271 | test | TEST-009 | GET | `/test/token/email` | EmailVerificationToken 발급 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:180` |
| 272 | test | TEST-010 | GET | `/test/token/oauth` | oAuthVerificationToken 발급 | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:190` |
| 273 | test | TEST-011 | GET | `/test/health-check` | 헬스 체크 API | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:198` |
| 274 | test | TEST-012 | GET | `/test/check-authenticated` | 인증된 사용자인지 여부를 확인합니다. | X | `src/main/java/com/umc/product/test/adapter/in/web/TestController.java:205` |

