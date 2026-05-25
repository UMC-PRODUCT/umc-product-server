# ErrorCode Catalog

서버가 반환하는 ErrorCode를 도메인, HTTP 상태, 코드, 메시지 기준으로 정리합니다.

> 소스 기준: 각 도메인의 `*ErrorCode.java` enum을 스캔합니다. 갱신: `./gradlew generateDocumentationCatalogs`

## analytics

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 1 | analytics | `ANALYTICS-0001` | 403 FORBIDDEN | 운영진 대시보드에 접근할 권한이 없습니다. | AnalyticsErrorCode | `RESOURCE_ACCESS_DENIED` | `src/main/java/com/umc/product/analytics/domain/AnalyticsErrorCode.java:11` |
| 2 | analytics | `ANALYTICS-0002` | 400 BAD_REQUEST | 지원하지 않는 정렬 조건입니다. | AnalyticsErrorCode | `INVALID_SORT` | `src/main/java/com/umc/product/analytics/domain/AnalyticsErrorCode.java:12` |
| 3 | analytics | `ANALYTICS-0003` | 400 BAD_REQUEST | 조회 시작 시각은 종료 시각보다 빨라야 합니다. | AnalyticsErrorCode | `INVALID_PERIOD` | `src/main/java/com/umc/product/analytics/domain/AnalyticsErrorCode.java:13` |

## authentication

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 4 | authentication | `AUTHENTICATION-0001` | 400 BAD_REQUEST | 지원하지 않은 OAuth 제공자입니다. | AuthenticationErrorCode | `OAUTH_PROVIDER_NOT_FOUND` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:20` |
| 5 | authentication | `AUTHENTICATION-0002` | 404 NOT_FOUND | 제공된 OAuth Provider와 ProviderId와 일치하는 회원이 존재하지 않습니다. | AuthenticationErrorCode | `NO_MATCHING_MEMBER` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:21` |
| 6 | authentication | `AUTHENTICATION-0003` | 400 BAD_REQUEST | 잘못된 이메일 요청 인증입니다. | AuthenticationErrorCode | `NO_EMAIL_VERIFICATION_METHOD_GIVEN` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:23` |
| 7 | authentication | `AUTHENTICATION-0004` | 401 UNAUTHORIZED | 이메일 인증 정보가 일치하지 않습니다. | AuthenticationErrorCode | `INVALID_EMAIL_VERIFICATION` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:25` |
| 8 | authentication | `AUTHENTICATION-0006` | 404 NOT_FOUND | OAuth 인증은 성공하였으나, 가입된 회원이 없습니다. oAuthVerificationToken을 확인하세요. | AuthenticationErrorCode | `OAUTH_SUCCESS_BUT_NO_MEMBER` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:29` |
| 9 | authentication | `AUTHENTICATION-0007` | 503 SERVICE_UNAVAILABLE | OAuth 인증은 성공하였으나, 필요한 사용자 정보를 제공받지 못했습니다. 관리자에게 문의하세요. | AuthenticationErrorCode | `OAUTH_SUCCESS_BUT_NO_INFO` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:31` |
| 10 | authentication | `AUTHENTICATION-0008` | 400 BAD_REQUEST | OAuth 인증에 실패하였습니다. | AuthenticationErrorCode | `OAUTH_FAILURE` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:33` |
| 11 | authentication | `AUTHENTICATION-0009` | 400 BAD_REQUEST | 유효하지 않은 OAuth측 AccessToken 입니다. | AuthenticationErrorCode | `OAUTH_INVALID_ACCESS_TOKEN` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:34` |
| 12 | authentication | `AUTHENTICATION-0010` | 401 UNAUTHORIZED | OAuth 토큰 검증에 실패하였습니다. 관리자에게 문의하세요. | AuthenticationErrorCode | `OAUTH_TOKEN_VERIFICATION_FAILED` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:35` |
| 13 | authentication | `AUTHENTICATION-0011` | 401 UNAUTHORIZED | 유효하지 않은 OAuth 토큰입니다. | AuthenticationErrorCode | `INVALID_OAUTH_TOKEN` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:37` |
| 14 | authentication | `AUTHENTICATION-0012` | 401 UNAUTHORIZED | 이미 다른 계정에 연동된 OAuth 계정입니다. | AuthenticationErrorCode | `OAUTH_ALREADY_LINKED` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:38` |
| 15 | authentication | `AUTHENTICATION-0013` | 401 UNAUTHORIZED | 해당 계정에 이미 연동된 OAuth 제공자입니다. 기존에 연결된 계정을 해제하고 다시 시도해주세요. | AuthenticationErrorCode | `OAUTH_PROVIDER_ALREADY_LINKED` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:39` |
| 16 | authentication | `AUTHENTICATION-0014` | 404 NOT_FOUND | 해당하는 Member OAuth가 존재하지 않습니다. | AuthenticationErrorCode | `MEMBER_OAUTH_NOT_FOUND` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:41` |
| 17 | authentication | `AUTHENTICATION-0015` | 403 FORBIDDEN | 해당 작업을 할 권한이 없는 사용자입니다. | AuthenticationErrorCode | `NOT_VALID_MEMBER` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:42` |
| 18 | authentication | `AUTHENTICATION-0016` | 400 BAD_REQUEST | 게정과 연동된 유일한 OAuth는 연동 해제할 수 없습니다. 회원 탈퇴를 이용해주세요. | AuthenticationErrorCode | `OAUTH_CANNOT_UNLINK_LAST_PROVIDER` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:43` |
| 19 | authentication | `AUTHENTICATION-0017` | 400 BAD_REQUEST | 이미 인증이 완료된 이메일 인증 세션입니다. | AuthenticationErrorCode | `ALREADY_VERIFIED_EMAIL` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:47` |
| 20 | authentication | `AUTHENTICATION-0018` | 400 BAD_REQUEST | 만료된 이메일 인증 세션입니다. 새로운 인증을 요청해주세요. | AuthenticationErrorCode | `EMAIL_VERIFICATION_SESSION_EXPIRED` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:49` |
| 21 | authentication | `AUTHENTICATION-0019` | 409 CONFLICT | 이미 사용 중인 로그인 ID입니다. | AuthenticationErrorCode | `LOGIN_ID_ALREADY_EXISTS` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:55` |
| 22 | authentication | `AUTHENTICATION-0020` | 400 BAD_REQUEST | 로그인 ID 형식이 올바르지 않습니다. 영문/숫자/._- 5~20자로 입력해주세요. | AuthenticationErrorCode | `INVALID_LOGIN_ID_FORMAT` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:56` |
| 23 | authentication | `AUTHENTICATION-0021` | 400 BAD_REQUEST | 비밀번호는 8~64자이며 영문/숫자/특수문자 중 2종류 이상을 포함해야 합니다. | AuthenticationErrorCode | `PASSWORD_POLICY_VIOLATION` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:58` |
| 24 | authentication | `AUTHENTICATION-0022` | 401 UNAUTHORIZED | 로그인 ID 또는 비밀번호가 올바르지 않습니다. | AuthenticationErrorCode | `INVALID_LOGIN_CREDENTIAL` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:61` |
| 25 | authentication | `AUTHENTICATION-0023` | 400 BAD_REQUEST | 해당 OAuth Provider는 요청한 인증 흐름을 지원하지 않습니다. | AuthenticationErrorCode | `UNSUPPORTED_OAUTH_FLOW` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:69` |
| 26 | authentication | `AUTHENTICATION-0024` | 400 BAD_REQUEST | 허용되지 않은 OAuth redirect URI입니다. | AuthenticationErrorCode | `INVALID_OAUTH_REDIRECT_URI` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:71` |
| 27 | authentication | `AUTHENTICATION-0025` | 400 BAD_REQUEST | 이메일 형식이 올바르지 않습니다. | AuthenticationErrorCode | `INVALID_EMAIL_FORMAT` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:63` |
| 28 | authentication | `AUTHENTICATION-0026` | 409 CONFLICT | 이미 사용 중인 이메일입니다. | AuthenticationErrorCode | `EMAIL_ALREADY_EXISTS` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:65` |
| 29 | authentication | `AUTHENTICATION-0027` | 429 TOO_MANY_REQUESTS | 이메일 인증 발송 빈도가 너무 잦습니다. 잠시 후 다시 시도해주세요. | AuthenticationErrorCode | `EMAIL_VERIFICATION_THROTTLED` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:51` |
| 30 | authentication | `JWT-0001` | 401 UNAUTHORIZED | JWT 토큰의 서명이 잘못되었습니다. | AuthenticationErrorCode | `WRONG_JWT_SIGNATURE` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:14` |
| 31 | authentication | `JWT-0002` | 401 UNAUTHORIZED | 만료된 JWT 토큰입니다. | AuthenticationErrorCode | `EXPIRED_JWT_TOKEN` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:15` |
| 32 | authentication | `JWT-0003` | 401 UNAUTHORIZED | 지원하지 않는 JWT 토큰입니다. | AuthenticationErrorCode | `UNSUPPORTED_JWT` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:16` |
| 33 | authentication | `JWT-0004` | 401 UNAUTHORIZED | 잘못된 JWT 토큰입니다. | AuthenticationErrorCode | `INVALID_JWT` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:17` |

