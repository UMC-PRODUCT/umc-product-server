# UMC PRODUCT API 목록

UMC PRODUCT 서버에서 제공하는 모든 API의 ID, 엔드포인트, HTTP 메서드, 역할, deprecated 여부를 정리한 표입니다. Swagger 상의 `summary` prefix(`[XXX-NNN]`)로 사용되는 API ID를 기준으로 도메인별로 그룹화하였습니다.

> 본 문서는 컨트롤러 코드를 직접 스캔하여 자동 추출한 것이며, API 추가/수정 시 함께 갱신해주세요.

## audit

| 순번 | 도메인   | API ID    | Endpoint                   | HTTP Method | 역할       | Deprecated |
|---:|-------|-----------|----------------------------|-------------|----------|:----------:|
|  1 | audit | AUDIT-001 | `/api/v1/admin/audit-logs` | GET         | 감사 로그 검색 |     X      |

## authentication

| 순번 | 도메인            | API ID         | Endpoint                                 | HTTP Method | 역할                        | Deprecated |
|---:|----------------|----------------|------------------------------------------|-------------|---------------------------|:----------:|
|  2 | authentication | CREDENTIAL-001 | `/api/v1/auth/login-id/availability`     | GET         | 로그인 ID 사용 가능 여부 조회        |     X      |
|  3 | authentication | CREDENTIAL-002 | `/api/v1/auth/credentials`               | POST        | ID/PW 최초 등록               |     X      |
|  4 | authentication | CREDENTIAL-003 | `/api/v1/auth/password`                  | PATCH       | 비밀번호 변경                   |     X      |
|  5 | authentication | EMAIL-001      | `/api/v1/auth/email-verification/code`   | POST        | 6자리 인증코드로 이메일 인증          |     X      |
|  6 | authentication | EMAIL-002      | `/api/v1/auth/email-verification`        | POST        | 이메일 인증 코드 발송              |     X      |
|  7 | authentication | EMAIL-003      | `/api/v1/auth/email-verification/resend` | POST        | 이메일 인증 코드 재전송             |     X      |
|  8 | authentication | LOGIN-001      | `/api/v1/auth/login/google`              | POST        | Google 로그인                |     X      |
|  9 | authentication | LOGIN-002      | `/api/v1/auth/login/kakao`               | POST        | Kakao 로그인                 |     X      |
| 10 | authentication | LOGIN-003      | `/api/v1/auth/login/apple`               | POST        | Apple 로그인                 |     X      |
| 11 | authentication | LOGIN-004      | `/api/v1/auth/login/id-pw`               | POST        | ID/PW 로그인                 |     X      |
| 12 | authentication | OAUTH-001      | `/api/v1/member-oauth`                   | POST        | 로그인용 OAuth 수단 추가          |     X      |
| 13 | authentication | OAUTH-002      | `/api/v1/member-oauth/{memberOAuthId}`   | DELETE      | 로그인용 OAuth 수단 제거          |     X      |
| 14 | authentication | OAUTH-101      | `/api/v1/member-oauth/me`                | GET         | 현재 회원 계정과 연동된 OAuth 정보 조회 |     X      |
| 15 | authentication | TOKEN-001      | `/api/v1/auth/token/renew`               | POST        | AccessToken 재발급           |     X      |

## authorization

| 순번 | 도메인           | API ID         | Endpoint                                                   | HTTP Method | 역할        | Deprecated |
|---:|---------------|----------------|------------------------------------------------------------|-------------|-----------|:----------:|
| 16 | authorization | PERMISSION-001 | `/api/v1/authorization/resource-permission`                | GET         | 리소스 권한 조회 |     X      |
| 17 | authorization | STAFF-001      | `/api/v1/authorization/challenger-role`                    | POST        | 운영진 기록 생성 |     X      |
| 18 | authorization | STAFF-002      | `/api/v1/authorization/challenger-role/{challengerRoleId}` | DELETE      | 운영진 기록 삭제 |     X      |
| 19 | authorization | STAFF-101      | `/api/v1/authorization/challenger-role/{challengerRoleId}` | GET         | 운영진 기록 조회 |     O      |

## challenger