## authorization

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 34 | authorization | `AUTHORIZATION-0001` | 403 FORBIDDEN | 권한이 없습니다. | AuthorizationErrorCode | `PERMISSION_DENIED` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:12` |
| 35 | authorization | `AUTHORIZATION-0002` | 403 FORBIDDEN | 해당 리소스에 접근할 권한이 없습니다. | AuthorizationErrorCode | `RESOURCE_ACCESS_DENIED` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:13` |
| 36 | authorization | `AUTHORIZATION-0003` | 400 BAD_REQUEST | 유효하지 않은 권한입니다. | AuthorizationErrorCode | `INVALID_PERMISSION` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:14` |
| 37 | authorization | `AUTHORIZATION-0004` | 500 INTERNAL_SERVER_ERROR | 권한 검증 중 오류가 발생했습니다. | AuthorizationErrorCode | `POLICY_EVALUATION_FAILED` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:15` |
| 38 | authorization | `AUTHORIZATION-0005` | 500 INTERNAL_SERVER_ERROR | 해당 리소스 타입에 해당하는 Permission Evaluator가 존재하지 않습니다. 관리자에게 문의하세요. | AuthorizationErrorCode | `NO_EVALUATOR_MATCHING_RESOURCE_TYPE` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:16` |
| 39 | authorization | `AUTHORIZATION-0006` | 500 INTERNAL_SERVER_ERROR | 리소스 유형에서 지원하지 않는 권한 유형을 검사하고자 시도하였습니다. 관리자에게 문의하세요. | AuthorizationErrorCode | `PERMISSION_TYPE_NOT_SUPPORTED_BY_RESOURCE_TYPE` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:18` |
| 40 | authorization | `AUTHORIZATION-0007` | 400 BAD_REQUEST | 잘못된 권한 확인입니다. | AuthorizationErrorCode | `INVALID_INPUT_VALUE` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:20` |
| 41 | authorization | `AUTHORIZATION-0008` | 400 BAD_REQUEST | 권한 평가를 위한 리소스 ID 타입이 잘못 전달되었습니다. 관리자에게 문의하세요. | AuthorizationErrorCode | `INVALID_RESOURCE_ID_TYPE` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:21` |
| 42 | authorization | `AUTHORIZATION-0009` | 500 INTERNAL_SERVER_ERROR | 잘못된 권한 평가 요청입니다. 관리자에게 문의하세요. | AuthorizationErrorCode | `INVALID_RESOURCE_PERMISSION_GIVEN` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:23` |
| 43 | authorization | `AUTHORIZATION-0010` | 404 NOT_FOUND | 해당하는 역할을 찾을 수 없습니다. | AuthorizationErrorCode | `CHALLENGER_ROLE_NOT_FOUND` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:25` |
| 44 | authorization | `AUTHORIZATION-0011` | 501 NOT_IMPLEMENTED | ResourceType이 지원하는 PermissionType에 대해서 PermissionEvaluator가 구현을 하지 않았습니다. 관리자에게 문의하세요. | AuthorizationErrorCode | `PERMISSION_TYPE_NOT_IMPLEMENTED` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:26` |

## challenger

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 45 | challenger | `CHALLENGER-0001` | 404 NOT_FOUND | 사용자를 찾을 수 없습니다. | ChallengerErrorCode | `CHALLENGER_NOT_FOUND` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:12` |
| 46 | challenger | `CHALLENGER-0002` | 409 CONFLICT | 이미 등록된 사용자입니다. | ChallengerErrorCode | `CHALLENGER_ALREADY_EXISTS` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:13` |
| 47 | challenger | `CHALLENGER-0003` | 400 BAD_REQUEST | 이미 탈퇴한 사용자입니다. | ChallengerErrorCode | `CHALLENGER_ALREADY_WITHDRAWN` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:14` |
| 48 | challenger | `CHALLENGER-0004` | 400 BAD_REQUEST | 올바르지 않은 사용자 상태입니다. | ChallengerErrorCode | `INVALID_CHALLENGER_STATUS` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:15` |
| 49 | challenger | `CHALLENGER-0005` | 400 BAD_REQUEST | 유효한 챌린저가 아닙니다. | ChallengerErrorCode | `CHALLENGER_NOT_ACTIVE` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:16` |
| 50 | challenger | `CHALLENGER-0007` | 404 NOT_FOUND | 상벌점 기록을 찾을 수 없습니다. | ChallengerErrorCode | `CHALLENGER_POINT_NOT_FOUND` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:17` |
| 51 | challenger | `CHALLENGER-0008` | 404 NOT_FOUND | 잘못된 챌린저 업데이트 요청입니다. | ChallengerErrorCode | `BAD_CHALLENGER_UPDATE_REQUEST` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:18` |
| 52 | challenger | `CHALLENGER-0009` | 400 BAD_REQUEST | 활성 또는 수료 상태의 사용자만 일정 생성이 가능합니다. | ChallengerErrorCode | `NOT_ALLOWED_AUTHOR` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:19` |
| 53 | challenger | `CHALLENGER-0010` | 404 NOT_FOUND | 챌린저에 연결된 멤버 프로필을 찾을 수 없습니다. | ChallengerErrorCode | `MEMBER_PROFILE_NOT_FOUND` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:20` |
| 54 | challenger | `CHALLENGER-0011` | 400 BAD_REQUEST | 유효하지 않은 커서 ID입니다. | ChallengerErrorCode | `INVALID_CURSOR_ID` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:21` |
| 55 | challenger | `CHALLENGER-0012` | 400 BAD_REQUEST | 이미 사용된 챌린저 기록 추가용 코드입니다. | ChallengerErrorCode | `USED_CHALLENGER_RECORD_CODE` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:22` |
| 56 | challenger | `CHALLENGER-0013` | 400 BAD_REQUEST | 코드에 등록된 사용자 이름이 요청자와 일치하지 않습니다. | ChallengerErrorCode | `INVALID_MEMBER_NAME_FOR_RECORD` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:23` |
| 57 | challenger | `CHALLENGER-0014` | 400 BAD_REQUEST | 코드에 등록된 학교가 요청자 소속과 일치하지 않습니다. | ChallengerErrorCode | `INVALID_SCHOOL_FOR_RECORD` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:24` |
| 58 | challenger | `CHALLENGER-0015` | 400 BAD_REQUEST | 제공된 정보로 챌린저 기록을 생성할 수 없습니다. | ChallengerErrorCode | `INVALID_CHALLENGER_RECORD_CREATE_REQUEST` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:25` |
| 59 | challenger | `CHALLENGER-0016` | 404 NOT_FOUND | 해당 회원은 주어진 기수에 활동한 챌린저가 아닙니다. | ChallengerErrorCode | `NO_CHALLENGER_IN_MEMBER_GISU` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:26` |
| 60 | challenger | `CHALLENGER-0017` | 404 NOT_FOUND | 해당 Challenger의 Part는 존재하지 않습니다. | ChallengerErrorCode | `CHALLENGER_PART_NOT_FOUND` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:27` |

## community

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 61 | community | `COMMUNITY-0001` | 404 NOT_FOUND | 게시글을 찾을 수 없습니다. | CommunityErrorCode | `POST_NOT_FOUND` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:12` |
| 62 | community | `COMMUNITY-0002` | 404 NOT_FOUND | 댓글을 찾을 수 없습니다. | CommunityErrorCode | `COMMENT_NOT_FOUND` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:13` |
| 63 | community | `COMMUNITY-0003` | 404 NOT_FOUND | 상장을 찾을 수 없습니다. | CommunityErrorCode | `TROPHY_NOT_FOUND` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:14` |
| 64 | community | `COMMUNITY-0004` | 400 BAD_REQUEST | 게시글 제목이 유효하지 않습니다. | CommunityErrorCode | `INVALID_POST_TITLE` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:16` |
| 65 | community | `COMMUNITY-0005` | 400 BAD_REQUEST | 게시글 내용이 유효하지 않습니다. | CommunityErrorCode | `INVALID_POST_CONTENT` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:17` |
| 66 | community | `COMMUNITY-0006` | 400 BAD_REQUEST | 게시글 카테고리가 유효하지 않습니다. | CommunityErrorCode | `INVALID_POST_CATEGORY` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:18` |
| 67 | community | `COMMUNITY-0007` | 400 BAD_REQUEST | 게시글 지역이 유효하지 않습니다. | CommunityErrorCode | `INVALID_POST_REGION` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:19` |
| 68 | community | `COMMUNITY-0008` | 400 BAD_REQUEST | 일반 게시글을 번개 게시글로 변경할 수 없습니다. | CommunityErrorCode | `CANNOT_CHANGE_TO_LIGHTNING` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:20` |
| 69 | community | `COMMUNITY-0009` | 400 BAD_REQUEST | 번개 게시글을 일반 게시글로 변경할 수 없습니다. | CommunityErrorCode | `CANNOT_CHANGE_FROM_LIGHTNING` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:21` |
| 70 | community | `COMMUNITY-0010` | 400 BAD_REQUEST | 댓글 내용이 유효하지 않습니다. | CommunityErrorCode | `INVALID_COMMENT_CONTENT` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:23` |
| 71 | community | `COMMUNITY-0011` | 403 FORBIDDEN | 본인의 댓글만 삭제할 수 있습니다. | CommunityErrorCode | `COMMENT_NOT_OWNED` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:24` |
| 72 | community | `COMMUNITY-0012` | 400 BAD_REQUEST | 상장 주차가 유효하지 않습니다. | CommunityErrorCode | `INVALID_TROPHY_WEEK` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:26` |
| 73 | community | `COMMUNITY-0013` | 400 BAD_REQUEST | 상장 제목이 유효하지 않습니다. | CommunityErrorCode | `INVALID_TROPHY_TITLE` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:27` |
| 74 | community | `COMMUNITY-0014` | 400 BAD_REQUEST | 상장 내용이 유효하지 않습니다. | CommunityErrorCode | `INVALID_TROPHY_CONTENT` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:28` |
| 75 | community | `COMMUNITY-0015` | 400 BAD_REQUEST | 상장 URL이 유효하지 않습니다. | CommunityErrorCode | `INVALID_TROPHY_URL` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:29` |
| 76 | community | `COMMUNITY-0016` | 409 CONFLICT | 이미 신고한 게시글/댓글입니다. | CommunityErrorCode | `REPORT_ALREADY_EXISTS` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:31` |
| 77 | community | `COMMUNITY-0017` | 400 BAD_REQUEST | 작성자 ID는 필수입니다. | CommunityErrorCode | `INVALID_POST_AUTHOR` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:34` |
| 78 | community | `COMMUNITY-0018` | 400 BAD_REQUEST | 번개 게시글이 아닙니다. | CommunityErrorCode | `NOT_LIGHTNING_POST` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:35` |
| 79 | community | `COMMUNITY-0019` | 400 BAD_REQUEST | 번개 게시글은 번개 전용 API를 사용하세요. | CommunityErrorCode | `USE_LIGHTNING_API` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:36` |
| 80 | community | `COMMUNITY-0020` | 400 BAD_REQUEST | 번개 게시글은 추가 정보가 필수입니다. | CommunityErrorCode | `LIGHTNING_INFO_REQUIRED` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:37` |
| 81 | community | `COMMUNITY-0021` | 403 FORBIDDEN | 본인의 게시글만 수정/삭제할 수 있습니다. | CommunityErrorCode | `POST_NOT_OWNED` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:38` |
| 82 | community | `COMMUNITY-0022` | 400 BAD_REQUEST | 모임 시간은 필수입니다. | CommunityErrorCode | `INVALID_LIGHTNING_MEET_AT` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:41` |
| 83 | community | `COMMUNITY-0023` | 400 BAD_REQUEST | 모임 장소는 필수입니다. | CommunityErrorCode | `INVALID_LIGHTNING_LOCATION` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:42` |
| 84 | community | `COMMUNITY-0024` | 400 BAD_REQUEST | 최대 참가자는 1명 이상이어야 합니다. | CommunityErrorCode | `INVALID_LIGHTNING_MAX_PARTICIPANTS` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:43` |
| 85 | community | `COMMUNITY-0025` | 400 BAD_REQUEST | 오픈 채팅 링크는 필수입니다. | CommunityErrorCode | `INVALID_LIGHTNING_OPEN_CHAT_URL` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:44` |
| 86 | community | `COMMUNITY-0026` | 400 BAD_REQUEST | 오픈 채팅 링크는 http:// 또는 https://로 시작해야 합니다. | CommunityErrorCode | `INVALID_LIGHTNING_OPEN_CHAT_URL_FORMAT` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:45` |
| 87 | community | `COMMUNITY-0027` | 400 BAD_REQUEST | 모임 시간은 현재 이후여야 합니다. | CommunityErrorCode | `INVALID_LIGHTNING_MEET_AT_PAST` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:46` |
| 88 | community | `COMMUNITY-0028` | 400 BAD_REQUEST | 게시글 ID는 필수입니다. | CommunityErrorCode | `INVALID_COMMENT_POST_ID` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:49` |
| 89 | community | `COMMUNITY-0029` | 400 BAD_REQUEST | 챌린저 ID는 필수입니다. | CommunityErrorCode | `INVALID_COMMENT_CHALLENGER_ID` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:50` |
| 90 | community | `COMMUNITY-0030` | 400 BAD_REQUEST | ID는 양수여야 합니다. | CommunityErrorCode | `INVALID_ID` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:53` |
| 91 | community | `COMMUNITY-0031` | 400 BAD_REQUEST | 새 게시글 생성 시에는 작성자 정보가 필요합니다. | CommunityErrorCode | `POST_SAVE_REQUIRES_AUTHOR` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:56` |
| 92 | community | `COMMUNITY-0032` | 400 BAD_REQUEST | 이미 ID가 있는 게시글은 update용 save를 사용하세요. | CommunityErrorCode | `POST_UPDATE_INVALID_CALL` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:57` |

## curriculum

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 93 | curriculum | `CURRICULUM-0001` | 404 NOT_FOUND | 커리큘럼을 찾을 수 없습니다. | CurriculumErrorCode | `CURRICULUM_NOT_FOUND` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:12` |
| 94 | curriculum | `CURRICULUM-0002` | 404 NOT_FOUND | 워크북을 찾을 수 없습니다. | CurriculumErrorCode | `WORKBOOK_NOT_FOUND` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:13` |
| 95 | curriculum | `CURRICULUM-0003` | 404 NOT_FOUND | 미션을 찾을 수 없습니다. | CurriculumErrorCode | `MISSION_NOT_FOUND` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:14` |
| 96 | curriculum | `CURRICULUM-0004` | 409 CONFLICT | 제출된 워크북이 있어 삭제할 수 없습니다. | CurriculumErrorCode | `WORKBOOK_HAS_SUBMISSIONS` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:15` |
| 97 | curriculum | `CURRICULUM-0005` | 404 NOT_FOUND | 커리큘럼에 해당 워크북이 존재하지 않습니다. | CurriculumErrorCode | `WORKBOOK_NOT_IN_CURRICULUM` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:16` |
| 98 | curriculum | `CURRICULUM-0006` | 404 NOT_FOUND | 챌린저 워크북을 찾을 수 없습니다. | CurriculumErrorCode | `CHALLENGER_WORKBOOK_NOT_FOUND` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:17` |
| 99 | curriculum | `CURRICULUM-0007` | 400 BAD_REQUEST | 제출 내용이 필요합니다. | CurriculumErrorCode | `SUBMISSION_REQUIRED` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:18` |
| 100 | curriculum | `CURRICULUM-0008` | 400 BAD_REQUEST | 워크북 상태가 유효하지 않습니다. | CurriculumErrorCode | `INVALID_WORKBOOK_STATUS` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:19` |
| 101 | curriculum | `CURRICULUM-0009` | 409 CONFLICT | 이미 해당 주차의 워크북 미션을 제출하였습니다. | CurriculumErrorCode | `WORKBOOK_SUBMISSION_ALREADY_EXISTS` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:20` |
| 102 | curriculum | `CURRICULUM-0010` | 409 CONFLICT | 해당 기수와 파트의 커리큘럼이 이미 존재합니다. | CurriculumErrorCode | `CURRICULUM_ALREADY_EXISTS` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:21` |
| 103 | curriculum | `CURRICULUM-0011` | 403 FORBIDDEN | 해당 워크북에 대한 접근 권한이 없습니다. | CurriculumErrorCode | `WORKBOOK_ACCESS_DENIED` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:22` |
| 104 | curriculum | `CURRICULUM-0012` | 400 BAD_REQUEST | 주차 커리큘럼의 시작일이 종료일보다 늦을 수 없습니다. | CurriculumErrorCode | `INVALID_WEEKLY_CURRICULUM_PERIOD` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:23` |
| 105 | curriculum | `CURRICULUM-0013` | 400 BAD_REQUEST | 유효하지 않은 워크북 상태 변경 요청입니다. | CurriculumErrorCode | `INVALID_WORKBOOK_STATUS_TRANSITION` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:24` |
| 106 | curriculum | `CURRICULUM-0014` | 404 NOT_FOUND | 주차별 커리큘럼을 찾을 수 없습니다. | CurriculumErrorCode | `WEEKLY_CURRICULUM_NOT_FOUND` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:25` |
| 107 | curriculum | `CURRICULUM-0015` | 409 CONFLICT | 주차별 커리큘럼이 존재하여 커리큘럼을 삭제할 수 없습니다. | CurriculumErrorCode | `CURRICULUM_HAS_WEEKLY_CURRICULUMS` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:26` |
| 108 | curriculum | `CURRICULUM-0016` | 409 CONFLICT | 원본 워크북이 존재하여 주차별 커리큘럼을 삭제할 수 없습니다. | CurriculumErrorCode | `WEEKLY_CURRICULUM_HAS_WORKBOOKS` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:27` |
| 109 | curriculum | `CURRICULUM-0017` | 409 CONFLICT | 배포된 워크북이 존재하여 주차 기간을 수정할 수 없습니다. | CurriculumErrorCode | `WEEKLY_CURRICULUM_DATE_LOCKED` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:28` |
| 110 | curriculum | `CURRICULUM-0018` | 409 CONFLICT | 이미 동일한 주차와 부록 여부를 가진 주차별 커리큘럼이 존재합니다. | CurriculumErrorCode | `WEEKLY_CURRICULUM_ALREADY_EXISTS` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:29` |
| 111 | curriculum | `CURRICULUM-0019` | 400 BAD_REQUEST | 이미 종료된 기간으로 주차별 커리큘럼을 생성하거나 수정할 수 없습니다. | CurriculumErrorCode | `WEEKLY_CURRICULUM_PERIOD_ALREADY_ENDED` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:30` |

## figma

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 112 | figma | `FIGMA-0001` | 404 NOT_FOUND | Figma OAuth 통합 정보가 등록되어 있지 않습니다. | FigmaErrorCode | `INTEGRATION_NOT_FOUND` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:12` |
| 113 | figma | `FIGMA-0002` | 502 BAD_GATEWAY | Figma OAuth 토큰 교환에 실패했습니다. | FigmaErrorCode | `OAUTH_TOKEN_EXCHANGE_FAILED` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:13` |
| 114 | figma | `FIGMA-0003` | 502 BAD_GATEWAY | Figma access token 갱신에 실패했습니다. | FigmaErrorCode | `OAUTH_TOKEN_REFRESH_FAILED` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:14` |
| 115 | figma | `FIGMA-0004` | 502 BAD_GATEWAY | Figma 댓글 조회에 실패했습니다. | FigmaErrorCode | `COMMENT_FETCH_FAILED` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:15` |
| 116 | figma | `FIGMA-0005` | 502 BAD_GATEWAY | Figma 파일 메타데이터 조회에 실패했습니다. | FigmaErrorCode | `FILE_METADATA_FETCH_FAILED` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:16` |
| 117 | figma | `FIGMA-0006` | 404 NOT_FOUND | 등록된 Figma 폴링 대상 파일이 아닙니다. | FigmaErrorCode | `WATCHED_FILE_NOT_FOUND` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:17` |
| 118 | figma | `FIGMA-0007` | 409 CONFLICT | 이미 등록된 Figma 파일 키 입니다. | FigmaErrorCode | `WATCHED_FILE_ALREADY_EXISTS` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:18` |
| 119 | figma | `FIGMA-0008` | 400 BAD_REQUEST | Figma OAuth state 값이 일치하지 않습니다. | FigmaErrorCode | `OAUTH_STATE_MISMATCH` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:19` |
| 120 | figma | `FIGMA-0009` | 500 INTERNAL_SERVER_ERROR | Figma 토큰 암복호화에 실패했습니다. | FigmaErrorCode | `TOKEN_ENCRYPTION_FAILED` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:20` |
| 121 | figma | `FIGMA-0010` | 502 BAD_GATEWAY | Discord 멘션 전송에 실패했습니다. | FigmaErrorCode | `DISCORD_MENTION_SEND_FAILED` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:21` |
| 122 | figma | `FIGMA-0013` | 404 NOT_FOUND | 등록된 Figma 라우팅 도메인이 아닙니다. | FigmaErrorCode | `ROUTING_DOMAIN_NOT_FOUND` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:22` |
| 123 | figma | `FIGMA-0014` | 409 CONFLICT | 동일한 domain_key 의 라우팅 도메인이 이미 등록되어 있습니다. | FigmaErrorCode | `ROUTING_DOMAIN_ALREADY_EXISTS` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:23` |
| 124 | figma | `FIGMA-0015` | 404 NOT_FOUND | 해당 라우팅 도메인의 mention 이 아닙니다. | FigmaErrorCode | `ROUTING_DOMAIN_MENTION_NOT_FOUND` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:24` |
| 125 | figma | `FIGMA-0016` | 412 PRECONDITION_FAILED | 라우팅 도메인이 한 건도 등록되어 있지 않습니다. | FigmaErrorCode | `ROUTING_DOMAIN_NOT_REGISTERED` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:25` |
| 126 | figma | `FIGMA-0017` | 400 BAD_REQUEST | digest 의 from/to 시간창이 유효하지 않습니다. | FigmaErrorCode | `DIGEST_RANGE_INVALID` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:26` |