| 순번 | 도메인        | API ID                | Endpoint                                        | HTTP Method | 역할                             | Deprecated |
|---:|------------|-----------------------|-------------------------------------------------|-------------|--------------------------------|:----------:|
| 20 | challenger | CHALLENGER-001        | `/api/v1/challenger`                            | POST        | 챌린저 생성                         |     X      |
| 21 | challenger | CHALLENGER-002        | `/api/v1/challenger/batch`                      | POST        | 챌린저 batch 생성                   |     X      |
| 22 | challenger | CHALLENGER-003        | `/api/v1/challenger/{challengerId}/deactivate`  | POST        | 챌린저 비활성화 (제명/탈부 처리)            |     X      |
| 23 | challenger | CHALLENGER-004        | `/api/v1/challenger/{challengerId}/part`        | PATCH       | 챌린저 파트 변경                      |     X      |
| 24 | challenger | CHALLENGER-005        | `/api/v1/challenger/{challengerId}`             | DELETE      | [주의] 챌린저 삭제 (Hard Delete)      |     X      |
| 25 | challenger | CHALLENGER-101        | `/api/v1/challenger/{challengerId}`             | GET         | 챌린저 정보 조회                      |     X      |
| 26 | challenger | CHALLENGER-102        | `/api/v1/challenger/search/cursor`              | GET         | 챌린저 검색 (Cursor 기반)             |     X      |
| 27 | challenger | CHALLENGER-103        | `/api/v1/challenger/search/offset`              | GET         | 챌린저 검색 (Offset 기반)             |     X      |
| 28 | challenger | CHALLENGER-104        | `/api/v1/challenger/search/global`              | GET         | 챌린저 전체 검색 (Cursor 기반, 일정 생성용)  |     O      |
| 29 | challenger | CHALLENGER-RECORD-001 | `/api/v1/challenger-record/member`              | POST        | 6자리 코드를 이용해서 회원(계정)에 챌린저 기록 추가 |     X      |
| 30 | challenger | CHALLENGER-RECORD-002 | `/api/v1/challenger-record`                     | POST        | [ADMIN] 과거 챌린저 기록을 위한 코드 생성 기능 |     X      |
| 31 | challenger | CHALLENGER-RECORD-003 | `/api/v1/challenger-record/bulk`                | POST        | [ADMIN] 챌린저 기록용 코드 벌크 추가       |     X      |
| 32 | challenger | CHALLENGER-RECORD-101 | `/api/v1/challenger-record/code/{code}`         | GET         | 코드로 ChallengerRecord 조회        |     X      |
| 33 | challenger | CHALLENGER-RECORD-102 | `/api/v1/challenger-record/id/{id}`             | GET         | ID로 ChallengerRecord 조회        |     X      |
| 34 | challenger | POINT-001             | `/api/v1/challenger/{challengerId}/points`      | POST        | 챌린저 상벌점 부여                     |     X      |
| 35 | challenger | POINT-002             | `/api/v1/challenger/points/{challengerPointId}` | PATCH       | 챌린저 상벌점 사유 수정                  |     X      |
| 36 | challenger | POINT-003             | `/api/v1/challenger/points/{challengerPointId}` | DELETE      | 챌린저 상벌점 삭제                     |     X      |

## community

| 순번 | 도메인       | API ID      | Endpoint                                           | HTTP Method | 역할         | Deprecated |
|---:|-----------|-------------|----------------------------------------------------|-------------|------------|:----------:|
| 37 | community | COMMENT-001 | `/api/v1/posts/{postId}/comments`                  | POST        | 댓글 작성      |     X      |
| 38 | community | COMMENT-002 | `/api/v1/posts/{postId}/comments/{commentId}`      | DELETE      | 댓글 삭제      |     X      |
| 39 | community | COMMENT-003 | `/api/v1/posts/{postId}/comments/{commentId}/like` | POST        | 댓글 좋아요 토글  |     X      |
| 40 | community | COMMENT-101 | `/api/v1/posts/{postId}/comments`                  | GET         | 댓글 목록 조회   |     X      |
| 41 | community | POST-001    | `/api/v1/posts`                                    | POST        | 일반 게시글 생성  |     X      |
| 42 | community | POST-002    | `/api/v1/posts/lightning`                          | POST        | 번개글 생성     |     X      |
| 43 | community | POST-003    | `/api/v1/posts/{postId}`                           | PATCH       | 일반 게시글 수정  |     X      |
| 44 | community | POST-004    | `/api/v1/posts/{postId}/lightning`                 | PATCH       | 번개글 수정     |     X      |
| 45 | community | POST-005    | `/api/v1/posts/{postId}`                           | DELETE      | 게시글 삭제     |     X      |
| 46 | community | POST-006    | `/api/v1/posts/{postId}/like`                      | POST        | 게시글 좋아요 토글 |     X      |
| 47 | community | POST-007    | `/api/v1/posts/{postId}/scrap`                     | POST        | 게시글 스크랩 토글 |     X      |
| 48 | community | POST-101    | `/api/v1/posts/{postId}`                           | GET         | 게시글 상세 조회  |     X      |
| 49 | community | POST-102    | `/api/v1/posts`                                    | GET         | 게시글 목록 조회  |     X      |
| 50 | community | POST-103    | `/api/v1/posts/search`                             | GET         | 게시글 검색     |     X      |
| 51 | community | POST-104    | `/api/v1/posts/my`                                 | GET         | 내가 쓴 글 조회  |     X      |
| 52 | community | POST-105    | `/api/v1/posts/commented`                          | GET         | 댓글 단 글 조회  |     X      |
| 53 | community | POST-106    | `/api/v1/posts/scrapped`                           | GET         | 스크랩한 글 조회  |     X      |
| 54 | community | REPORT-001  | `/api/v1/posts/{postId}/reports`                   | POST        | 게시글 신고     |     X      |
| 55 | community | REPORT-002  | `/api/v1/comments/{commentId}/reports`             | POST        | 댓글 신고      |     X      |
| 56 | community | TROPHY-001  | `/api/v1/trophies`                                 | POST        | 베스트 워크북 생성 |     X      |
| 57 | community | TROPHY-101  | `/api/v1/trophies`                                 | GET         | 상장 목록 조회   |     X      |