## global

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 127 | global | `COMMON-0001` | 500 INTERNAL_SERVER_ERROR | 알 수 없는 오류입니다. 관리자에게 문의해주세요. | CommonErrorCode | `INTERNAL_SERVER_ERROR` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:24` |
| 128 | global | `COMMON-400` | 400 BAD_REQUEST | 잘못된 요청입니다. | CommonErrorCode | `BAD_REQUEST` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:26` |
| 129 | global | `COMMON-401` | 401 UNAUTHORIZED | 인증이 필요합니다. | CommonErrorCode | `UNAUTHORIZED` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:27` |
| 130 | global | `COMMON-403` | 403 FORBIDDEN | 허용되지 않는 요청입니다. | CommonErrorCode | `FORBIDDEN` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:28` |
| 131 | global | `COMMON-404` | 404 NOT_FOUND | 요청한 리소스를 찾을 수 없습니다. | CommonErrorCode | `NOT_FOUND` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:29` |
| 132 | global | `COMMON-501` | 501 NOT_IMPLEMENTED | 아직 구현되지 않은 기능입니다. 서버팀에게 문의해주세요. | CommonErrorCode | `NOT_IMPLEMENTED` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:30` |
| 133 | global | `ENV-0001` | 400 BAD_REQUEST | 현재 실행 환경에서는 사용할 수 없는 기능입니다. | CommonErrorCode | `INVALID_ENV` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:37` |
| 134 | global | `PE-0001` | 501 NOT_IMPLEMENTED | 요청하신 PE가 존재하지 않습니다. 관리자에게 문의해주세요. | CommonErrorCode | `PERMISSION_TYPE_NOT_IMPLEMENTED` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:40` |
| 135 | global | `SECURITY-0001` | 401 UNAUTHORIZED | 인증 정보가 전달되지 않았습니다. | CommonErrorCode | `SECURITY_NOT_GIVEN` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:33` |
| 136 | global | `SECURITY-0002` | 403 FORBIDDEN | 권한이 부족합니다. | CommonErrorCode | `SECURITY_FORBIDDEN` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:34` |

## llm

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 137 | llm | `LLM-0001` | 502 BAD_GATEWAY | LLM 호출에 실패했습니다. | LlmErrorCode | `CHAT_COMPLETION_FAILED` | `src/main/java/com/umc/product/llm/domain/exception/LlmErrorCode.java:12` |
| 138 | llm | `LLM-0002` | 502 BAD_GATEWAY | LLM 응답을 해석할 수 없습니다. | LlmErrorCode | `CHAT_COMPLETION_INVALID_RESPONSE` | `src/main/java/com/umc/product/llm/domain/exception/LlmErrorCode.java:13` |
| 139 | llm | `LLM-0003` | 500 INTERNAL_SERVER_ERROR | LLM provider 설정이 누락되었습니다. | LlmErrorCode | `PROVIDER_NOT_CONFIGURED` | `src/main/java/com/umc/product/llm/domain/exception/LlmErrorCode.java:14` |

## maintenance

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 140 | maintenance | `MAINTENANCE-0001` | 503 SERVICE_UNAVAILABLE | 서비스 점검 중입니다. | MaintenanceErrorCode | `SERVICE_UNDER_MAINTENANCE` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:12` |
| 141 | maintenance | `MAINTENANCE-0002` | 404 NOT_FOUND | 점검 윈도우를 찾을 수 없습니다. | MaintenanceErrorCode | `MAINTENANCE_WINDOW_NOT_FOUND` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:13` |
| 142 | maintenance | `MAINTENANCE-0003` | 400 BAD_REQUEST | 종료 시각은 시작 시각 이후여야 합니다. | MaintenanceErrorCode | `INVALID_TIME_RANGE` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:14` |
| 143 | maintenance | `MAINTENANCE-0004` | 400 BAD_REQUEST | 시작 시각은 현재 시각 이후여야 합니다. | MaintenanceErrorCode | `START_AT_IN_PAST` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:15` |
| 144 | maintenance | `MAINTENANCE-0005` | 400 BAD_REQUEST | PER_DOMAIN 점검은 대상 도메인을 1개 이상 지정해야 합니다. | MaintenanceErrorCode | `TARGET_DOMAINS_REQUIRED` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:16` |
| 145 | maintenance | `MAINTENANCE-0006` | 409 CONFLICT | 다른 점검 윈도우와 시간이 겹칩니다. | MaintenanceErrorCode | `OVERLAPPING_WINDOW` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:17` |
| 146 | maintenance | `MAINTENANCE-0007` | 400 BAD_REQUEST | 이미 종료된 점검 윈도우입니다. | MaintenanceErrorCode | `ALREADY_ENDED` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:18` |
| 147 | maintenance | `MAINTENANCE-0008` | 403 FORBIDDEN | 점검 관리 권한이 없습니다. | MaintenanceErrorCode | `NOT_SUPER_ADMIN` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:19` |

## member

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 148 | member | `MEMBER-0001` | 404 NOT_FOUND | 사용자를 찾을 수 없습니다. | MemberErrorCode | `MEMBER_NOT_FOUND` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:12` |
| 149 | member | `MEMBER-0002` | 409 CONFLICT | 이미 등록된 사용자입니다. | MemberErrorCode | `MEMBER_ALREADY_EXISTS` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:13` |
| 150 | member | `MEMBER-0003` | 409 CONFLICT | 이미 사용 중인 이메일입니다. | MemberErrorCode | `EMAIL_ALREADY_EXISTS` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:14` |
| 151 | member | `MEMBER-0004` | 400 BAD_REQUEST | 이미 탈퇴한 사용자입니다. | MemberErrorCode | `MEMBER_ALREADY_WITHDRAWN` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:15` |
| 152 | member | `MEMBER-0005` | 400 BAD_REQUEST | 올바르지 않은 사용자 상태입니다. | MemberErrorCode | `INVALID_MEMBER_STATUS` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:16` |
| 153 | member | `MEMBER-0006` | 400 BAD_REQUEST | 올바르지 않은 사용자 상태입니다. | MemberErrorCode | `MEMBER_NOT_ACTIVE` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:17` |
| 154 | member | `MEMBER-0007` | 400 BAD_REQUEST | 이미 회원가입을 완료한 사용자입니다. | MemberErrorCode | `MEMBER_ALREADY_REGISTERED` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:18` |
| 155 | member | `MEMBER-0008` | 404 NOT_FOUND | 프로필을 찾을 수 없습니다. | MemberErrorCode | `MEMBER_PROFILE_NOT_FOUND` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:19` |
| 156 | member | `MEMBER-0009` | 400 BAD_REQUEST | 학교가 등록되지 않은 사용자입니다. | MemberErrorCode | `MEMBER_SCHOOL_NOT_ASSIGNED` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:20` |
| 157 | member | `MEMBER-0010` | 409 CONFLICT | 이미 ID/PW 자격증명이 등록된 사용자입니다. | MemberErrorCode | `CREDENTIAL_ALREADY_REGISTERED` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:21` |
| 158 | member | `MEMBER-0011` | 400 BAD_REQUEST | ID/PW 자격증명이 등록되지 않은 사용자입니다. | MemberErrorCode | `CREDENTIAL_NOT_REGISTERED` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:22` |
| 159 | member | `MEMBER-0012` | 400 BAD_REQUEST | 올바르지 않은 로그인 아이디입니다. | MemberErrorCode | `INVALID_LOGIN_ID` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:23` |
| 160 | member | `MEMBER-0013` | 400 BAD_REQUEST | 올바르지 않은 비밀번호입니다. | MemberErrorCode | `INVALID_PASSWORD` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:24` |

## notice

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 161 | notice | `NOTICE-0001` | 404 NOT_FOUND | 공지사항을 찾을 수 없습니다. | NoticeErrorCode | `NOTICE_NOT_FOUND` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:12` |
| 162 | notice | `NOTICE-0002` | 400 BAD_REQUEST | 이미 게시된 공지사항입니다. | NoticeErrorCode | `ALREADY_PUBLISHED_NOTICE` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:13` |
| 163 | notice | `NOTICE-0003` | 400 BAD_REQUEST | 공지사항 제목이 유효하지 않습니다. | NoticeErrorCode | `INVALID_NOTICE_TITLE` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:14` |
| 164 | notice | `NOTICE-0004` | 400 BAD_REQUEST | 공지사항 내용이 유효하지 않습니다. | NoticeErrorCode | `INVALID_NOTICE_CONTENT` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:15` |
| 165 | notice | `NOTICE-0005` | 400 BAD_REQUEST | 공지사항 알림을 보낼 수 없는 상태입니다. | NoticeErrorCode | `INVALID_NOTICE_STATUS_FOR_REMINDER` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:16` |
| 166 | notice | `NOTICE-0006` | 400 BAD_REQUEST | 공지사항 작성자는 필수입니다. | NoticeErrorCode | `AUTHOR_REQUIRED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:17` |
| 167 | notice | `NOTICE-0007` | 400 BAD_REQUEST | 공지사항 대상 범위는 필수입니다. | NoticeErrorCode | `NOTICE_SCOPE_REQUIRED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:18` |
| 168 | notice | `NOTICE-0008` | 403 FORBIDDEN | 공지사항 작성자가 아닙니다. | NoticeErrorCode | `NOTICE_AUTHOR_MISMATCH` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:19` |
| 169 | notice | `NOTICE-0009` | 403 FORBIDDEN | 해당 공지사항을 작성할 권한이 없습니다. | NoticeErrorCode | `NO_WRITE_PERMISSION` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:20` |
| 170 | notice | `NOTICE-0010` | 400 BAD_REQUEST | 공지사항 수신자 설정이 잘못되었습니다. | NoticeErrorCode | `INVALID_TARGET_SETTING` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:22` |
| 171 | notice | `NOTICE-0011` | 404 NOT_FOUND | 해당 공지사항에 설정된 수신 대상이 존재하지 않습니다. | NoticeErrorCode | `NO_TARGET_FOUND` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:23` |
| 172 | notice | `NOTICE-0012` | 403 FORBIDDEN | 해당 공지사항을 조회할 권한이 없습니다. | NoticeErrorCode | `NO_READ_PERMISSION` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:21` |
| 173 | notice | `NOTICE-9999` | 501 NOT_IMPLEMENTED | 아직 구현되지 않은 기능입니다. | NoticeErrorCode | `NOT_IMPLEMENTED_YET` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:41` |
| 174 | notice | `NOTICE-CONTENTS-0001` | 400 BAD_REQUEST | 투표 ID 목록은 필수입니다. | NoticeErrorCode | `VOTE_IDS_REQUIRED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:26` |
| 175 | notice | `NOTICE-CONTENTS-0002` | 400 BAD_REQUEST | 이미지 URL 목록은 필수입니다. | NoticeErrorCode | `IMAGE_URLS_REQUIRED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:27` |
| 176 | notice | `NOTICE-CONTENTS-0003` | 400 BAD_REQUEST | 링크 URL 목록은 필수입니다. | NoticeErrorCode | `LINK_URLS_REQUIRED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:28` |
| 177 | notice | `NOTICE-CONTENTS-0004` | 404 NOT_FOUND | 공지사항 투표를 찾을 수 없습니다. | NoticeErrorCode | `NOTICE_VOTE_NOT_FOUND` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:29` |
| 178 | notice | `NOTICE-CONTENTS-0005` | 404 NOT_FOUND | 공지사항 이미지를 찾을 수 없습니다. | NoticeErrorCode | `NOTICE_IMAGE_NOT_FOUND` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:30` |
| 179 | notice | `NOTICE-CONTENTS-0006` | 404 NOT_FOUND | 공지사항 링크를 찾을 수 없습니다. | NoticeErrorCode | `NOTICE_LINK_NOT_FOUND` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:31` |
| 180 | notice | `NOTICE-CONTENTS-0007` | 400 BAD_REQUEST | 공지사항 이미지는 최대 10장까지 등록할 수 있습니다. | NoticeErrorCode | `IMAGE_LIMIT_EXCEEDED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:32` |
| 181 | notice | `NOTICE-CONTENTS-0008` | 409 CONFLICT | 해당 공지사항에 이미 투표가 존재합니다. | NoticeErrorCode | `VOTE_ALREADY_EXISTS` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:33` |
| 182 | notice | `NOTICE-CONTENTS-0009` | 400 BAD_REQUEST | 투표 항목은 2개 이상 5개 이하여야 합니다. | NoticeErrorCode | `INVALID_VOTE_OPTION_COUNT` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:34` |
| 183 | notice | `NOTICE-CONTENTS-0010` | 400 BAD_REQUEST | 투표 항목에 빈 값이 포함될 수 없습니다. | NoticeErrorCode | `INVALID_VOTE_OPTION_CONTENT` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:35` |
| 184 | notice | `NOTICE-CONTENTS-0011` | 400 BAD_REQUEST | 아직 투표 기간이 시작되지 않았습니다. | NoticeErrorCode | `VOTE_NOT_STARTED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:36` |
| 185 | notice | `NOTICE-CONTENTS-0012` | 400 BAD_REQUEST | 이미 종료된 투표입니다. | NoticeErrorCode | `VOTE_CLOSED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:37` |
| 186 | notice | `NOTICE-CONTENTS-0013` | 400 BAD_REQUEST | 선택한 투표 항목 ID 목록은 필수입니다. | NoticeErrorCode | `SELECTED_OPTION_IDS_REQUIRED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:38` |

## notification

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 187 | notification | `EMAIL-0001` | 400 BAD_REQUEST | 알 수 없는 사유로 이메일 전송에 실패했습니다. | EmailErrorCode | `EMAIL_GENERAL_ERROR` | `src/main/java/com/umc/product/notification/domain/exception/EmailErrorCode.java:11` |
| 188 | notification | `EMAIL-0002` | 400 BAD_REQUEST | 인코딩 과정에서 오류가 발생했습니다. | EmailErrorCode | `EMAIL_ENCODING_ERROR` | `src/main/java/com/umc/product/notification/domain/exception/EmailErrorCode.java:12` |
| 189 | notification | `EMAIL-0003` | 400 BAD_REQUEST | 메일 전송 과정에서 오류가 발생했습니다. | EmailErrorCode | `EMAIL_MESSAGING_ERROR` | `src/main/java/com/umc/product/notification/domain/exception/EmailErrorCode.java:13` |
| 190 | notification | `FCM-0001` | 404 NOT_FOUND | FCM 토큰을 찾을 수 없습니다. | FcmErrorCode | `FCM_NOT_FOUND` | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:12` |
| 191 | notification | `FCM-0002` | 404 NOT_FOUND | 해당 유저의 FCM 토큰을 찾을 수 없습니다. | FcmErrorCode | `USER_FCM_NOT_FOUND` | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:13` |
| 192 | notification | `FCM-0003` | 500 INTERNAL_SERVER_ERROR | FCM 메시지 전송에 실패했습니다. | FcmErrorCode | `FCM_SEND_FAILED` | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:14` |
| 193 | notification | `FCM-0004` | 500 INTERNAL_SERVER_ERROR | FCM 토픽 구독에 실패했습니다. | FcmErrorCode | `TOPIC_SUBSCRIBE_FAILED` | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:15` |
| 194 | notification | `FCM-0005` | 500 INTERNAL_SERVER_ERROR | FCM 토픽 구독 해제에 실패했습니다. | FcmErrorCode | `TOPIC_UNSUBSCRIBE_FAILED` | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:16` |
| 195 | notification | `FCM-0006` | 500 INTERNAL_SERVER_ERROR | FCM 토픽 메시지 전송에 실패했습니다. | FcmErrorCode | `TOPIC_SEND_FAILED` | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:17` |
| 196 | notification | `FCM-0007` | 429 TOO_MANY_REQUESTS | FCM API 요청 한도를 초과했습니다. | FcmErrorCode | `RATE_LIMITED` | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:18` |
| 197 | notification | `WEBHOOK-0001` | 500 INTERNAL_SERVER_ERROR | 웹훅 메시지 전송에 실패했습니다. | WebhookErrorCode | `WEBHOOK_SEND_FAILED` | `src/main/java/com/umc/product/notification/domain/exception/WebhookErrorCode.java:12` |
| 198 | notification | `WEBHOOK-0002` | 400 BAD_REQUEST | 해당 플랫폼의 웹훅 어댑터가 등록되지 않았습니다. | WebhookErrorCode | `WEBHOOK_ADAPTER_NOT_FOUND` | `src/main/java/com/umc/product/notification/domain/exception/WebhookErrorCode.java:13` |

## organization

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 199 | organization | `ORGANIZATION-0001` | 400 BAD_REQUEST | 기수는 필수입니다. | OrganizationErrorCode | `GISU_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:12` |
| 200 | organization | `ORGANIZATION-0002` | 400 BAD_REQUEST | 조직 이름 설정은 필수입니다. | OrganizationErrorCode | `ORGAN_NAME_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:13` |
| 201 | organization | `ORGANIZATION-0003` | 400 BAD_REQUEST | 학교는 필수입니다. | OrganizationErrorCode | `SCHOOL_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:14` |
| 202 | organization | `ORGANIZATION-0004` | 400 BAD_REQUEST | 지부는 필수입니다. | OrganizationErrorCode | `CHAPTER_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:15` |
| 203 | organization | `ORGANIZATION-0005` | 400 BAD_REQUEST | 기수 시작일은 필수입니다. | OrganizationErrorCode | `GISU_START_AT_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:18` |
| 204 | organization | `ORGANIZATION-0006` | 400 BAD_REQUEST | 기수 종료일은 필수입니다. | OrganizationErrorCode | `GISU_END_AT_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:19` |
| 205 | organization | `ORGANIZATION-0007` | 400 BAD_REQUEST | 기수 시작일은 종료일보다 이전이어야 합니다. | OrganizationErrorCode | `GISU_PERIOD_INVALID` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:20` |
| 206 | organization | `ORGANIZATION-0008` | 400 BAD_REQUEST | 학교 이름은 필수입니다. | OrganizationErrorCode | `SCHOOL_NAME_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:22` |
| 207 | organization | `ORGANIZATION-0009` | 400 BAD_REQUEST | 학교 이메일 도메인은 필수입니다. | OrganizationErrorCode | `SCHOOL_DOMAIN_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:23` |
| 208 | organization | `ORGANIZATION-0010` | 400 BAD_REQUEST | 스터디 그룹 이름은 필수입니다. | OrganizationErrorCode | `STUDY_GROUP_NAME_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:25` |
| 209 | organization | `ORGANIZATION-0011` | 400 BAD_REQUEST | 스터디 그룹 리더는 필수입니다. | OrganizationErrorCode | `STUDY_GROUP_LEADER_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:26` |
| 210 | organization | `ORGANIZATION-0012` | 400 BAD_REQUEST | 스터디 그룹은 필수입니다. | OrganizationErrorCode | `STUDY_GROUP_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:28` |
| 211 | organization | `ORGANIZATION-0013` | 400 BAD_REQUEST | 스터디 그룹 멤버는 최소 1명 이상이어야 합니다. | OrganizationErrorCode | `STUDY_GROUP_MEMBER_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:29` |
| 212 | organization | `ORGANIZATION-0014` | 400 BAD_REQUEST | 스터디 그룹을 만들 때 Member ID는 필수입니다. | OrganizationErrorCode | `STUDY_GROUP_MEMBER_ID_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:30` |
| 213 | organization | `ORGANIZATION-0015` | 400 BAD_REQUEST | 이미 존재하는 스터디 그룹 멤버입니다. | OrganizationErrorCode | `STUDY_GROUP_MEMBER_ALREADY_EXISTS` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:31` |
| 214 | organization | `ORGANIZATION-0016` | 404 NOT_FOUND | 스터디 그룹 멤버를 찾을 수 없습니다. | OrganizationErrorCode | `STUDY_GROUP_MEMBER_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:32` |
| 215 | organization | `ORGANIZATION-0017` | 404 NOT_FOUND | 지부를 찾을 수 없습니다. | OrganizationErrorCode | `CHAPTER_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:34` |
| 216 | organization | `ORGANIZATION-0018` | 404 NOT_FOUND | 학교를 찾을 수 없습니다. | OrganizationErrorCode | `SCHOOL_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:35` |
| 217 | organization | `ORGANIZATION-0019` | 404 NOT_FOUND | 활성화된 기수를 찾을 수 없습니다. | OrganizationErrorCode | `GISU_IS_ACTIVE_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:36` |
| 218 | organization | `ORGANIZATION-0020` | 404 NOT_FOUND | 기수를 찾을 수 없습니다. | OrganizationErrorCode | `GISU_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:37` |
| 219 | organization | `ORGANIZATION-0021` | 400 BAD_REQUEST | 파트는 필수입니다. | OrganizationErrorCode | `PART_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:38` |
| 220 | organization | `ORGANIZATION-0022` | 400 BAD_REQUEST | 유효하지 않은 스터디 그룹 이름입니다. | OrganizationErrorCode | `STUDY_GROUP_NAME_INVALID` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:39` |
| 221 | organization | `ORGANIZATION-0023` | 400 BAD_REQUEST | 스터디 그룹을 찾을 수 없습니다. | OrganizationErrorCode | `STUDY_GROUP_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:40` |
| 222 | organization | `ORGANIZATION-0024` | 400 BAD_REQUEST | 스터디 그룹의 리더 또는 멤버로 존재하지 않는 챌린저가 포함되어 있습니다. | OrganizationErrorCode | `STUDY_GROUP_CHALLENGER_INVALID` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:42` |
| 223 | organization | `ORGANIZATION-0025` | 400 BAD_REQUEST | 스터디 그룹의 리더는 멤버가 될 수 없습니다. | OrganizationErrorCode | `LEADER_CANNOT_BE_MEMBER` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:44` |
| 224 | organization | `ORGANIZATION-0026` | 400 BAD_REQUEST | 스터디 그룹 멤버 ID에 중복이 있습니다. | OrganizationErrorCode | `STUDY_GROUP_MEMBER_DUPLICATED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:45` |
| 225 | organization | `ORGANIZATION-0027` | 404 NOT_FOUND | 요청하신 학교와 지부와 일치하는 정보가 없습니다. | OrganizationErrorCode | `NO_SUCH_CHAPTER_SCHOOL` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:46` |
| 226 | organization | `ORGANIZATION-0028` | 409 CONFLICT | 이미 존재하는 기수입니다. | OrganizationErrorCode | `GISU_ALREADY_EXISTS` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:47` |
| 227 | organization | `ORGANIZATION-0029` | 409 CONFLICT | 해당 기수에서 이미 다른 지부에 배정된 학교가 포함되어 있습니다. | OrganizationErrorCode | `SCHOOL_ALREADY_ASSIGNED_TO_CHAPTER` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:48` |
| 228 | organization | `ORGANIZATION-0030` | 409 CONFLICT | 해당 기수에 동일한 이름의 지부가 이미 존재합니다. | OrganizationErrorCode | `CHAPTER_NAME_DUPLICATED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:50` |
| 229 | organization | `ORGANIZATION-0031` | 403 FORBIDDEN | 스터디 그룹 조회 권한이 없습니다. | OrganizationErrorCode | `STUDY_GROUP_ACCESS_DENIED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:52` |
| 230 | organization | `ORGANIZATION-0032` | 409 CONFLICT | 해당 기수에 연결된 지부 또는 학교가 존재하여 삭제할 수 없습니다. | OrganizationErrorCode | `GISU_HAS_ASSOCIATED_CHAPTERS` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:53` |
| 231 | organization | `ORGANIZATION-0033` | 400 BAD_REQUEST | 스터디 그룹 파트장은 최소 1명 이상이어야 합니다. | OrganizationErrorCode | `STUDY_GROUP_MENTOR_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:55` |
| 232 | organization | `ORGANIZATION-0034` | 400 BAD_REQUEST | 스터디 그룹 멘토의 ID는 필수입니다. | OrganizationErrorCode | `STUDY_GROUP_MENTOR_ID_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:56` |
| 233 | organization | `ORGANIZATION-0035` | 409 CONFLICT | 다른 스터디 그룹과 중복된 멤버가 있습니다. | OrganizationErrorCode | `STUDY_GROUP_MEMBER_ALREADY_IN_PART_STUDY` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:58` |
| 234 | organization | `ORGANIZATION-0036` | 400 BAD_REQUEST | 이미 해당 스터디에 속한 파트장입니다. | OrganizationErrorCode | `STUDY_GROUP_MENTOR_DUPLICATED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:59` |
| 235 | organization | `ORGANIZATION-0037` | 404 NOT_FOUND | 스터디 그룹의 파트장 정보를 찾을 수 없습니다. | OrganizationErrorCode | `STUDY_GROUP_MENTOR_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:60` |
| 236 | organization | `ORGANIZATION-0038` | 400 BAD_REQUEST | 스터디 그룹 일정은 출석 정책이 필수입니다. | OrganizationErrorCode | `STUDY_GROUP_SCHEDULE_ATTENDANCE_POLICY_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:62` |

## project

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 237 | project | `PROJECT-0001` | 404 NOT_FOUND | 프로젝트를 찾을 수 없습니다. | ProjectErrorCode | `PROJECT_NOT_FOUND` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:13` |
| 238 | project | `PROJECT-0002` | 400 BAD_REQUEST | 이미 완료된 프로젝트입니다. | ProjectErrorCode | `ALREADY_COMPLETED_PROJECT` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:14` |
| 239 | project | `PROJECT-0003` | 400 BAD_REQUEST | 해당 프로젝트를 해산시킬 수 없습니다. | ProjectErrorCode | `PROJECT_ABORT_UNAVAILABLE` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:15` |
| 240 | project | `PROJECT-0004` | 400 BAD_REQUEST | 요청하신 조작은 지원서가 제출된 상태에서만 가능합니다. | ProjectErrorCode | `APPLICATION_NOT_SUBMITTED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:18` |
| 241 | project | `PROJECT-0005` | 400 BAD_REQUEST | 이미 지원서가 제출되었거나 평가가 완료된 상태입니다. | ProjectErrorCode | `APPLICATION_SUBMIT_NOT_AVAILABLE` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:19` |
| 242 | project | `PROJECT-0006` | 404 NOT_FOUND | 프로젝트에서 해당 지원용 폼을 찾을 수 없습니다. | ProjectErrorCode | `APPLICATION_FORM_NOT_FOUND` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:27` |
| 243 | project | `PROJECT-0007` | 403 FORBIDDEN | 요청하신 지원용 폼 섹션에 접근 권한이 없습니다. | ProjectErrorCode | `APPLICATION_FORM_ACCESS_NOT_ALLOWED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:28` |
| 244 | project | `PROJECT-0008` | 409 CONFLICT | 작성 중인 DRAFT 프로젝트가 있어 새로 시작할 수 없습니다. | ProjectErrorCode | `PROJECT_DRAFT_ALREADY_IN_PROGRESS` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:37` |
| 245 | project | `PROJECT-0009` | 400 BAD_REQUEST | 현재 상태에서 수행할 수 없는 작업입니다. | ProjectErrorCode | `PROJECT_INVALID_STATE` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:38` |
| 246 | project | `PROJECT-0010` | 400 BAD_REQUEST | 프로젝트 PO는 PLAN 파트 챌린저여야 합니다. | ProjectErrorCode | `PROJECT_OWNER_NOT_PLAN_CHALLENGER` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:39` |
| 247 | project | `PROJECT-0011` | 400 BAD_REQUEST | 제출에 필요한 필수 정보가 누락되었습니다. | ProjectErrorCode | `PROJECT_SUBMIT_VALIDATION_FAILED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:40` |
| 248 | project | `PROJECT-0012` | 403 FORBIDDEN | 해당 프로젝트에 대한 접근 권한이 없습니다. | ProjectErrorCode | `PROJECT_ACCESS_DENIED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:41` |
| 249 | project | `PROJECT-0013` | 400 BAD_REQUEST | PART 타입 섹션은 1개 이상의 파트를 지정해야 합니다. | ProjectErrorCode | `APPLICATION_FORM_POLICY_PARTS_EMPTY` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:29` |
| 250 | project | `PROJECT-0014` | 400 BAD_REQUEST | 현재 폼에 존재하지 않는 sectionId 입니다. | ProjectErrorCode | `APPLICATION_FORM_INVALID_SECTION_ID` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:30` |
| 251 | project | `PROJECT-0015` | 400 BAD_REQUEST | 해당 섹션에 속하지 않는 questionId 입니다. | ProjectErrorCode | `APPLICATION_FORM_INVALID_QUESTION_ID` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:31` |
| 252 | project | `PROJECT-0016` | 400 BAD_REQUEST | 해당 질문에 속하지 않는 optionId 입니다. | ProjectErrorCode | `APPLICATION_FORM_INVALID_OPTION_ID` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:32` |
| 253 | project | `PROJECT-0017` | 400 BAD_REQUEST | 선택지 타입(RADIO/CHECKBOX/DROPDOWN)이 아닌 질문에는 옵션을 지정할 수 없습니다. | ProjectErrorCode | `APPLICATION_FORM_OPTIONS_NOT_ALLOWED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:33` |
| 254 | project | `PROJECT-0018` | 400 BAD_REQUEST | 선택지 타입 질문에는 1개 이상의 옵션이 필요합니다. | ProjectErrorCode | `APPLICATION_FORM_OPTIONS_REQUIRED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:34` |
| 255 | project | `PROJECT-0019` | 500 INTERNAL_SERVER_ERROR | 임시저장 상태의 지원서는 PM/운영진 응답에 매핑할 수 없습니다. | ProjectErrorCode | `APPLICATION_DRAFT_NOT_EXPOSABLE` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:20` |
| 256 | project | `PROJECT-0020` | 400 BAD_REQUEST | 임시저장(DRAFT)은 PM/운영진 지원자 목록 조회 필터로 사용할 수 없습니다. | ProjectErrorCode | `APPLICATION_DRAFT_FILTER_NOT_ALLOWED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:22` |
| 257 | project | `PROJECT-0021` | 404 NOT_FOUND | 지원서를 찾을 수 없습니다. | ProjectErrorCode | `PROJECT_APPLICATION_NOT_FOUND` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:24` |
| 258 | project | `PROJECT-0100` | 404 NOT_FOUND | 프로젝트 멤버를 찾을 수 없습니다. | ProjectErrorCode | `PROJECT_MEMBER_NOT_FOUND` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:44` |
| 259 | project | `PROJECT-0101` | 409 CONFLICT | 이미 해당 프로젝트의 멤버입니다. | ProjectErrorCode | `PROJECT_MEMBER_ALREADY_EXISTS` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:45` |
| 260 | project | `PROJECT-0102` | 400 BAD_REQUEST | 메인 PM 은 팀원 제거가 아닌 소유권 양도 API 로 변경해야 합니다. | ProjectErrorCode | `PROJECT_MAIN_PM_REMOVAL_REQUIRES_TRANSFER` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:46` |
| 261 | project | `PROJECT-0200` | 400 BAD_REQUEST | 파트 정원은 1 이상이어야 합니다. | ProjectErrorCode | `PROJECT_PART_QUOTA_INVALID` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:49` |
| 262 | project | `PROJECT-0202` | 400 BAD_REQUEST | 공개하려면 파트별 정원이 1개 이상 등록되어 있어야 합니다. | ProjectErrorCode | `PROJECT_PART_QUOTA_REQUIRED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:50` |
| 263 | project | `PROJECT-0203` | 400 BAD_REQUEST | 동일 파트가 중복으로 입력되었습니다. | ProjectErrorCode | `PROJECT_PART_QUOTA_DUPLICATE` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:51` |
| 264 | project | `PROJECT-0204` | 404 NOT_FOUND | 작성 중인 지원서를 찾을 수 없습니다. | ProjectErrorCode | `PROJECT_DRAFT_APPLICATION_NOT_FOUND` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:73` |
| 265 | project | `PROJECT-0205` | 403 FORBIDDEN | 해당 프로젝트에 지원 가능한 파트가 아닙니다. | ProjectErrorCode | `PROJECT_APPLICATION_PART_NOT_ALLOWED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:74` |
| 266 | project | `PROJECT-0206` | 409 CONFLICT | 이미 해당 기수에 소속된 팀이 있어 지원할 수 없습니다. | ProjectErrorCode | `PROJECT_APPLICATION_MEMBER_ALREADY_IN_TEAM` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:75` |
| 267 | project | `PROJECT-0207` | 409 CONFLICT | 동일한 매칭 차수에 이미 제출된 지원서가 있습니다. | ProjectErrorCode | `PROJECT_APPLICATION_DUPLICATE_SUBMISSION` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:76` |
| 268 | project | `PROJECT-0208` | 400 BAD_REQUEST | 해당 매칭 차수의 지원 기간이 아닙니다. | ProjectErrorCode | `PROJECT_APPLICATION_ROUND_NOT_OPEN` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:77` |
| 269 | project | `PROJECT-0209` | 400 BAD_REQUEST | 선택한 매칭 차수가 본인 파트에 해당하지 않습니다. | ProjectErrorCode | `PROJECT_APPLICATION_ROUND_TYPE_MISMATCH` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:78` |
| 270 | project | `PROJECT-0210` | 409 CONFLICT | 이미 작성 중인 지원서가 있습니다. | ProjectErrorCode | `PROJECT_APPLICATION_ALREADY_EXISTS` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:79` |
| 271 | project | `PROJECT-0211` | 403 FORBIDDEN | 본인이 운영하는 프로젝트에는 지원할 수 없습니다. | ProjectErrorCode | `PROJECT_APPLICATION_SELF_APPLY_NOT_ALLOWED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:80` |
| 272 | project | `PROJECT-0212` | 400 BAD_REQUEST | 현재 상태에서는 합/불 결정을 변경할 수 없습니다. | ProjectErrorCode | `PROJECT_APPLICATION_DECISION_INVALID_TRANSITION` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:81` |
| 273 | project | `PROJECT-0213` | 409 CONFLICT | 해당 파트의 남은 자리를 초과하여 합격 처리할 수 없습니다. | ProjectErrorCode | `PROJECT_APPLICATION_QUOTA_EXCEEDED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:83` |
| 274 | project | `PROJECT-0214` | 400 BAD_REQUEST | 이미 종결된 지원서는 철회할 수 없습니다. | ProjectErrorCode | `PROJECT_APPLICATION_CANCEL_NOT_ALLOWED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:85` |
| 275 | project | `PROJECT-0215` | 400 BAD_REQUEST | 매칭 차수가 종료되어 지원서를 철회할 수 없습니다. | ProjectErrorCode | `PROJECT_APPLICATION_CANCEL_ROUND_CLOSED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:86` |
| 276 | project | `PROJECT-0300` | 404 NOT_FOUND | 매칭 차수를 찾을 수 없습니다. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_NOT_FOUND` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:54` |
| 277 | project | `PROJECT-0301` | 400 BAD_REQUEST | 매칭 차수 기간은 startsAt < endsAt < decisionDeadline 순서여야 합니다. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_INVALID_PERIOD` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:55` |
| 278 | project | `PROJECT-0302` | 409 CONFLICT | 같은 지부 내에서는 매칭 차수 기간이 중복될 수 없습니다. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_PERIOD_OVERLAPPED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:57` |
| 279 | project | `PROJECT-0303` | 403 FORBIDDEN | 해당 매칭 차수에 대한 관리 권한이 없습니다. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_ACCESS_DENIED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:59` |
| 280 | project | `PROJECT-0304` | 409 CONFLICT | 연관된 지원서가 있는 매칭 차수는 삭제할 수 없습니다. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_DELETE_CONFLICT` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:61` |
| 281 | project | `PROJECT-0305` | 400 BAD_REQUEST | time 기준 조회는 chapterId와 함께 요청해야 합니다. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_TIME_REQUIRES_CHAPTER` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:63` |
| 282 | project | `PROJECT-0306` | 400 BAD_REQUEST | 매칭 차수가 종료되어 더 이상 결정을 변경할 수 없습니다. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_LOCKED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:65` |
| 283 | project | `PROJECT-0307` | 400 BAD_REQUEST | 결정 마감 시각이 지나기 전에는 자동 선발을 실행할 수 없습니다. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_NOT_FINALIZABLE` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:67` |
| 284 | project | `PROJECT-0308` | 500 INTERNAL_SERVER_ERROR | 해당 매칭 종류에 대한 자동 선발 정책이 정의되지 않았습니다. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_POLICY_NOT_FOUND` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:69` |