## curriculum

| 순번 | 도메인        | API ID                          | Endpoint                                                                         | HTTP Method | 역할                             | Deprecated |
|---:|------------|---------------------------------|----------------------------------------------------------------------------------|-------------|--------------------------------|:----------:|
| 58 | curriculum | CHALLENGER-WORKBOOK-001         | `/api/v2/curriculums/challenger-workbooks/deploy`                                | POST        | 챌린저용: 특정 원본 워크북 배포 요청          |     X      |
| 59 | curriculum | CHALLENGER-WORKBOOK-002         | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}`                | PATCH       | 챌린저 워크북 수정                     |     X      |
| 60 | curriculum | CHALLENGER-WORKBOOK-003         | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}`                | DELETE      | 챌린저 워크북 삭제                     |     X      |
| 61 | curriculum | CHALLENGER-WORKBOOK-004         | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}/excuse`         | POST        | 회장단용: 특정 워크북 인정 처리             |     X      |
| 62 | curriculum | CHALLENGER-WORKBOOK-005         | `/api/v2/curriculums/challenger-workbooks/weekly-best`                           | POST        | 베스트 워크북 선정                     |     X      |
| 63 | curriculum | CHALLENGER-WORKBOOK-006         | `/api/v2/curriculums/challenger-workbooks/weekly-best/{weeklyBestWorkbookId}`    | PATCH       | 베스트 워크북 선정 사유 수정               |     X      |
| 64 | curriculum | CHALLENGER-WORKBOOK-007         | `/api/v2/curriculums/challenger-workbooks/weekly-best/{weeklyBestWorkbookId}`    | DELETE      | 베스트 워크북 선정 철회                  |     X      |
| 65 | curriculum | CHALLENGER-WORKBOOK-MISSION-001 | `/api/v2/curriculums/challenger-workbooks/missions`                              | POST        | 챌린저용: 워크북 내 미션 제출              |     X      |
| 66 | curriculum | CHALLENGER-WORKBOOK-MISSION-002 | `/api/v2/curriculums/challenger-workbooks/missions/{missionSubmissionId}`        | PATCH       | 챌린저용: 제출한 워크북 미션 수정            |     X      |
| 67 | curriculum | CHALLENGER-WORKBOOK-MISSION-003 | `/api/v2/curriculums/challenger-workbooks/missions/{missionSubmissionId}`        | DELETE      | 챌린저용: 제출한 워크북 미션 철회            |     X      |
| 68 | curriculum | CHALLENGER-WORKBOOK-MISSION-004 | `/api/v2/curriculums/challenger-workbooks/missions/feedback`                     | POST        | 운영진용: 제출된 미션에 대한 피드백 작성        |     X      |
| 69 | curriculum | CHALLENGER-WORKBOOK-MISSION-005 | `/api/v2/curriculums/challenger-workbooks/missions/feedback/{missionFeedbackId}` | PATCH       | 운영진용: 제출된 미션에 대한 피드백 수정        |     X      |
| 70 | curriculum | CHALLENGER-WORKBOOK-MISSION-006 | `/api/v2/curriculums/challenger-workbooks/missions/feedback/{missionFeedbackId}` | DELETE      | 운영진용: 제출된 미션에 대한 피드백 삭제        |     X      |
| 71 | curriculum | CURRICULUM-001                  | `/api/v2/curriculums`                                                            | POST        | 커리큘럼 생성                        |     X      |
| 72 | curriculum | CURRICULUM-002                  | `/api/v2/curriculums/{curriculumId}`                                             | PATCH       | 커리큘럼 수정                        |     X      |
| 73 | curriculum | CURRICULUM-003                  | `/api/v2/curriculums/{curriculumId}`                                             | DELETE      | 중앙운영사무국 총괄단용: 커리큘럼 삭제          |     X      |
| 74 | curriculum | CURRICULUM-004                  | `/api/v2/curriculums/weekly`                                                     | POST        | 각 커리큘럼에 새로운 주차 생성              |     X      |
| 75 | curriculum | CURRICULUM-005                  | `/api/v2/curriculums/weekly/{weeklyCurriculumId}`                                | PATCH       | 주차별 커리큘럼 수정                    |     X      |
| 76 | curriculum | CURRICULUM-006                  | `/api/v2/curriculums/weekly/{weeklyCurriculumId}`                                | DELETE      | 주차별 커리큘럼 삭제                    |     X      |
| 77 | curriculum | CURRICULUM-101                  | `/api/v2/curriculums/overview`                                                   | GET         | 특정 기수의 파트별 커리큘럼 조회             |     X      |
| 78 | curriculum | CURRICULUM-102                  | `/api/v2/curriculums/progress/me`                                                | GET         | 내 커리큘럼 진행 상황 조회                |     X      |
| 79 | curriculum | ORIGINAL-WORKBOOK-001           | `/api/v2/curriculums/original-workbooks`                                         | POST        | 중앙파트장용: 원본 워크북 추가 (READY 상태)   |     X      |
| 80 | curriculum | ORIGINAL-WORKBOOK-002           | `/api/v2/curriculums/original-workbooks/draft`                                   | POST        | 중앙파트장용: 원본 워크북 임시저장 (DRAFT 상태) |     X      |
| 81 | curriculum | ORIGINAL-WORKBOOK-003           | `/api/v2/curriculums/original-workbooks/{originalWorkbookId}`                    | PATCH       | 중앙파트장용: 원본 워크북 수정              |     X      |
| 82 | curriculum | ORIGINAL-WORKBOOK-004           | `/api/v2/curriculums/original-workbooks/{originalWorkbookId}`                    | DELETE      | 중앙파트장용: 원본 워크북 삭제              |     X      |
| 83 | curriculum | ORIGINAL-WORKBOOK-005           | `/api/v2/curriculums/original-workbooks/status`                                  | PATCH       | 중앙파트장용: 원본 워크북 상태 일괄 변경        |     X      |
| 84 | curriculum | ORIGINAL-WORKBOOK-MISSION-001   | `/api/v2/curriculums/original-workbooks/missions`                                | POST        | 중앙파트장용: 원본 워크북에 미션 추가          |     X      |
| 85 | curriculum | ORIGINAL-WORKBOOK-MISSION-002   | `/api/v2/curriculums/original-workbooks/missions/{originalWorkbookMissionId}`    | PATCH       | 중앙파트장용: 원본 워크북의 미션 수정          |     X      |
| 86 | curriculum | ORIGINAL-WORKBOOK-MISSION-003   | `/api/v2/curriculums/original-workbooks/missions/{originalWorkbookMissionId}`    | DELETE      | 중앙파트장용: 원본 워크북의 미션 삭제          |     X      |
| 87 | curriculum | WORKBOOK-101                    | `/api/v2/curriculums/original-workbooks/{originalWorkbookId}`                    | GET         | OriginalWorkbook 상세 조회         |     X      |
| 88 | curriculum | WORKBOOK-102                    | `/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}`                | GET         | ChallengerWorkbook 상세 조회       |     X      |
| 89 | curriculum | WORKBOOK-103                    | `/api/v2/curriculums/weekly-best-workbooks`                                      | GET         | 베스트 워크북 조회                     |     X      |

## member

| 순번 | 도메인    | API ID       | Endpoint                            | HTTP Method | 역할                              | Deprecated |
|---:|--------|--------------|-------------------------------------|-------------|---------------------------------|:----------:|
| 90 | member | MEMBER-001   | `/api/v1/member`                    | PATCH       | 내 회원 정보 수정                      |     X      |
| 91 | member | MEMBER-002   | `/api/v1/member/profile/links`      | PATCH       | 내 회원 프로필 링크 수정                  |     X      |
| 92 | member | MEMBER-003   | `/api/v1/member`                    | DELETE      | 회원 탈퇴                           |     X      |
| 93 | member | MEMBER-004   | `/api/v1/member/{memberId}`         | DELETE      | 관리자 권한으로 회원 계정 삭제 (Hard Delete) |     X      |
| 94 | member | MEMBER-101   | `/api/v1/member/profile/{memberId}` | GET         | memberId로 회원 정보 조회              |     X      |
| 95 | member | MEMBER-102   | `/api/v1/member/me`                 | GET         | 내 프로필 조회                        |     X      |
| 96 | member | MEMBER-103   | `/api/v1/member/search`             | GET         | 회원 검색                           |     X      |
| 97 | member | REGISTER-001 | `/api/v1/member/register`           | POST        | OAuth 회원가입                      |     X      |
| 98 | member | REGISTER-002 | `/api/v1/member/register/id-pw`     | POST        | ID/PW 이용 회원가입                   |     X      |

## notice

|  순번 | 도메인    | API ID          | Endpoint                                     | HTTP Method | 역할               | Deprecated |
|----:|--------|-----------------|----------------------------------------------|-------------|------------------|:----------:|
|  99 | notice | NOTICE-001      | `/api/v1/notices`                            | GET         | 공지사항 전체 조회       |     X      |
| 100 | notice | NOTICE-002      | `/api/v1/notices/search`                     | GET         | 공지사항 검색          |     X      |
| 101 | notice | NOTICE-003      | `/api/v1/notices/{noticeId}`                 | GET         | 공지사항 상세 조회       |     X      |
| 102 | notice | NOTICE-004      | `/api/v1/notices/{noticeId}/read-statics`    | GET         | 공지사항 읽음 통계 조회    |     X      |
| 103 | notice | NOTICE-005      | `/api/v1/notices/{noticeId}/read-status`     | GET         | 공지사항 읽음 현황 상세 조회 |     X      |
| 104 | notice | NOTICE-101      | `/api/v1/notices/{noticeId}/images`          | POST        | 공지사항 이미지 추가      |     X      |
| 105 | notice | NOTICE-102      | `/api/v1/notices/{noticeId}/links`           | POST        | 공지사항 링크 추가       |     X      |
| 106 | notice | NOTICE-103      | `/api/v1/notices/{noticeId}/votes`           | POST        | 공지사항 투표 추가       |     X      |
| 107 | notice | NOTICE-104      | `/api/v1/notices/{noticeId}/images`          | PATCH       | 공지사항 이미지 전체 수정   |     X      |
| 108 | notice | NOTICE-105      | `/api/v1/notices/{noticeId}/links`           | PATCH       | 공지사항 링크 전체 수정    |     X      |
| 109 | notice | NOTICE-106      | `/api/v1/notices/{noticeId}/vote`            | DELETE      | 공지사항 투표 삭제       |     X      |
| 110 | notice | NOTICE-201      | `/api/v1/notices`                            | POST        | 공지사항 생성          |     X      |
| 111 | notice | NOTICE-202      | `/api/v1/notices/{noticeId}`                 | DELETE      | 공지사항 삭제          |     X      |
| 112 | notice | NOTICE-203      | `/api/v1/notices/{noticeId}`                 | PATCH       | 공지사항 수정          |     X      |
| 113 | notice | NOTICE-204      | `/api/v1/notices/{noticeId}/reminders`       | POST        | 공지사항 리마인더 발송     |     X      |
| 114 | notice | NOTICE-205      | `/api/v1/notices/{noticeId}/read`            | POST        | 공지사항 읽음 처리       |     X      |
| 115 | notice | NOTICE-VOTE-001 | `/api/v1/notices/{noticeId}/votes/responses` | POST        | 공지사항 투표 응답 제출    |     X      |
| 116 | notice | NOTICE-VOTE-002 | `/api/v1/notices/{noticeId}/votes/responses` | PUT         | 공지사항 투표 응답 수정/취소 |     X      |

## notification

|  순번 | 도메인          | API ID  | Endpoint                                 | HTTP Method | 역할                                  | Deprecated |
|----:|--------------|---------|------------------------------------------|-------------|-------------------------------------|:----------:|
| 117 | notification | FCM-001 | `/api/v1/notification/fcm/token`         | PUT         | FCM 토큰 등록                           |     X      |
| 118 | notification | FCM-002 | `/api/v1/notification/fcm/topics/legacy` | DELETE      | Legacy 토픽 구독 해제                     |     X      |
| 119 | notification | FCM-003 | (HTTP 미매핑)                               | -           | FCM Topic 재구독 처리 (구현체에 매핑 어노테이션 부재) |     X      |

## organization

|  순번 | 도메인          | API ID                   | Endpoint                                                 | HTTP Method | 역할                   | Deprecated |
|----:|--------------|--------------------------|----------------------------------------------------------|-------------|----------------------|:----------:|
| 120 | organization | CHAPTER-001              | `/api/v1/chapters`                                       | POST        | 지부 생성                |     X      |
| 121 | organization | CHAPTER-002              | `/api/v1/chapters/bulk`                                  | POST        | 지부 일괄 생성             |     X      |
| 122 | organization | CHAPTER-003              | `/api/v1/chapters/{chapterId}`                           | DELETE      | 지부 삭제                |     X      |
| 123 | organization | CHAPTER-101              | `/api/v1/chapters`                                       | GET         | 지부 목록 조회             |     O      |
| 124 | organization | CHAPTER-102              | `/api/v1/chapters/with-schools`                          | GET         | 기수별 지부 및 소속 학교 목록 조회 |     X      |
| 125 | organization | CHAPTER-103              | `/api/v1/chapters/{chapterId}`                           | GET         | 지부 ID로 지부 조회         |     O      |
| 126 | organization | GISU-001                 | `/api/v1/gisu`                                           | POST        | 기수 생성                |     X      |
| 127 | organization | GISU-002                 | `/api/v1/gisu/{gisuId}`                                  | DELETE      | 기수 삭제                |     X      |
| 128 | organization | GISU-003                 | `/api/v1/gisu/{gisuId}/active`                           | POST        | 활성 기수 변경             |     X      |
| 129 | organization | GISU-101                 | `/api/v1/gisu`                                           | GET         | 기수 목록 조회             |     X      |
| 130 | organization | GISU-102                 | `/api/v1/gisu/all`                                       | GET         | 기수 전체 목록 조회          |     O      |
| 131 | organization | GISU-103                 | `/api/v1/gisu/active`                                    | GET         | 활성화된 기수 조회           |     X      |
| 132 | organization | SCHOOL-001               | `/api/v1/schools`                                        | POST        | 학교 생성                |     X      |
| 133 | organization | SCHOOL-002               | `/api/v1/schools/{schoolId}`                             | PATCH       | 학교 수정                |     X      |
| 134 | organization | SCHOOL-003               | `/api/v1/schools`                                        | DELETE      | 학교 삭제                |     X      |
| 135 | organization | SCHOOL-004               | `/api/v1/schools/{schoolId}/assign`                      | PATCH       | 학교 지부 배정             |     O      |
| 136 | organization | SCHOOL-005               | `/api/v1/schools/{schoolId}/unassign`                    | PATCH       | 학교 지부 배정 해제          |     O      |
| 137 | organization | SCHOOL-101               | `/api/v1/schools/all`                                    | GET         | 학교 전체 목록 조회          |     X      |
| 138 | organization | SCHOOL-102               | `/api/v1/schools/{schoolId}`                             | GET         | 학교 상세 조회             |     X      |
| 139 | organization | SCHOOL-103               | `/api/v1/schools/unassigned`                             | GET         | 배정 대기 중인 학교 목록 조회    |     X      |
| 140 | organization | SCHOOL-104               | `/api/v1/schools/link/{schoolId}`                        | GET         | 학교 링크 조회             |     X      |
| 141 | organization | STUDY-GROUP-001          | `/api/v1/study-groups`                                   | POST        | 스터디 그룹 생성            |     X      |
| 142 | organization | STUDY-GROUP-002          | `/api/v1/study-groups/{studyGroupId}`                    | PATCH       | 스터디 그룹 수정 (이름만 가능)   |     X      |
| 143 | organization | STUDY-GROUP-003          | `/api/v1/study-groups/{studyGroupId}/members/{memberId}` | PATCH       | 스터디 그룹에 스터디원 추가      |     X      |
| 144 | organization | STUDY-GROUP-004          | `/api/v1/study-groups/{studyGroupId}/mentors/{mentorId}` | PATCH       | 스터디 그룹에 담당 파트장 추가    |     X      |
| 145 | organization | STUDY-GROUP-005          | `/api/v1/study-groups/{studyGroupId}/members/{memberId}` | DELETE      | 스터디 그룹에서 스터디원 제거     |     X      |
| 146 | organization | STUDY-GROUP-006          | `/api/v1/study-groups/{studyGroupId}/mentors/{mentorId}` | DELETE      | 스터디 그룹에서 담당 파트장 제거   |     X      |
| 147 | organization | STUDY-GROUP-007          | `/api/v1/study-groups/{studyGroupId}`                    | DELETE      | 스터디 그룹 삭제            |     X      |
| 148 | organization | STUDY-GROUP-101          | `/api/v1/study-groups/managed`                           | GET         | 내가 관리하는 스터디 그룹 목록 조회 |     X      |
| 149 | organization | STUDY-GROUP-102          | `/api/v1/study-groups/{studyGroupId}`                    | GET         | 스터디 그룹 정보 조회         |     X      |
| 150 | organization | STUDY-GROUP-SCHEDULE-001 | `/api/v1/study-groups/schedules`                         | POST        | 스터디 그룹 일정 생성         |     X      |

## project

|  순번 | 도메인     | API ID               | Endpoint                                            | HTTP Method | 역할              | Deprecated |
|----:|---------|----------------------|-----------------------------------------------------|-------------|-----------------|:----------:|
| 151 | project | PROJECT-001          | `/api/v1/projects`                                  | GET         | 프로젝트 목록 조회      |     X      |
| 152 | project | PROJECT-002          | `/api/v1/projects/{projectId}`                      | GET         | 프로젝트 상세 조회      |     X      |
| 153 | project | PROJECT-003          | `/api/v1/projects/{projectId}/members`              | GET         | 프로젝트 팀원 구성 조회   |     X      |
| 154 | project | PROJECT-004          | `/api/v1/projects/{projectId}/members`              | POST        | 프로젝트 팀원 추가      |     X      |
| 155 | project | PROJECT-005          | `/api/v1/projects/{projectId}/members/{memberId}`   | DELETE      | 프로젝트 팀원 제거      |     X      |
| 156 | project | PROJECT-006          | `/api/v1/projects/me/managed`                       | GET         | 내가 관리하는 프로젝트 목록 |     X      |
| 157 | project | PROJECT-101          | `/api/v1/projects`                                  | POST        | 프로젝트 Draft 생성   |     X      |
| 158 | project | PROJECT-102          | `/api/v1/projects/{projectId}`                      | PATCH       | 프로젝트 기본정보 수정    |     X      |
| 159 | project | PROJECT-103          | `/api/v1/projects/me/draft`                         | GET         | 내 Draft 조회      |     X      |
| 160 | project | PROJECT-104          | `/api/v1/projects/{projectId}/transfer-ownership`   | POST        | 프로젝트 소유권 양도     |     X      |
| 161 | project | PROJECT-105          | `/api/v1/projects/{projectId}/part-quotas`          | PUT         | 파트별 정원 일괄 갱신    |     X      |
| 162 | project | PROJECT-106          | `/api/v1/projects/{projectId}/application-form`     | PUT         | 지원 폼 저장         |     X      |
| 163 | project | PROJECT-106-GET      | `/api/v1/projects/{projectId}/application-form`     | GET         | 지원 폼 조회         |     X      |
| 164 | project | PROJECT-107          | `/api/v1/projects/{projectId}/submit`               | POST        | 프로젝트 제출         |     X      |
| 165 | project | PROJECT-108          | `/api/v1/projects/{projectId}/publish`              | POST        | 프로젝트 공개         |     X      |
| 166 | project | PROJECT-MATCHING-001 | `/api/v1/project/matching-rounds`                   | GET         | 매칭 차수 목록 조회     |     X      |
| 167 | project | PROJECT-MATCHING-101 | `/api/v1/project/matching-rounds`                   | POST        | 매칭 차수 생성        |     X      |
| 168 | project | PROJECT-MATCHING-102 | `/api/v1/project/matching-rounds/{matchingRoundId}` | PATCH       | 매칭 차수 수정        |     X      |
| 169 | project | PROJECT-MATCHING-103 | `/api/v1/project/matching-rounds/{matchingRoundId}` | DELETE      | 매칭 차수 삭제        |     X      |

## schedule

|  순번 | 도메인      | API ID        | Endpoint                                             | HTTP Method | 역할                      | Deprecated |
|----:|----------|---------------|------------------------------------------------------|-------------|-------------------------|:----------:|
| 170 | schedule | SCHEDULE-C001 | `/api/v2/schedules`                                  | POST        | 일정 생성                   |     X      |
| 171 | schedule | SCHEDULE-C002 | `/api/v2/schedules/{scheduleId}`                     | PATCH       | 일정 수정                   |     X      |
| 172 | schedule | SCHEDULE-C003 | `/api/v2/schedules/{scheduleId}/attendances/request` | POST        | 출석 요청하기                 |     X      |
| 173 | schedule | SCHEDULE-C004 | `/api/v2/schedules/{scheduleId}/attendances/excuse`  | POST        | 출석 요청이 불가능한 경우, 사유 제출하기 |     X      |
| 174 | schedule | SCHEDULE-C005 | `/api/v2/schedules/{scheduleId}/attendances/decide`  | POST        | [운영진용] 출석 요청 승인/거절      |     X      |
| 175 | schedule | SCHEDULE-Q001 | `/api/v2/schedules/capabilities`                     | GET         | 일정 생성, 수정 관련 권한 조회      |     X      |
| 176 | schedule | SCHEDULE-Q002 | `/api/v2/schedules/me`                               | GET         | 내 일정 조회                 |     X      |
| 177 | schedule | SCHEDULE-Q003 | `/api/v2/schedules/{scheduleId}`                     | GET         | 일정 상세 조회                |     X      |
| 178 | schedule | SCHEDULE-Q004 | `/api/v2/schedules/attendance`                       | GET         | [운영진용] 일정들의 출석 현황 조회    |     X      |
| 179 | schedule | SCHEDULE-Q005 | `/api/v2/schedules/{scheduleId}/attendance`          | GET         | [운영진용] 단일 일정 출석 현황 조회   |     X      |

## storage

|  순번 | 도메인     | API ID      | Endpoint                           | HTTP Method | 역할                       | Deprecated |
|----:|---------|-------------|------------------------------------|-------------|--------------------------|:----------:|
| 180 | storage | STORAGE-001 | `/api/v1/storage/prepare-upload`   | POST        | 파일 업로드를 위한 Signed URL 생성 |     X      |
| 181 | storage | STORAGE-002 | `/api/v1/storage/{fileId}/confirm` | POST        | 파일 업로드 완료 처리             |     X      |
| 182 | storage | STORAGE-003 | `/api/v1/storage/{fileId}`         | DELETE      | 파일 삭제                    |     X      |

## term

|  순번 | 도메인  | API ID   | Endpoint                        | HTTP Method | 역할            | Deprecated |
|----:|------|----------|---------------------------------|-------------|---------------|:----------:|
| 183 | term | TERM-001 | `/api/v1/terms`                 | POST        | 약관 생성         |     X      |
| 184 | term | TERM-101 | `/api/v1/terms/type/{termType}` | GET         | 약관 유형으로 약관 조회 |     X      |
| 185 | term | TERM-102 | `/api/v1/terms/{termsId}`       | GET         | 약관 ID로 약관 조회  |     X      |

## test (개발/로컬 환경 한정)

> `@Profile("local | dev")` 적용으로 운영 환경에서는 노출되지 않습니다.

|  순번 | 도메인  | API ID   | Endpoint                     | HTTP Method | 역할                          | Deprecated |
|----:|------|----------|------------------------------|-------------|-----------------------------|:----------:|
| 186 | test | TEST-001 | `/test/file/{fileId}`        | GET         | [개발용] 파일 ID 기반 접근 가능 URL 조회 |     X      |
| 187 | test | TEST-002 | `/test/fcm/test-send`        | POST        | FCM 푸시 알림 테스트 전송            |     X      |
| 188 | test | TEST-003 | `/test/webhook/aop-test`     | GET         | AOP로 전송하는 알람 테스트            |     X      |
| 189 | test | TEST-004 | `/test/webhook/alarm`        | POST        | 웹훅 알람 전송 테스트                |     X      |
| 190 | test | TEST-005 | `/test/webhook/alarm/buffer` | POST        | 웹훅 알람 버퍼 전송 테스트             |     X      |
| 191 | test | TEST-006 | `/test/apple-client-secret`  | GET         | Apple Client Secret 생성      |     X      |
| 192 | test | TEST-007 | `/test/token/access`         | GET         | AccessToken 발급              |     X      |
| 193 | test | TEST-008 | `/test/token/refresh`        | GET         | RefreshToken 발급             |     X      |
| 194 | test | TEST-009 | `/test/token/email`          | GET         | EmailVerificationToken 발급   |     X      |
| 195 | test | TEST-010 | `/test/token/oauth`          | GET         | oAuthVerificationToken 발급   |     X      |
| 196 | test | TEST-011 | `/test/health-check`         | GET         | 헬스 체크 API                   |     X      |
| 197 | test | TEST-012 | `/test/check-authenticated`  | GET         | 인증된 사용자인지 여부 확인             |     X      |

## 통계

- **총 API 수**: 197개
- **도메인 수**: 15개 (test 포함)
- **Deprecated 처리된 API**: 7개
- **HTTP 미매핑 API**: 1개 (FCM-003)