## schedule

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 285 | schedule | `SCHEDULE-0006` | 400 BAD_REQUEST | 시작 시간은 종료 시간보다 이전이어야 합니다. | ScheduleErrorCode | `INVALID_TIME_RANGE` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:12` |
| 286 | schedule | `SCHEDULE-0009` | 404 NOT_FOUND | 일정을 찾을 수 없습니다. | ScheduleErrorCode | `SCHEDULE_NOT_FOUND` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:14` |
| 287 | schedule | `SCHEDULE-0010` | 400 BAD_REQUEST | 태그는 최소 1개 이상 선택해야 합니다. | ScheduleErrorCode | `TAG_REQUIRED` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:16` |
| 288 | schedule | `SCHEDULE-0011` | 400 BAD_REQUEST | 기존 출석 요청이 존재합니다. | ScheduleErrorCode | `NOT_FIRST_ATTENDANCE_REQUEST` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:18` |
| 289 | schedule | `SCHEDULE-0012` | 404 NOT_FOUND | 출석 요청이 존재하지 않습니다. 출석 요청을 생성하고 다시 시도해주세요. | ScheduleErrorCode | `NO_ATTENDANCE_RECORD` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:20` |
| 290 | schedule | `SCHEDULE-0013` | 400 BAD_REQUEST | 출석 사유 제출은 첫 요청, 결석 또는 지각 상태에서만 가능합니다. | ScheduleErrorCode | `INVALID_ATTENDANCE_STATUS_FOR_EXCUSE` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:22` |
| 291 | schedule | `SCHEDULE-0014` | 400 BAD_REQUEST | 현재 출석 상태에서는 승인이 불가능합니다. | ScheduleErrorCode | `INVALID_ATTENDANCE_STATUS_FOR_APPROVAL` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:25` |
| 292 | schedule | `SCHEDULE-0015` | 400 BAD_REQUEST | 출석 요청에 대한 거절을 할 수 없는 상태입니다. | ScheduleErrorCode | `INVALID_ATTENDANCE_STATUS_FOR_REJECT` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:27` |
| 293 | schedule | `SCHEDULE-0016` | 400 BAD_REQUEST | 출석 인정을 요청하는 사유가 제공되지 않았거나 비어있습니다. | ScheduleErrorCode | `NO_EXCUSE_REASON_GIVEN` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:29` |
| 294 | schedule | `SCHEDULE-0017` | 400 BAD_REQUEST | 해당 출석 요청은 운영진의 승인 또는 기각을 필요로 하는 상태가 아닙니다. | ScheduleErrorCode | `ATTENDANCE_NOT_REQUIRES_CONFIRM` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:31` |
| 295 | schedule | `SCHEDULE-0018` | 400 BAD_REQUEST | 종료된 일정에 대한 출석 요청은 허용되지 않습니다. | ScheduleErrorCode | `SCHEDULE_ENDED` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:34` |
| 296 | schedule | `SCHEDULE-0019` | 400 BAD_REQUEST | 출석 가능한 시간 이전입니다. 출석 가능한 시간 이후에 다시 시도해주세요. | ScheduleErrorCode | `CHECK_IN_TOO_EARLY` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:36` |
| 297 | schedule | `SCHEDULE-0020` | 400 BAD_REQUEST | 대면 일정은 위치 정보가 필수입니다. | ScheduleErrorCode | `OFFLINE_SCHEDULE_REQUIRES_LOCATION` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:38` |
| 298 | schedule | `SCHEDULE-0021` | 400 BAD_REQUEST | 출석 정책이 존재하지 않아 출석 요청이 불가능한 일정입니다. | ScheduleErrorCode | `SCHEDULE_ATTENDANCE_POLICY_NOT_EXIST` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:40` |
| 299 | schedule | `SCHEDULE-0022` | 400 BAD_REQUEST | 일정에 대한 참석자 정보가 존재하지 않습니다. | ScheduleErrorCode | `PARTICIPANT_NOT_FOUND` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:42` |
| 300 | schedule | `SCHEDULE-0023` | 400 BAD_REQUEST | 사용자의 출석 인증 범위 내의 존재 여부가 확인되지 않습니다. | ScheduleErrorCode | `LOCATION_NOT_VERIFIED` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:44` |
| 301 | schedule | `SCHEDULE-0024` | 400 BAD_REQUEST | 비대면 일정으로 변경 시 위치 정보를 포함할 수 없습니다. | ScheduleErrorCode | `ONLINE_SCHEDULE_SHOULD_NOT_HAVE_LOCATION` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:46` |
| 302 | schedule | `SCHEDULE-0025` | 400 BAD_REQUEST | 현재 기수의 일정만 생성할 수 있습니다. | ScheduleErrorCode | `NOT_ACTIVE_GISU_SCHEDULE` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:49` |
| 303 | schedule | `SCHEDULE-0026` | 400 BAD_REQUEST | 일정의 참여자가 아닙니다. | ScheduleErrorCode | `NOT_SCHEDULE_PARTICIPANT` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:52` |
| 304 | schedule | `SCHEDULE-0027` | 400 BAD_REQUEST | 출석을 요하는 일정의 출석 정책은 필수입니다. | ScheduleErrorCode | `ATTENDANCE_POLICY_REQUIRED` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:54` |
| 305 | schedule | `SCHEDULE-0028` | 400 BAD_REQUEST | 시작된 일정은 수정이 불가합니다. | ScheduleErrorCode | `STARTED_SCHEDULE_CANT_BE_EDITED` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:56` |
| 306 | schedule | `SCHEDULE-0029` | 403 FORBIDDEN | 일정을 생성할 수 없습니다. 챌린저 활동 이력이 필요합니다. | ScheduleErrorCode | `CANNOT_CREATE_SCHEDULE` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:58` |
| 307 | schedule | `SCHEDULE-0030` | 400 BAD_REQUEST | 초대 가능한 최대 참여자 수를 초과했습니다. | ScheduleErrorCode | `EXCEEDED_MAX_PARTICIPANTS` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:60` |
| 308 | schedule | `SCHEDULE-0031` | 403 FORBIDDEN | 출석을 요하는 일정을 생성할 권한이 없습니다. | ScheduleErrorCode | `CANNOT_CREATE_ATTENDANCE_REQUIRED_SCHEDULE` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:62` |
| 309 | schedule | `SCHEDULE-0032` | 400 BAD_REQUEST | 초대하려는 참여자에 유효하지 않은 사용자가 포함되어 있습니다. | ScheduleErrorCode | `INVALID_MEMBER_INVITE` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:64` |
| 310 | schedule | `SCHEDULE-0033` | 400 BAD_REQUEST | 출석 기록이 존재하는 일정은 삭제할 수 없습니다. | ScheduleErrorCode | `SCHEDULE_HAS_ATTENDANCE_RECORD` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:66` |

## storage

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 311 | storage | `STORAGE-0001` | 404 NOT_FOUND | 파일을 찾을 수 없습니다. | StorageErrorCode | `FILE_NOT_FOUND` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:13` |
| 312 | storage | `STORAGE-0002` | 400 BAD_REQUEST | 파일 업로드가 완료되지 않았습니다. | StorageErrorCode | `FILE_UPLOAD_NOT_COMPLETED` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:14` |
| 313 | storage | `STORAGE-0003` | 400 BAD_REQUEST | 이미 업로드가 완료된 파일입니다. | StorageErrorCode | `FILE_ALREADY_UPLOADED` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:15` |
| 314 | storage | `STORAGE-0004` | 400 BAD_REQUEST | 허용되지 않는 파일 확장자입니다. | StorageErrorCode | `INVALID_FILE_EXTENSION` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:18` |
| 315 | storage | `STORAGE-0005` | 400 BAD_REQUEST | 파일 크기가 허용 범위를 초과했습니다. | StorageErrorCode | `FILE_SIZE_EXCEEDED` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:19` |
| 316 | storage | `STORAGE-0006` | 400 BAD_REQUEST | 잘못된 Content-Type입니다. | StorageErrorCode | `INVALID_CONTENT_TYPE` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:20` |
| 317 | storage | `STORAGE-0007` | 500 INTERNAL_SERVER_ERROR | 파일 업로드에 실패했습니다. | StorageErrorCode | `STORAGE_UPLOAD_FAILED` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:23` |
| 318 | storage | `STORAGE-0008` | 500 INTERNAL_SERVER_ERROR | 파일 삭제에 실패했습니다. | StorageErrorCode | `STORAGE_DELETE_FAILED` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:24` |
| 319 | storage | `STORAGE-0009` | 500 INTERNAL_SERVER_ERROR | 파일 접근 URL 생성에 실패했습니다. | StorageErrorCode | `STORAGE_URL_GENERATION_FAILED` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:25` |
| 320 | storage | `STORAGE-0010` | 500 INTERNAL_SERVER_ERROR | CDN Signed URL 생성에 실패했습니다. | StorageErrorCode | `CDN_SIGNING_FAILED` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:28` |
| 321 | storage | `STORAGE-0011` | 500 INTERNAL_SERVER_ERROR | CDN이 활성화되어 있지만 관련 환경변수가 설정되어 있지 않습니다. 관리자에게 문의하세요. | StorageErrorCode | `NO_ENV_KEYS` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:29` |
| 322 | storage | `STORAGE-0012` | 500 INTERNAL_SERVER_ERROR | 올바르지 않은 서버 실행 환경입니다. 관리자에게 문의하세요. | StorageErrorCode | `INVALID_SPRING_PROFILE` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:30` |

## survey

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 323 | survey | `SURVEY-0001` | 404 NOT_FOUND | 폼을 찾을 수 없습니다. | SurveyErrorCode | `SURVEY_NOT_FOUND` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:12` |
| 324 | survey | `SURVEY-0002` | 409 CONFLICT | 임시저장 상태의 폼만 편집할 수 있습니다. | SurveyErrorCode | `SURVEY_NOT_DRAFT` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:13` |
| 325 | survey | `SURVEY-0003` | 404 NOT_FOUND | 질문을 찾을 수 없습니다. | SurveyErrorCode | `QUESTION_NOT_FOUND` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:14` |
| 326 | survey | `SURVEY-0006` | 404 NOT_FOUND | 폼 응답을 찾을 수 없습니다. | SurveyErrorCode | `FORM_RESPONSE_NOT_FOUND` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:16` |
| 327 | survey | `SURVEY-0007` | 400 BAD_REQUEST | 질문이 해당 폼의 질문이 아닙니다. | SurveyErrorCode | `QUESTION_IS_NOT_OWNED_BY_FORM` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:17` |
| 328 | survey | `SURVEY-0008` | 403 FORBIDDEN | 해당 폼 응답에 접근할 수 있는 권한이 없습니다. | SurveyErrorCode | `FORM_RESPONSE_FORBIDDEN` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:18` |
| 329 | survey | `SURVEY-0009` | 400 BAD_REQUEST | 질문 유형이 일치하지 않습니다. | SurveyErrorCode | `QUESTION_TYPE_MISMATCH` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:19` |
| 330 | survey | `SURVEY-0010` | 400 BAD_REQUEST | 필수 질문에 대한 응답이 누락되었습니다. | SurveyErrorCode | `REQUIRED_QUESTION_NOT_ANSWERED` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:20` |
| 331 | survey | `SURVEY-0011` | 400 BAD_REQUEST | 응답 형식이 올바르지 않습니다. | SurveyErrorCode | `INVALID_ANSWER_FORMAT` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:21` |
| 332 | survey | `SURVEY-0012` | 400 BAD_REQUEST | '기타' 선택지가 중복되었습니다. | SurveyErrorCode | `OTHER_OPTION_DUPLICATED` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:22` |
| 333 | survey | `SURVEY-0013` | 400 BAD_REQUEST | 선택지가 해당 질문의 선택지에 포함되지 않습니다. | SurveyErrorCode | `OPTION_NOT_IN_QUESTION` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:23` |
| 334 | survey | `SURVEY-0014` | 400 BAD_REQUEST | '기타' 선택지의 텍스트는 필수입니다. | SurveyErrorCode | `OPTION_TEXT_REQUIRED` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:24` |
| 335 | survey | `SURVEY-0015` | 400 BAD_REQUEST | 폼의 응답 가능 기간이 올바르지 않습니다. | SurveyErrorCode | `INVALID_FORM_ACTIVE_PERIOD` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:25` |
| 336 | survey | `SURVEY-0023` | 400 BAD_REQUEST | 선택이 올바르지 않습니다. | SurveyErrorCode | `INVALID_VOTE_SELECTION` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:27` |
| 337 | survey | `SURVEY-0025` | 400 BAD_REQUEST | 투표의 질문 형식이 올바르지 않습니다. | SurveyErrorCode | `INVALID_VOTE_FORM_STRUCTURE` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:29` |
| 338 | survey | `SURVEY-0027` | 400 BAD_REQUEST | 이미 제출한 응답이 있습니다. | SurveyErrorCode | `FORM_RESPONSE_ALREADY_EXISTS` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:31` |
| 339 | survey | `SURVEY-0028` | 409 CONFLICT | 발행된 폼만 응답할 수 있습니다. | SurveyErrorCode | `SURVEY_NOT_PUBLISHED` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:32` |
| 340 | survey | `SURVEY-0029` | 404 NOT_FOUND | 선택지를 찾을 수 없습니다. | SurveyErrorCode | `QUESTION_OPTION_NOT_FOUND` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:33` |
| 341 | survey | `SURVEY-0030` | 404 NOT_FOUND | 답변을 찾을 수 없습니다. | SurveyErrorCode | `ANSWER_NOT_FOUND` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:34` |
| 342 | survey | `SURVEY-0031` | 409 CONFLICT | 임시저장 상태의 응답에만 가능한 작업입니다. | SurveyErrorCode | `FORM_RESPONSE_NOT_DRAFT` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:35` |
| 343 | survey | `SURVEY-0032` | 400 BAD_REQUEST | 이미 해당 질문에 대한 답변이 존재합니다. | SurveyErrorCode | `ANSWER_ALREADY_EXISTS` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:36` |
| 344 | survey | `SURVEY-005` | 400 BAD_REQUEST | 이미 발행된 폼입니다. | SurveyErrorCode | `SURVEY_ALREADY_PUBLISHED` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:15` |

## term

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 345 | term | `TERMS-0001` | 404 NOT_FOUND | 약관을 찾을 수 없습니다. | TermErrorCode | `TERMS_NOT_FOUND` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:12` |
| 346 | term | `TERMS-0002` | 400 BAD_REQUEST | 약관 타입은 필수입니다. | TermErrorCode | `TERMS_TYPE_REQUIRED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:13` |
| 347 | term | `TERMS-0003` | 400 BAD_REQUEST | 약관 제목은 필수입니다. | TermErrorCode | `TERMS_TITLE_REQUIRED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:14` |
| 348 | term | `TERMS-0004` | 400 BAD_REQUEST | 약관 내용은 필수입니다. | TermErrorCode | `TERMS_CONTENT_REQUIRED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:15` |
| 349 | term | `TERMS-0005` | 400 BAD_REQUEST | 약관 버전은 필수입니다. | TermErrorCode | `TERMS_VERSION_REQUIRED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:16` |
| 350 | term | `TERMS-0006` | 404 NOT_FOUND | 약관 동의 정보를 찾을 수 없습니다. | TermErrorCode | `TERMS_CONSENT_NOT_FOUND` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:18` |
| 351 | term | `TERMS-0007` | 400 BAD_REQUEST | 이미 동의한 약관입니다. | TermErrorCode | `TERMS_CONSENT_ALREADY_EXISTS` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:19` |
| 352 | term | `TERMS-0008` | 400 BAD_REQUEST | 회원 ID는 필수입니다. | TermErrorCode | `MEMBER_ID_REQUIRED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:20` |
| 353 | term | `TERMS-0009` | 400 BAD_REQUEST | 약관 ID는 필수입니다. | TermErrorCode | `TERM_ID_REQUIRED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:21` |
| 354 | term | `TERMS-0010` | 400 BAD_REQUEST | 필수 약관에 모두 동의해야 합니다. | TermErrorCode | `MANDATORY_TERMS_NOT_AGREED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:22` |
| 355 | term | `TERMS-0011` | 403 FORBIDDEN | 해당 작업을 수행할 권한이 없습니다. | TermErrorCode | `TERM_PERMISSION_DENIED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:24` |

