# UMC PRODUCT ErrorCode 목록

UMC PRODUCT 서버에서 사용하는 `BaseCode` 기반 ErrorCode enum을 정리한 표입니다.

> 본 문서는 `./gradlew generateErrorCodeCatalog` 또는 `./gradlew generateDocumentationCatalogs`로 자동 생성합니다. ErrorCode의 원본은 각 `*ErrorCode.java` enum입니다.

## analytics

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 1 | analytics | AnalyticsErrorCode | `RESOURCE_ACCESS_DENIED` | FORBIDDEN | `ANALYTICS-0001` | 운영진 대시보드에 접근할 권한이 없습니다. | `src/main/java/com/umc/product/analytics/domain/AnalyticsErrorCode.java:11` |
| 2 | analytics | AnalyticsErrorCode | `INVALID_SORT` | BAD_REQUEST | `ANALYTICS-0002` | 지원하지 않는 정렬 조건입니다. | `src/main/java/com/umc/product/analytics/domain/AnalyticsErrorCode.java:12` |
| 3 | analytics | AnalyticsErrorCode | `INVALID_PERIOD` | BAD_REQUEST | `ANALYTICS-0003` | 조회 시작 시각은 종료 시각보다 빨라야 합니다. | `src/main/java/com/umc/product/analytics/domain/AnalyticsErrorCode.java:13` |

## authentication

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 4 | authentication | AuthenticationErrorCode | `OAUTH_PROVIDER_NOT_FOUND` | BAD_REQUEST | `AUTHENTICATION-0001` | 지원하지 않은 OAuth 제공자입니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:20` |
| 5 | authentication | AuthenticationErrorCode | `NO_MATCHING_MEMBER` | NOT_FOUND | `AUTHENTICATION-0002` | 제공된 OAuth Provider와 ProviderId와 일치하는 회원이 존재하지 않습니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:21` |
| 6 | authentication | AuthenticationErrorCode | `NO_EMAIL_VERIFICATION_METHOD_GIVEN` | BAD_REQUEST | `AUTHENTICATION-0003` | 잘못된 이메일 요청 인증입니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:23` |
| 7 | authentication | AuthenticationErrorCode | `INVALID_EMAIL_VERIFICATION` | UNAUTHORIZED | `AUTHENTICATION-0004` | 이메일 인증 정보가 일치하지 않습니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:25` |
| 8 | authentication | AuthenticationErrorCode | `OAUTH_SUCCESS_BUT_NO_MEMBER` | NOT_FOUND | `AUTHENTICATION-0006` | OAuth 인증은 성공하였으나, 가입된 회원이 없습니다. oAuthVerificationToken을 확인하세요. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:29` |
| 9 | authentication | AuthenticationErrorCode | `OAUTH_SUCCESS_BUT_NO_INFO` | SERVICE_UNAVAILABLE | `AUTHENTICATION-0007` | OAuth 인증은 성공하였으나, 필요한 사용자 정보를 제공받지 못했습니다. 관리자에게 문의하세요. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:31` |
| 10 | authentication | AuthenticationErrorCode | `OAUTH_FAILURE` | BAD_REQUEST | `AUTHENTICATION-0008` | OAuth 인증에 실패하였습니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:33` |
| 11 | authentication | AuthenticationErrorCode | `OAUTH_INVALID_ACCESS_TOKEN` | BAD_REQUEST | `AUTHENTICATION-0009` | 유효하지 않은 OAuth측 AccessToken 입니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:34` |
| 12 | authentication | AuthenticationErrorCode | `OAUTH_TOKEN_VERIFICATION_FAILED` | UNAUTHORIZED | `AUTHENTICATION-0010` | OAuth 토큰 검증에 실패하였습니다. 관리자에게 문의하세요. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:35` |
| 13 | authentication | AuthenticationErrorCode | `INVALID_OAUTH_TOKEN` | UNAUTHORIZED | `AUTHENTICATION-0011` | 유효하지 않은 OAuth 토큰입니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:37` |
| 14 | authentication | AuthenticationErrorCode | `OAUTH_ALREADY_LINKED` | UNAUTHORIZED | `AUTHENTICATION-0012` | 이미 다른 계정에 연동된 OAuth 계정입니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:38` |
| 15 | authentication | AuthenticationErrorCode | `OAUTH_PROVIDER_ALREADY_LINKED` | UNAUTHORIZED | `AUTHENTICATION-0013` | 해당 계정에 이미 연동된 OAuth 제공자입니다. 기존에 연결된 계정을 해제하고 다시 시도해주세요. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:39` |
| 16 | authentication | AuthenticationErrorCode | `MEMBER_OAUTH_NOT_FOUND` | NOT_FOUND | `AUTHENTICATION-0014` | 해당하는 Member OAuth가 존재하지 않습니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:41` |
| 17 | authentication | AuthenticationErrorCode | `NOT_VALID_MEMBER` | FORBIDDEN | `AUTHENTICATION-0015` | 해당 작업을 할 권한이 없는 사용자입니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:42` |
| 18 | authentication | AuthenticationErrorCode | `OAUTH_CANNOT_UNLINK_LAST_PROVIDER` | BAD_REQUEST | `AUTHENTICATION-0016` | 게정과 연동된 유일한 OAuth는 연동 해제할 수 없습니다. 회원 탈퇴를 이용해주세요. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:43` |
| 19 | authentication | AuthenticationErrorCode | `ALREADY_VERIFIED_EMAIL` | BAD_REQUEST | `AUTHENTICATION-0017` | 이미 인증이 완료된 이메일 인증 세션입니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:47` |
| 20 | authentication | AuthenticationErrorCode | `EMAIL_VERIFICATION_SESSION_EXPIRED` | BAD_REQUEST | `AUTHENTICATION-0018` | 만료된 이메일 인증 세션입니다. 새로운 인증을 요청해주세요. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:49` |
| 21 | authentication | AuthenticationErrorCode | `LOGIN_ID_ALREADY_EXISTS` | CONFLICT | `AUTHENTICATION-0019` | 이미 사용 중인 로그인 ID입니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:55` |
| 22 | authentication | AuthenticationErrorCode | `INVALID_LOGIN_ID_FORMAT` | BAD_REQUEST | `AUTHENTICATION-0020` | 로그인 ID 형식이 올바르지 않습니다. 영문/숫자/._- 5~20자로 입력해주세요. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:56` |
| 23 | authentication | AuthenticationErrorCode | `PASSWORD_POLICY_VIOLATION` | BAD_REQUEST | `AUTHENTICATION-0021` | 비밀번호는 8~64자이며 영문/숫자/특수문자 중 2종류 이상을 포함해야 합니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:58` |
| 24 | authentication | AuthenticationErrorCode | `INVALID_LOGIN_CREDENTIAL` | UNAUTHORIZED | `AUTHENTICATION-0022` | 로그인 ID 또는 비밀번호가 올바르지 않습니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:61` |
| 25 | authentication | AuthenticationErrorCode | `UNSUPPORTED_OAUTH_FLOW` | BAD_REQUEST | `AUTHENTICATION-0023` | 해당 OAuth Provider는 요청한 인증 흐름을 지원하지 않습니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:69` |
| 26 | authentication | AuthenticationErrorCode | `INVALID_OAUTH_REDIRECT_URI` | BAD_REQUEST | `AUTHENTICATION-0024` | 허용되지 않은 OAuth redirect URI입니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:71` |
| 27 | authentication | AuthenticationErrorCode | `INVALID_EMAIL_FORMAT` | BAD_REQUEST | `AUTHENTICATION-0025` | 이메일 형식이 올바르지 않습니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:63` |
| 28 | authentication | AuthenticationErrorCode | `EMAIL_ALREADY_EXISTS` | CONFLICT | `AUTHENTICATION-0026` | 이미 사용 중인 이메일입니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:65` |
| 29 | authentication | AuthenticationErrorCode | `EMAIL_VERIFICATION_THROTTLED` | TOO_MANY_REQUESTS | `AUTHENTICATION-0027` | 이메일 인증 발송 빈도가 너무 잦습니다. 잠시 후 다시 시도해주세요. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:51` |
| 30 | authentication | AuthenticationErrorCode | `WRONG_JWT_SIGNATURE` | UNAUTHORIZED | `JWT-0001` | JWT 토큰의 서명이 잘못되었습니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:14` |
| 31 | authentication | AuthenticationErrorCode | `EXPIRED_JWT_TOKEN` | UNAUTHORIZED | `JWT-0002` | 만료된 JWT 토큰입니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:15` |
| 32 | authentication | AuthenticationErrorCode | `UNSUPPORTED_JWT` | UNAUTHORIZED | `JWT-0003` | 지원하지 않는 JWT 토큰입니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:16` |
| 33 | authentication | AuthenticationErrorCode | `INVALID_JWT` | UNAUTHORIZED | `JWT-0004` | 잘못된 JWT 토큰입니다. | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:17` |

## authorization

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 34 | authorization | AuthorizationErrorCode | `PERMISSION_DENIED` | FORBIDDEN | `AUTHORIZATION-0001` | 권한이 없습니다. | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:12` |
| 35 | authorization | AuthorizationErrorCode | `RESOURCE_ACCESS_DENIED` | FORBIDDEN | `AUTHORIZATION-0002` | 해당 리소스에 접근할 권한이 없습니다. | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:13` |
| 36 | authorization | AuthorizationErrorCode | `INVALID_PERMISSION` | BAD_REQUEST | `AUTHORIZATION-0003` | 유효하지 않은 권한입니다. | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:14` |
| 37 | authorization | AuthorizationErrorCode | `POLICY_EVALUATION_FAILED` | INTERNAL_SERVER_ERROR | `AUTHORIZATION-0004` | 권한 검증 중 오류가 발생했습니다. | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:15` |
| 38 | authorization | AuthorizationErrorCode | `NO_EVALUATOR_MATCHING_RESOURCE_TYPE` | INTERNAL_SERVER_ERROR | `AUTHORIZATION-0005` | 해당 리소스 타입에 해당하는 Permission Evaluator가 존재하지 않습니다. 관리자에게 문의하세요. | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:16` |
| 39 | authorization | AuthorizationErrorCode | `PERMISSION_TYPE_NOT_SUPPORTED_BY_RESOURCE_TYPE` | INTERNAL_SERVER_ERROR | `AUTHORIZATION-0006` | 리소스 유형에서 지원하지 않는 권한 유형을 검사하고자 시도하였습니다. 관리자에게 문의하세요. | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:18` |
| 40 | authorization | AuthorizationErrorCode | `INVALID_INPUT_VALUE` | BAD_REQUEST | `AUTHORIZATION-0007` | 잘못된 권한 확인입니다. | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:20` |
| 41 | authorization | AuthorizationErrorCode | `INVALID_RESOURCE_ID_TYPE` | BAD_REQUEST | `AUTHORIZATION-0008` | 권한 평가를 위한 리소스 ID 타입이 잘못 전달되었습니다. 관리자에게 문의하세요. | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:21` |
| 42 | authorization | AuthorizationErrorCode | `INVALID_RESOURCE_PERMISSION_GIVEN` | INTERNAL_SERVER_ERROR | `AUTHORIZATION-0009` | 잘못된 권한 평가 요청입니다. 관리자에게 문의하세요. | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:23` |
| 43 | authorization | AuthorizationErrorCode | `CHALLENGER_ROLE_NOT_FOUND` | NOT_FOUND | `AUTHORIZATION-0010` | 해당하는 역할을 찾을 수 없습니다. | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:25` |
| 44 | authorization | AuthorizationErrorCode | `PERMISSION_TYPE_NOT_IMPLEMENTED` | NOT_IMPLEMENTED | `AUTHORIZATION-0011` | ResourceType이 지원하는 PermissionType에 대해서 PermissionEvaluator가 구현을 하지 않았습니다. 관리자에게 문의하세요. | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:26` |

## challenger

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 45 | challenger | ChallengerErrorCode | `CHALLENGER_NOT_FOUND` | NOT_FOUND | `CHALLENGER-0001` | 사용자를 찾을 수 없습니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:12` |
| 46 | challenger | ChallengerErrorCode | `CHALLENGER_ALREADY_EXISTS` | CONFLICT | `CHALLENGER-0002` | 이미 등록된 사용자입니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:13` |
| 47 | challenger | ChallengerErrorCode | `CHALLENGER_ALREADY_WITHDRAWN` | BAD_REQUEST | `CHALLENGER-0003` | 이미 탈퇴한 사용자입니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:14` |
| 48 | challenger | ChallengerErrorCode | `INVALID_CHALLENGER_STATUS` | BAD_REQUEST | `CHALLENGER-0004` | 올바르지 않은 사용자 상태입니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:15` |
| 49 | challenger | ChallengerErrorCode | `CHALLENGER_NOT_ACTIVE` | BAD_REQUEST | `CHALLENGER-0005` | 유효한 챌린저가 아닙니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:16` |
| 50 | challenger | ChallengerErrorCode | `CHALLENGER_POINT_NOT_FOUND` | NOT_FOUND | `CHALLENGER-0007` | 상벌점 기록을 찾을 수 없습니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:17` |
| 51 | challenger | ChallengerErrorCode | `BAD_CHALLENGER_UPDATE_REQUEST` | NOT_FOUND | `CHALLENGER-0008` | 잘못된 챌린저 업데이트 요청입니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:18` |
| 52 | challenger | ChallengerErrorCode | `NOT_ALLOWED_AUTHOR` | BAD_REQUEST | `CHALLENGER-0009` | 활성 또는 수료 상태의 사용자만 일정 생성이 가능합니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:19` |
| 53 | challenger | ChallengerErrorCode | `MEMBER_PROFILE_NOT_FOUND` | NOT_FOUND | `CHALLENGER-0010` | 챌린저에 연결된 멤버 프로필을 찾을 수 없습니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:20` |
| 54 | challenger | ChallengerErrorCode | `INVALID_CURSOR_ID` | BAD_REQUEST | `CHALLENGER-0011` | 유효하지 않은 커서 ID입니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:21` |
| 55 | challenger | ChallengerErrorCode | `USED_CHALLENGER_RECORD_CODE` | BAD_REQUEST | `CHALLENGER-0012` | 이미 사용된 챌린저 기록 추가용 코드입니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:22` |
| 56 | challenger | ChallengerErrorCode | `INVALID_MEMBER_NAME_FOR_RECORD` | BAD_REQUEST | `CHALLENGER-0013` | 코드에 등록된 사용자 이름이 요청자와 일치하지 않습니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:23` |
| 57 | challenger | ChallengerErrorCode | `INVALID_SCHOOL_FOR_RECORD` | BAD_REQUEST | `CHALLENGER-0014` | 코드에 등록된 학교가 요청자 소속과 일치하지 않습니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:24` |
| 58 | challenger | ChallengerErrorCode | `INVALID_CHALLENGER_RECORD_CREATE_REQUEST` | BAD_REQUEST | `CHALLENGER-0015` | 제공된 정보로 챌린저 기록을 생성할 수 없습니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:25` |
| 59 | challenger | ChallengerErrorCode | `NO_CHALLENGER_IN_MEMBER_GISU` | NOT_FOUND | `CHALLENGER-0016` | 해당 회원은 주어진 기수에 활동한 챌린저가 아닙니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:26` |
| 60 | challenger | ChallengerErrorCode | `CHALLENGER_PART_NOT_FOUND` | NOT_FOUND | `CHALLENGER-0017` | 해당 Challenger의 Part는 존재하지 않습니다. | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:27` |

## community

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 61 | community | CommunityErrorCode | `POST_NOT_FOUND` | NOT_FOUND | `COMMUNITY-0001` | 게시글을 찾을 수 없습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:12` |
| 62 | community | CommunityErrorCode | `COMMENT_NOT_FOUND` | NOT_FOUND | `COMMUNITY-0002` | 댓글을 찾을 수 없습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:13` |
| 63 | community | CommunityErrorCode | `TROPHY_NOT_FOUND` | NOT_FOUND | `COMMUNITY-0003` | 상장을 찾을 수 없습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:14` |
| 64 | community | CommunityErrorCode | `INVALID_POST_TITLE` | BAD_REQUEST | `COMMUNITY-0004` | 게시글 제목이 유효하지 않습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:16` |
| 65 | community | CommunityErrorCode | `INVALID_POST_CONTENT` | BAD_REQUEST | `COMMUNITY-0005` | 게시글 내용이 유효하지 않습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:17` |
| 66 | community | CommunityErrorCode | `INVALID_POST_CATEGORY` | BAD_REQUEST | `COMMUNITY-0006` | 게시글 카테고리가 유효하지 않습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:18` |
| 67 | community | CommunityErrorCode | `INVALID_POST_REGION` | BAD_REQUEST | `COMMUNITY-0007` | 게시글 지역이 유효하지 않습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:19` |
| 68 | community | CommunityErrorCode | `CANNOT_CHANGE_TO_LIGHTNING` | BAD_REQUEST | `COMMUNITY-0008` | 일반 게시글을 번개 게시글로 변경할 수 없습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:20` |
| 69 | community | CommunityErrorCode | `CANNOT_CHANGE_FROM_LIGHTNING` | BAD_REQUEST | `COMMUNITY-0009` | 번개 게시글을 일반 게시글로 변경할 수 없습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:21` |
| 70 | community | CommunityErrorCode | `INVALID_COMMENT_CONTENT` | BAD_REQUEST | `COMMUNITY-0010` | 댓글 내용이 유효하지 않습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:23` |
| 71 | community | CommunityErrorCode | `COMMENT_NOT_OWNED` | FORBIDDEN | `COMMUNITY-0011` | 본인의 댓글만 삭제할 수 있습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:24` |
| 72 | community | CommunityErrorCode | `INVALID_TROPHY_WEEK` | BAD_REQUEST | `COMMUNITY-0012` | 상장 주차가 유효하지 않습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:26` |
| 73 | community | CommunityErrorCode | `INVALID_TROPHY_TITLE` | BAD_REQUEST | `COMMUNITY-0013` | 상장 제목이 유효하지 않습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:27` |
| 74 | community | CommunityErrorCode | `INVALID_TROPHY_CONTENT` | BAD_REQUEST | `COMMUNITY-0014` | 상장 내용이 유효하지 않습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:28` |
| 75 | community | CommunityErrorCode | `INVALID_TROPHY_URL` | BAD_REQUEST | `COMMUNITY-0015` | 상장 URL이 유효하지 않습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:29` |
| 76 | community | CommunityErrorCode | `REPORT_ALREADY_EXISTS` | CONFLICT | `COMMUNITY-0016` | 이미 신고한 게시글/댓글입니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:31` |
| 77 | community | CommunityErrorCode | `INVALID_POST_AUTHOR` | BAD_REQUEST | `COMMUNITY-0017` | 작성자 ID는 필수입니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:34` |
| 78 | community | CommunityErrorCode | `NOT_LIGHTNING_POST` | BAD_REQUEST | `COMMUNITY-0018` | 번개 게시글이 아닙니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:35` |
| 79 | community | CommunityErrorCode | `USE_LIGHTNING_API` | BAD_REQUEST | `COMMUNITY-0019` | 번개 게시글은 번개 전용 API를 사용하세요. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:36` |
| 80 | community | CommunityErrorCode | `LIGHTNING_INFO_REQUIRED` | BAD_REQUEST | `COMMUNITY-0020` | 번개 게시글은 추가 정보가 필수입니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:37` |
| 81 | community | CommunityErrorCode | `POST_NOT_OWNED` | FORBIDDEN | `COMMUNITY-0021` | 본인의 게시글만 수정/삭제할 수 있습니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:38` |
| 82 | community | CommunityErrorCode | `INVALID_LIGHTNING_MEET_AT` | BAD_REQUEST | `COMMUNITY-0022` | 모임 시간은 필수입니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:41` |
| 83 | community | CommunityErrorCode | `INVALID_LIGHTNING_LOCATION` | BAD_REQUEST | `COMMUNITY-0023` | 모임 장소는 필수입니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:42` |
| 84 | community | CommunityErrorCode | `INVALID_LIGHTNING_MAX_PARTICIPANTS` | BAD_REQUEST | `COMMUNITY-0024` | 최대 참가자는 1명 이상이어야 합니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:43` |
| 85 | community | CommunityErrorCode | `INVALID_LIGHTNING_OPEN_CHAT_URL` | BAD_REQUEST | `COMMUNITY-0025` | 오픈 채팅 링크는 필수입니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:44` |
| 86 | community | CommunityErrorCode | `INVALID_LIGHTNING_OPEN_CHAT_URL_FORMAT` | BAD_REQUEST | `COMMUNITY-0026` | 오픈 채팅 링크는 http:// 또는 https://로 시작해야 합니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:45` |
| 87 | community | CommunityErrorCode | `INVALID_LIGHTNING_MEET_AT_PAST` | BAD_REQUEST | `COMMUNITY-0027` | 모임 시간은 현재 이후여야 합니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:46` |
| 88 | community | CommunityErrorCode | `INVALID_COMMENT_POST_ID` | BAD_REQUEST | `COMMUNITY-0028` | 게시글 ID는 필수입니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:49` |
| 89 | community | CommunityErrorCode | `INVALID_COMMENT_CHALLENGER_ID` | BAD_REQUEST | `COMMUNITY-0029` | 챌린저 ID는 필수입니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:50` |
| 90 | community | CommunityErrorCode | `INVALID_ID` | BAD_REQUEST | `COMMUNITY-0030` | ID는 양수여야 합니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:53` |
| 91 | community | CommunityErrorCode | `POST_SAVE_REQUIRES_AUTHOR` | BAD_REQUEST | `COMMUNITY-0031` | 새 게시글 생성 시에는 작성자 정보가 필요합니다. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:56` |
| 92 | community | CommunityErrorCode | `POST_UPDATE_INVALID_CALL` | BAD_REQUEST | `COMMUNITY-0032` | 이미 ID가 있는 게시글은 update용 save를 사용하세요. | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:57` |

## curriculum

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 93 | curriculum | CurriculumErrorCode | `CURRICULUM_NOT_FOUND` | NOT_FOUND | `CURRICULUM-0001` | 커리큘럼을 찾을 수 없습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:12` |
| 94 | curriculum | CurriculumErrorCode | `WORKBOOK_NOT_FOUND` | NOT_FOUND | `CURRICULUM-0002` | 워크북을 찾을 수 없습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:13` |
| 95 | curriculum | CurriculumErrorCode | `MISSION_NOT_FOUND` | NOT_FOUND | `CURRICULUM-0003` | 미션을 찾을 수 없습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:14` |
| 96 | curriculum | CurriculumErrorCode | `WORKBOOK_HAS_SUBMISSIONS` | CONFLICT | `CURRICULUM-0004` | 제출된 워크북이 있어 삭제할 수 없습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:15` |
| 97 | curriculum | CurriculumErrorCode | `WORKBOOK_NOT_IN_CURRICULUM` | NOT_FOUND | `CURRICULUM-0005` | 커리큘럼에 해당 워크북이 존재하지 않습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:16` |
| 98 | curriculum | CurriculumErrorCode | `CHALLENGER_WORKBOOK_NOT_FOUND` | NOT_FOUND | `CURRICULUM-0006` | 챌린저 워크북을 찾을 수 없습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:17` |
| 99 | curriculum | CurriculumErrorCode | `SUBMISSION_REQUIRED` | BAD_REQUEST | `CURRICULUM-0007` | 제출 내용이 필요합니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:18` |
| 100 | curriculum | CurriculumErrorCode | `INVALID_WORKBOOK_STATUS` | BAD_REQUEST | `CURRICULUM-0008` | 워크북 상태가 유효하지 않습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:19` |
| 101 | curriculum | CurriculumErrorCode | `WORKBOOK_SUBMISSION_ALREADY_EXISTS` | CONFLICT | `CURRICULUM-0009` | 이미 해당 주차의 워크북 미션을 제출하였습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:20` |
| 102 | curriculum | CurriculumErrorCode | `CURRICULUM_ALREADY_EXISTS` | CONFLICT | `CURRICULUM-0010` | 해당 기수와 파트의 커리큘럼이 이미 존재합니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:21` |
| 103 | curriculum | CurriculumErrorCode | `WORKBOOK_ACCESS_DENIED` | FORBIDDEN | `CURRICULUM-0011` | 해당 워크북에 대한 접근 권한이 없습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:22` |
| 104 | curriculum | CurriculumErrorCode | `INVALID_WEEKLY_CURRICULUM_PERIOD` | BAD_REQUEST | `CURRICULUM-0012` | 주차 커리큘럼의 시작일이 종료일보다 늦을 수 없습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:23` |
| 105 | curriculum | CurriculumErrorCode | `INVALID_WORKBOOK_STATUS_TRANSITION` | BAD_REQUEST | `CURRICULUM-0013` | 유효하지 않은 워크북 상태 변경 요청입니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:24` |
| 106 | curriculum | CurriculumErrorCode | `WEEKLY_CURRICULUM_NOT_FOUND` | NOT_FOUND | `CURRICULUM-0014` | 주차별 커리큘럼을 찾을 수 없습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:25` |
| 107 | curriculum | CurriculumErrorCode | `CURRICULUM_HAS_WEEKLY_CURRICULUMS` | CONFLICT | `CURRICULUM-0015` | 주차별 커리큘럼이 존재하여 커리큘럼을 삭제할 수 없습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:26` |
| 108 | curriculum | CurriculumErrorCode | `WEEKLY_CURRICULUM_HAS_WORKBOOKS` | CONFLICT | `CURRICULUM-0016` | 원본 워크북이 존재하여 주차별 커리큘럼을 삭제할 수 없습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:27` |
| 109 | curriculum | CurriculumErrorCode | `WEEKLY_CURRICULUM_DATE_LOCKED` | CONFLICT | `CURRICULUM-0017` | 배포된 워크북이 존재하여 주차 기간을 수정할 수 없습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:28` |
| 110 | curriculum | CurriculumErrorCode | `WEEKLY_CURRICULUM_ALREADY_EXISTS` | CONFLICT | `CURRICULUM-0018` | 이미 동일한 주차와 부록 여부를 가진 주차별 커리큘럼이 존재합니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:29` |
| 111 | curriculum | CurriculumErrorCode | `WEEKLY_CURRICULUM_PERIOD_ALREADY_ENDED` | BAD_REQUEST | `CURRICULUM-0019` | 이미 종료된 기간으로 주차별 커리큘럼을 생성하거나 수정할 수 없습니다. | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:30` |

## figma

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 112 | figma | FigmaErrorCode | `INTEGRATION_NOT_FOUND` | NOT_FOUND | `FIGMA-0001` | Figma OAuth 통합 정보가 등록되어 있지 않습니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:12` |
| 113 | figma | FigmaErrorCode | `OAUTH_TOKEN_EXCHANGE_FAILED` | BAD_GATEWAY | `FIGMA-0002` | Figma OAuth 토큰 교환에 실패했습니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:13` |
| 114 | figma | FigmaErrorCode | `OAUTH_TOKEN_REFRESH_FAILED` | BAD_GATEWAY | `FIGMA-0003` | Figma access token 갱신에 실패했습니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:14` |
| 115 | figma | FigmaErrorCode | `COMMENT_FETCH_FAILED` | BAD_GATEWAY | `FIGMA-0004` | Figma 댓글 조회에 실패했습니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:15` |
| 116 | figma | FigmaErrorCode | `FILE_METADATA_FETCH_FAILED` | BAD_GATEWAY | `FIGMA-0005` | Figma 파일 메타데이터 조회에 실패했습니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:16` |
| 117 | figma | FigmaErrorCode | `WATCHED_FILE_NOT_FOUND` | NOT_FOUND | `FIGMA-0006` | 등록된 Figma 폴링 대상 파일이 아닙니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:17` |
| 118 | figma | FigmaErrorCode | `WATCHED_FILE_ALREADY_EXISTS` | CONFLICT | `FIGMA-0007` | 이미 등록된 Figma 파일 키 입니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:18` |
| 119 | figma | FigmaErrorCode | `OAUTH_STATE_MISMATCH` | BAD_REQUEST | `FIGMA-0008` | Figma OAuth state 값이 일치하지 않습니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:19` |
| 120 | figma | FigmaErrorCode | `TOKEN_ENCRYPTION_FAILED` | INTERNAL_SERVER_ERROR | `FIGMA-0009` | Figma 토큰 암복호화에 실패했습니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:20` |
| 121 | figma | FigmaErrorCode | `DISCORD_MENTION_SEND_FAILED` | BAD_GATEWAY | `FIGMA-0010` | Discord 멘션 전송에 실패했습니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:21` |
| 122 | figma | FigmaErrorCode | `ROUTING_DOMAIN_NOT_FOUND` | NOT_FOUND | `FIGMA-0013` | 등록된 Figma 라우팅 도메인이 아닙니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:22` |
| 123 | figma | FigmaErrorCode | `ROUTING_DOMAIN_ALREADY_EXISTS` | CONFLICT | `FIGMA-0014` | 동일한 domain_key 의 라우팅 도메인이 이미 등록되어 있습니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:23` |
| 124 | figma | FigmaErrorCode | `ROUTING_DOMAIN_MENTION_NOT_FOUND` | NOT_FOUND | `FIGMA-0015` | 해당 라우팅 도메인의 mention 이 아닙니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:24` |
| 125 | figma | FigmaErrorCode | `ROUTING_DOMAIN_NOT_REGISTERED` | PRECONDITION_FAILED | `FIGMA-0016` | 라우팅 도메인이 한 건도 등록되어 있지 않습니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:25` |
| 126 | figma | FigmaErrorCode | `DIGEST_RANGE_INVALID` | BAD_REQUEST | `FIGMA-0017` | digest 의 from/to 시간창이 유효하지 않습니다. | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:26` |

## global

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 127 | global | CommonErrorCode | `INTERNAL_SERVER_ERROR` | INTERNAL_SERVER_ERROR | `COMMON-0001` | 알 수 없는 오류입니다. 관리자에게 문의해주세요. | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:24` |
| 128 | global | CommonErrorCode | `BAD_REQUEST` | BAD_REQUEST | `COMMON-400` | 잘못된 요청입니다. | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:26` |
| 129 | global | CommonErrorCode | `UNAUTHORIZED` | UNAUTHORIZED | `COMMON-401` | 인증이 필요합니다. | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:27` |
| 130 | global | CommonErrorCode | `FORBIDDEN` | FORBIDDEN | `COMMON-403` | 허용되지 않는 요청입니다. | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:28` |
| 131 | global | CommonErrorCode | `NOT_FOUND` | NOT_FOUND | `COMMON-404` | 요청한 리소스를 찾을 수 없습니다. | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:29` |
| 132 | global | CommonErrorCode | `NOT_IMPLEMENTED` | NOT_IMPLEMENTED | `COMMON-501` | 아직 구현되지 않은 기능입니다. 서버팀에게 문의해주세요. | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:30` |
| 133 | global | CommonErrorCode | `INVALID_ENV` | BAD_REQUEST | `ENV-0001` | 현재 실행 환경에서는 사용할 수 없는 기능입니다. | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:37` |
| 134 | global | CommonErrorCode | `PERMISSION_TYPE_NOT_IMPLEMENTED` | NOT_IMPLEMENTED | `PE-0001` | 요청하신 PE가 존재하지 않습니다. 관리자에게 문의해주세요. | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:40` |
| 135 | global | CommonErrorCode | `SECURITY_NOT_GIVEN` | UNAUTHORIZED | `SECURITY-0001` | 인증 정보가 전달되지 않았습니다. | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:33` |
| 136 | global | CommonErrorCode | `SECURITY_FORBIDDEN` | FORBIDDEN | `SECURITY-0002` | 권한이 부족합니다. | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:34` |

## llm

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 137 | llm | LlmErrorCode | `CHAT_COMPLETION_FAILED` | BAD_GATEWAY | `LLM-0001` | LLM 호출에 실패했습니다. | `src/main/java/com/umc/product/llm/domain/exception/LlmErrorCode.java:12` |
| 138 | llm | LlmErrorCode | `CHAT_COMPLETION_INVALID_RESPONSE` | BAD_GATEWAY | `LLM-0002` | LLM 응답을 해석할 수 없습니다. | `src/main/java/com/umc/product/llm/domain/exception/LlmErrorCode.java:13` |
| 139 | llm | LlmErrorCode | `PROVIDER_NOT_CONFIGURED` | INTERNAL_SERVER_ERROR | `LLM-0003` | LLM provider 설정이 누락되었습니다. | `src/main/java/com/umc/product/llm/domain/exception/LlmErrorCode.java:14` |

## maintenance

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 140 | maintenance | MaintenanceErrorCode | `SERVICE_UNDER_MAINTENANCE` | SERVICE_UNAVAILABLE | `MAINTENANCE-0001` | 서비스 점검 중입니다. | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:12` |
| 141 | maintenance | MaintenanceErrorCode | `MAINTENANCE_WINDOW_NOT_FOUND` | NOT_FOUND | `MAINTENANCE-0002` | 점검 윈도우를 찾을 수 없습니다. | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:13` |
| 142 | maintenance | MaintenanceErrorCode | `INVALID_TIME_RANGE` | BAD_REQUEST | `MAINTENANCE-0003` | 종료 시각은 시작 시각 이후여야 합니다. | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:14` |
| 143 | maintenance | MaintenanceErrorCode | `START_AT_IN_PAST` | BAD_REQUEST | `MAINTENANCE-0004` | 시작 시각은 현재 시각 이후여야 합니다. | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:15` |
| 144 | maintenance | MaintenanceErrorCode | `TARGET_DOMAINS_REQUIRED` | BAD_REQUEST | `MAINTENANCE-0005` | PER_DOMAIN 점검은 대상 도메인을 1개 이상 지정해야 합니다. | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:16` |
| 145 | maintenance | MaintenanceErrorCode | `OVERLAPPING_WINDOW` | CONFLICT | `MAINTENANCE-0006` | 다른 점검 윈도우와 시간이 겹칩니다. | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:17` |
| 146 | maintenance | MaintenanceErrorCode | `ALREADY_ENDED` | BAD_REQUEST | `MAINTENANCE-0007` | 이미 종료된 점검 윈도우입니다. | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:18` |
| 147 | maintenance | MaintenanceErrorCode | `NOT_SUPER_ADMIN` | FORBIDDEN | `MAINTENANCE-0008` | 점검 관리 권한이 없습니다. | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:19` |

## member

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 148 | member | MemberErrorCode | `MEMBER_NOT_FOUND` | NOT_FOUND | `MEMBER-0001` | 사용자를 찾을 수 없습니다. | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:12` |
| 149 | member | MemberErrorCode | `MEMBER_ALREADY_EXISTS` | CONFLICT | `MEMBER-0002` | 이미 등록된 사용자입니다. | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:13` |
| 150 | member | MemberErrorCode | `EMAIL_ALREADY_EXISTS` | CONFLICT | `MEMBER-0003` | 이미 사용 중인 이메일입니다. | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:14` |
| 151 | member | MemberErrorCode | `MEMBER_ALREADY_WITHDRAWN` | BAD_REQUEST | `MEMBER-0004` | 이미 탈퇴한 사용자입니다. | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:15` |
| 152 | member | MemberErrorCode | `INVALID_MEMBER_STATUS` | BAD_REQUEST | `MEMBER-0005` | 올바르지 않은 사용자 상태입니다. | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:16` |
| 153 | member | MemberErrorCode | `MEMBER_NOT_ACTIVE` | BAD_REQUEST | `MEMBER-0006` | 올바르지 않은 사용자 상태입니다. | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:17` |
| 154 | member | MemberErrorCode | `MEMBER_ALREADY_REGISTERED` | BAD_REQUEST | `MEMBER-0007` | 이미 회원가입을 완료한 사용자입니다. | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:18` |
| 155 | member | MemberErrorCode | `MEMBER_PROFILE_NOT_FOUND` | NOT_FOUND | `MEMBER-0008` | 프로필을 찾을 수 없습니다. | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:19` |
| 156 | member | MemberErrorCode | `MEMBER_SCHOOL_NOT_ASSIGNED` | BAD_REQUEST | `MEMBER-0009` | 학교가 등록되지 않은 사용자입니다. | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:20` |
| 157 | member | MemberErrorCode | `CREDENTIAL_ALREADY_REGISTERED` | CONFLICT | `MEMBER-0010` | 이미 ID/PW 자격증명이 등록된 사용자입니다. | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:21` |
| 158 | member | MemberErrorCode | `CREDENTIAL_NOT_REGISTERED` | BAD_REQUEST | `MEMBER-0011` | ID/PW 자격증명이 등록되지 않은 사용자입니다. | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:22` |
| 159 | member | MemberErrorCode | `INVALID_LOGIN_ID` | BAD_REQUEST | `MEMBER-0012` | 올바르지 않은 로그인 아이디입니다. | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:23` |
| 160 | member | MemberErrorCode | `INVALID_PASSWORD` | BAD_REQUEST | `MEMBER-0013` | 올바르지 않은 비밀번호입니다. | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:24` |

## notice

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 161 | notice | NoticeErrorCode | `NOTICE_NOT_FOUND` | NOT_FOUND | `NOTICE-0001` | 공지사항을 찾을 수 없습니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:12` |
| 162 | notice | NoticeErrorCode | `ALREADY_PUBLISHED_NOTICE` | BAD_REQUEST | `NOTICE-0002` | 이미 게시된 공지사항입니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:13` |
| 163 | notice | NoticeErrorCode | `INVALID_NOTICE_TITLE` | BAD_REQUEST | `NOTICE-0003` | 공지사항 제목이 유효하지 않습니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:14` |
| 164 | notice | NoticeErrorCode | `INVALID_NOTICE_CONTENT` | BAD_REQUEST | `NOTICE-0004` | 공지사항 내용이 유효하지 않습니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:15` |
| 165 | notice | NoticeErrorCode | `INVALID_NOTICE_STATUS_FOR_REMINDER` | BAD_REQUEST | `NOTICE-0005` | 공지사항 알림을 보낼 수 없는 상태입니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:16` |
| 166 | notice | NoticeErrorCode | `AUTHOR_REQUIRED` | BAD_REQUEST | `NOTICE-0006` | 공지사항 작성자는 필수입니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:17` |
| 167 | notice | NoticeErrorCode | `NOTICE_SCOPE_REQUIRED` | BAD_REQUEST | `NOTICE-0007` | 공지사항 대상 범위는 필수입니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:18` |
| 168 | notice | NoticeErrorCode | `NOTICE_AUTHOR_MISMATCH` | FORBIDDEN | `NOTICE-0008` | 공지사항 작성자가 아닙니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:19` |
| 169 | notice | NoticeErrorCode | `NO_WRITE_PERMISSION` | FORBIDDEN | `NOTICE-0009` | 해당 공지사항을 작성할 권한이 없습니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:20` |
| 170 | notice | NoticeErrorCode | `INVALID_TARGET_SETTING` | BAD_REQUEST | `NOTICE-0010` | 공지사항 수신자 설정이 잘못되었습니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:22` |
| 171 | notice | NoticeErrorCode | `NO_TARGET_FOUND` | NOT_FOUND | `NOTICE-0011` | 해당 공지사항에 설정된 수신 대상이 존재하지 않습니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:23` |
| 172 | notice | NoticeErrorCode | `NO_READ_PERMISSION` | FORBIDDEN | `NOTICE-0012` | 해당 공지사항을 조회할 권한이 없습니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:21` |
| 173 | notice | NoticeErrorCode | `NOT_IMPLEMENTED_YET` | NOT_IMPLEMENTED | `NOTICE-9999` | 아직 구현되지 않은 기능입니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:41` |
| 174 | notice | NoticeErrorCode | `VOTE_IDS_REQUIRED` | BAD_REQUEST | `NOTICE-CONTENTS-0001` | 투표 ID 목록은 필수입니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:26` |
| 175 | notice | NoticeErrorCode | `IMAGE_URLS_REQUIRED` | BAD_REQUEST | `NOTICE-CONTENTS-0002` | 이미지 URL 목록은 필수입니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:27` |
| 176 | notice | NoticeErrorCode | `LINK_URLS_REQUIRED` | BAD_REQUEST | `NOTICE-CONTENTS-0003` | 링크 URL 목록은 필수입니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:28` |
| 177 | notice | NoticeErrorCode | `NOTICE_VOTE_NOT_FOUND` | NOT_FOUND | `NOTICE-CONTENTS-0004` | 공지사항 투표를 찾을 수 없습니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:29` |
| 178 | notice | NoticeErrorCode | `NOTICE_IMAGE_NOT_FOUND` | NOT_FOUND | `NOTICE-CONTENTS-0005` | 공지사항 이미지를 찾을 수 없습니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:30` |
| 179 | notice | NoticeErrorCode | `NOTICE_LINK_NOT_FOUND` | NOT_FOUND | `NOTICE-CONTENTS-0006` | 공지사항 링크를 찾을 수 없습니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:31` |
| 180 | notice | NoticeErrorCode | `IMAGE_LIMIT_EXCEEDED` | BAD_REQUEST | `NOTICE-CONTENTS-0007` | 공지사항 이미지는 최대 10장까지 등록할 수 있습니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:32` |
| 181 | notice | NoticeErrorCode | `VOTE_ALREADY_EXISTS` | CONFLICT | `NOTICE-CONTENTS-0008` | 해당 공지사항에 이미 투표가 존재합니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:33` |
| 182 | notice | NoticeErrorCode | `INVALID_VOTE_OPTION_COUNT` | BAD_REQUEST | `NOTICE-CONTENTS-0009` | 투표 항목은 2개 이상 5개 이하여야 합니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:34` |
| 183 | notice | NoticeErrorCode | `INVALID_VOTE_OPTION_CONTENT` | BAD_REQUEST | `NOTICE-CONTENTS-0010` | 투표 항목에 빈 값이 포함될 수 없습니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:35` |
| 184 | notice | NoticeErrorCode | `VOTE_NOT_STARTED` | BAD_REQUEST | `NOTICE-CONTENTS-0011` | 아직 투표 기간이 시작되지 않았습니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:36` |
| 185 | notice | NoticeErrorCode | `VOTE_CLOSED` | BAD_REQUEST | `NOTICE-CONTENTS-0012` | 이미 종료된 투표입니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:37` |
| 186 | notice | NoticeErrorCode | `SELECTED_OPTION_IDS_REQUIRED` | BAD_REQUEST | `NOTICE-CONTENTS-0013` | 선택한 투표 항목 ID 목록은 필수입니다. | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:38` |

## notification

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 187 | notification | EmailErrorCode | `EMAIL_GENERAL_ERROR` | BAD_REQUEST | `EMAIL-0001` | 알 수 없는 사유로 이메일 전송에 실패했습니다. | `src/main/java/com/umc/product/notification/domain/exception/EmailErrorCode.java:11` |
| 188 | notification | EmailErrorCode | `EMAIL_ENCODING_ERROR` | BAD_REQUEST | `EMAIL-0002` | 인코딩 과정에서 오류가 발생했습니다. | `src/main/java/com/umc/product/notification/domain/exception/EmailErrorCode.java:12` |
| 189 | notification | EmailErrorCode | `EMAIL_MESSAGING_ERROR` | BAD_REQUEST | `EMAIL-0003` | 메일 전송 과정에서 오류가 발생했습니다. | `src/main/java/com/umc/product/notification/domain/exception/EmailErrorCode.java:13` |
| 190 | notification | FcmErrorCode | `FCM_NOT_FOUND` | NOT_FOUND | `FCM-0001` | FCM 토큰을 찾을 수 없습니다. | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:12` |
| 191 | notification | FcmErrorCode | `USER_FCM_NOT_FOUND` | NOT_FOUND | `FCM-0002` | 해당 유저의 FCM 토큰을 찾을 수 없습니다. | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:13` |
| 192 | notification | FcmErrorCode | `FCM_SEND_FAILED` | INTERNAL_SERVER_ERROR | `FCM-0003` | FCM 메시지 전송에 실패했습니다. | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:14` |
| 193 | notification | FcmErrorCode | `TOPIC_SUBSCRIBE_FAILED` | INTERNAL_SERVER_ERROR | `FCM-0004` | FCM 토픽 구독에 실패했습니다. | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:15` |
| 194 | notification | FcmErrorCode | `TOPIC_UNSUBSCRIBE_FAILED` | INTERNAL_SERVER_ERROR | `FCM-0005` | FCM 토픽 구독 해제에 실패했습니다. | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:16` |
| 195 | notification | FcmErrorCode | `TOPIC_SEND_FAILED` | INTERNAL_SERVER_ERROR | `FCM-0006` | FCM 토픽 메시지 전송에 실패했습니다. | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:17` |
| 196 | notification | FcmErrorCode | `RATE_LIMITED` | TOO_MANY_REQUESTS | `FCM-0007` | FCM API 요청 한도를 초과했습니다. | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:18` |
| 197 | notification | WebhookErrorCode | `WEBHOOK_SEND_FAILED` | INTERNAL_SERVER_ERROR | `WEBHOOK-0001` | 웹훅 메시지 전송에 실패했습니다. | `src/main/java/com/umc/product/notification/domain/exception/WebhookErrorCode.java:12` |
| 198 | notification | WebhookErrorCode | `WEBHOOK_ADAPTER_NOT_FOUND` | BAD_REQUEST | `WEBHOOK-0002` | 해당 플랫폼의 웹훅 어댑터가 등록되지 않았습니다. | `src/main/java/com/umc/product/notification/domain/exception/WebhookErrorCode.java:13` |

## organization

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 199 | organization | OrganizationErrorCode | `GISU_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0001` | 기수는 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:12` |
| 200 | organization | OrganizationErrorCode | `ORGAN_NAME_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0002` | 조직 이름 설정은 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:13` |
| 201 | organization | OrganizationErrorCode | `SCHOOL_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0003` | 학교는 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:14` |
| 202 | organization | OrganizationErrorCode | `CHAPTER_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0004` | 지부는 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:15` |
| 203 | organization | OrganizationErrorCode | `GISU_START_AT_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0005` | 기수 시작일은 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:18` |
| 204 | organization | OrganizationErrorCode | `GISU_END_AT_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0006` | 기수 종료일은 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:19` |
| 205 | organization | OrganizationErrorCode | `GISU_PERIOD_INVALID` | BAD_REQUEST | `ORGANIZATION-0007` | 기수 시작일은 종료일보다 이전이어야 합니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:20` |
| 206 | organization | OrganizationErrorCode | `SCHOOL_NAME_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0008` | 학교 이름은 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:22` |
| 207 | organization | OrganizationErrorCode | `SCHOOL_DOMAIN_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0009` | 학교 이메일 도메인은 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:23` |
| 208 | organization | OrganizationErrorCode | `STUDY_GROUP_NAME_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0010` | 스터디 그룹 이름은 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:25` |
| 209 | organization | OrganizationErrorCode | `STUDY_GROUP_LEADER_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0011` | 스터디 그룹 리더는 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:26` |
| 210 | organization | OrganizationErrorCode | `STUDY_GROUP_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0012` | 스터디 그룹은 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:28` |
| 211 | organization | OrganizationErrorCode | `STUDY_GROUP_MEMBER_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0013` | 스터디 그룹 멤버는 최소 1명 이상이어야 합니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:29` |
| 212 | organization | OrganizationErrorCode | `STUDY_GROUP_MEMBER_ID_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0014` | 스터디 그룹을 만들 때 Member ID는 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:30` |
| 213 | organization | OrganizationErrorCode | `STUDY_GROUP_MEMBER_ALREADY_EXISTS` | BAD_REQUEST | `ORGANIZATION-0015` | 이미 존재하는 스터디 그룹 멤버입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:31` |
| 214 | organization | OrganizationErrorCode | `STUDY_GROUP_MEMBER_NOT_FOUND` | NOT_FOUND | `ORGANIZATION-0016` | 스터디 그룹 멤버를 찾을 수 없습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:32` |
| 215 | organization | OrganizationErrorCode | `CHAPTER_NOT_FOUND` | NOT_FOUND | `ORGANIZATION-0017` | 지부를 찾을 수 없습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:34` |
| 216 | organization | OrganizationErrorCode | `SCHOOL_NOT_FOUND` | NOT_FOUND | `ORGANIZATION-0018` | 학교를 찾을 수 없습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:35` |
| 217 | organization | OrganizationErrorCode | `GISU_IS_ACTIVE_NOT_FOUND` | NOT_FOUND | `ORGANIZATION-0019` | 활성화된 기수를 찾을 수 없습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:36` |
| 218 | organization | OrganizationErrorCode | `GISU_NOT_FOUND` | NOT_FOUND | `ORGANIZATION-0020` | 기수를 찾을 수 없습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:37` |
| 219 | organization | OrganizationErrorCode | `PART_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0021` | 파트는 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:38` |
| 220 | organization | OrganizationErrorCode | `STUDY_GROUP_NAME_INVALID` | BAD_REQUEST | `ORGANIZATION-0022` | 유효하지 않은 스터디 그룹 이름입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:39` |
| 221 | organization | OrganizationErrorCode | `STUDY_GROUP_NOT_FOUND` | BAD_REQUEST | `ORGANIZATION-0023` | 스터디 그룹을 찾을 수 없습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:40` |
| 222 | organization | OrganizationErrorCode | `STUDY_GROUP_CHALLENGER_INVALID` | BAD_REQUEST | `ORGANIZATION-0024` | 스터디 그룹의 리더 또는 멤버로 존재하지 않는 챌린저가 포함되어 있습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:42` |
| 223 | organization | OrganizationErrorCode | `LEADER_CANNOT_BE_MEMBER` | BAD_REQUEST | `ORGANIZATION-0025` | 스터디 그룹의 리더는 멤버가 될 수 없습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:44` |
| 224 | organization | OrganizationErrorCode | `STUDY_GROUP_MEMBER_DUPLICATED` | BAD_REQUEST | `ORGANIZATION-0026` | 스터디 그룹 멤버 ID에 중복이 있습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:45` |
| 225 | organization | OrganizationErrorCode | `NO_SUCH_CHAPTER_SCHOOL` | NOT_FOUND | `ORGANIZATION-0027` | 요청하신 학교와 지부와 일치하는 정보가 없습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:46` |
| 226 | organization | OrganizationErrorCode | `GISU_ALREADY_EXISTS` | CONFLICT | `ORGANIZATION-0028` | 이미 존재하는 기수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:47` |
| 227 | organization | OrganizationErrorCode | `SCHOOL_ALREADY_ASSIGNED_TO_CHAPTER` | CONFLICT | `ORGANIZATION-0029` | 해당 기수에서 이미 다른 지부에 배정된 학교가 포함되어 있습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:48` |
| 228 | organization | OrganizationErrorCode | `CHAPTER_NAME_DUPLICATED` | CONFLICT | `ORGANIZATION-0030` | 해당 기수에 동일한 이름의 지부가 이미 존재합니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:50` |
| 229 | organization | OrganizationErrorCode | `STUDY_GROUP_ACCESS_DENIED` | FORBIDDEN | `ORGANIZATION-0031` | 스터디 그룹 조회 권한이 없습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:52` |
| 230 | organization | OrganizationErrorCode | `GISU_HAS_ASSOCIATED_CHAPTERS` | CONFLICT | `ORGANIZATION-0032` | 해당 기수에 연결된 지부 또는 학교가 존재하여 삭제할 수 없습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:53` |
| 231 | organization | OrganizationErrorCode | `STUDY_GROUP_MENTOR_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0033` | 스터디 그룹 파트장은 최소 1명 이상이어야 합니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:55` |
| 232 | organization | OrganizationErrorCode | `STUDY_GROUP_MENTOR_ID_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0034` | 스터디 그룹 멘토의 ID는 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:56` |
| 233 | organization | OrganizationErrorCode | `STUDY_GROUP_MEMBER_ALREADY_IN_PART_STUDY` | CONFLICT | `ORGANIZATION-0035` | 다른 스터디 그룹과 중복된 멤버가 있습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:58` |
| 234 | organization | OrganizationErrorCode | `STUDY_GROUP_MENTOR_DUPLICATED` | BAD_REQUEST | `ORGANIZATION-0036` | 이미 해당 스터디에 속한 파트장입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:59` |
| 235 | organization | OrganizationErrorCode | `STUDY_GROUP_MENTOR_NOT_FOUND` | NOT_FOUND | `ORGANIZATION-0037` | 스터디 그룹의 파트장 정보를 찾을 수 없습니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:60` |
| 236 | organization | OrganizationErrorCode | `STUDY_GROUP_SCHEDULE_ATTENDANCE_POLICY_REQUIRED` | BAD_REQUEST | `ORGANIZATION-0038` | 스터디 그룹 일정은 출석 정책이 필수입니다. | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:62` |

## project

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 237 | project | ProjectErrorCode | `PROJECT_NOT_FOUND` | NOT_FOUND | `PROJECT-0001` | 프로젝트를 찾을 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:13` |
| 238 | project | ProjectErrorCode | `ALREADY_COMPLETED_PROJECT` | BAD_REQUEST | `PROJECT-0002` | 이미 완료된 프로젝트입니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:14` |
| 239 | project | ProjectErrorCode | `PROJECT_ABORT_UNAVAILABLE` | BAD_REQUEST | `PROJECT-0003` | 해당 프로젝트를 해산시킬 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:15` |
| 240 | project | ProjectErrorCode | `APPLICATION_NOT_SUBMITTED` | BAD_REQUEST | `PROJECT-0004` | 요청하신 조작은 지원서가 제출된 상태에서만 가능합니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:18` |
| 241 | project | ProjectErrorCode | `APPLICATION_SUBMIT_NOT_AVAILABLE` | BAD_REQUEST | `PROJECT-0005` | 이미 지원서가 제출되었거나 평가가 완료된 상태입니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:19` |
| 242 | project | ProjectErrorCode | `APPLICATION_FORM_NOT_FOUND` | NOT_FOUND | `PROJECT-0006` | 프로젝트에서 해당 지원용 폼을 찾을 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:27` |
| 243 | project | ProjectErrorCode | `APPLICATION_FORM_ACCESS_NOT_ALLOWED` | FORBIDDEN | `PROJECT-0007` | 요청하신 지원용 폼 섹션에 접근 권한이 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:28` |
| 244 | project | ProjectErrorCode | `PROJECT_DRAFT_ALREADY_IN_PROGRESS` | CONFLICT | `PROJECT-0008` | 작성 중인 DRAFT 프로젝트가 있어 새로 시작할 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:37` |
| 245 | project | ProjectErrorCode | `PROJECT_INVALID_STATE` | BAD_REQUEST | `PROJECT-0009` | 현재 상태에서 수행할 수 없는 작업입니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:38` |
| 246 | project | ProjectErrorCode | `PROJECT_OWNER_NOT_PLAN_CHALLENGER` | BAD_REQUEST | `PROJECT-0010` | 프로젝트 PO는 PLAN 파트 챌린저여야 합니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:39` |
| 247 | project | ProjectErrorCode | `PROJECT_SUBMIT_VALIDATION_FAILED` | BAD_REQUEST | `PROJECT-0011` | 제출에 필요한 필수 정보가 누락되었습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:40` |
| 248 | project | ProjectErrorCode | `PROJECT_ACCESS_DENIED` | FORBIDDEN | `PROJECT-0012` | 해당 프로젝트에 대한 접근 권한이 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:41` |
| 249 | project | ProjectErrorCode | `APPLICATION_FORM_POLICY_PARTS_EMPTY` | BAD_REQUEST | `PROJECT-0013` | PART 타입 섹션은 1개 이상의 파트를 지정해야 합니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:29` |
| 250 | project | ProjectErrorCode | `APPLICATION_FORM_INVALID_SECTION_ID` | BAD_REQUEST | `PROJECT-0014` | 현재 폼에 존재하지 않는 sectionId 입니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:30` |
| 251 | project | ProjectErrorCode | `APPLICATION_FORM_INVALID_QUESTION_ID` | BAD_REQUEST | `PROJECT-0015` | 해당 섹션에 속하지 않는 questionId 입니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:31` |
| 252 | project | ProjectErrorCode | `APPLICATION_FORM_INVALID_OPTION_ID` | BAD_REQUEST | `PROJECT-0016` | 해당 질문에 속하지 않는 optionId 입니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:32` |
| 253 | project | ProjectErrorCode | `APPLICATION_FORM_OPTIONS_NOT_ALLOWED` | BAD_REQUEST | `PROJECT-0017` | 선택지 타입(RADIO/CHECKBOX/DROPDOWN)이 아닌 질문에는 옵션을 지정할 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:33` |
| 254 | project | ProjectErrorCode | `APPLICATION_FORM_OPTIONS_REQUIRED` | BAD_REQUEST | `PROJECT-0018` | 선택지 타입 질문에는 1개 이상의 옵션이 필요합니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:34` |
| 255 | project | ProjectErrorCode | `APPLICATION_DRAFT_NOT_EXPOSABLE` | INTERNAL_SERVER_ERROR | `PROJECT-0019` | 임시저장 상태의 지원서는 PM/운영진 응답에 매핑할 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:20` |
| 256 | project | ProjectErrorCode | `APPLICATION_DRAFT_FILTER_NOT_ALLOWED` | BAD_REQUEST | `PROJECT-0020` | 임시저장(DRAFT)은 PM/운영진 지원자 목록 조회 필터로 사용할 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:22` |
| 257 | project | ProjectErrorCode | `PROJECT_APPLICATION_NOT_FOUND` | NOT_FOUND | `PROJECT-0021` | 지원서를 찾을 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:24` |
| 258 | project | ProjectErrorCode | `PROJECT_MEMBER_NOT_FOUND` | NOT_FOUND | `PROJECT-0100` | 프로젝트 멤버를 찾을 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:44` |
| 259 | project | ProjectErrorCode | `PROJECT_MEMBER_ALREADY_EXISTS` | CONFLICT | `PROJECT-0101` | 이미 해당 프로젝트의 멤버입니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:45` |
| 260 | project | ProjectErrorCode | `PROJECT_MAIN_PM_REMOVAL_REQUIRES_TRANSFER` | BAD_REQUEST | `PROJECT-0102` | 메인 PM 은 팀원 제거가 아닌 소유권 양도 API 로 변경해야 합니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:46` |
| 261 | project | ProjectErrorCode | `PROJECT_PART_QUOTA_INVALID` | BAD_REQUEST | `PROJECT-0200` | 파트 정원은 1 이상이어야 합니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:49` |
| 262 | project | ProjectErrorCode | `PROJECT_PART_QUOTA_REQUIRED` | BAD_REQUEST | `PROJECT-0202` | 공개하려면 파트별 정원이 1개 이상 등록되어 있어야 합니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:50` |
| 263 | project | ProjectErrorCode | `PROJECT_PART_QUOTA_DUPLICATE` | BAD_REQUEST | `PROJECT-0203` | 동일 파트가 중복으로 입력되었습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:51` |
| 264 | project | ProjectErrorCode | `PROJECT_DRAFT_APPLICATION_NOT_FOUND` | NOT_FOUND | `PROJECT-0204` | 작성 중인 지원서를 찾을 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:73` |
| 265 | project | ProjectErrorCode | `PROJECT_APPLICATION_PART_NOT_ALLOWED` | FORBIDDEN | `PROJECT-0205` | 해당 프로젝트에 지원 가능한 파트가 아닙니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:74` |
| 266 | project | ProjectErrorCode | `PROJECT_APPLICATION_MEMBER_ALREADY_IN_TEAM` | CONFLICT | `PROJECT-0206` | 이미 해당 기수에 소속된 팀이 있어 지원할 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:75` |
| 267 | project | ProjectErrorCode | `PROJECT_APPLICATION_DUPLICATE_SUBMISSION` | CONFLICT | `PROJECT-0207` | 동일한 매칭 차수에 이미 제출된 지원서가 있습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:76` |
| 268 | project | ProjectErrorCode | `PROJECT_APPLICATION_ROUND_NOT_OPEN` | BAD_REQUEST | `PROJECT-0208` | 해당 매칭 차수의 지원 기간이 아닙니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:77` |
| 269 | project | ProjectErrorCode | `PROJECT_APPLICATION_ROUND_TYPE_MISMATCH` | BAD_REQUEST | `PROJECT-0209` | 선택한 매칭 차수가 본인 파트에 해당하지 않습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:78` |
| 270 | project | ProjectErrorCode | `PROJECT_APPLICATION_ALREADY_EXISTS` | CONFLICT | `PROJECT-0210` | 이미 작성 중인 지원서가 있습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:79` |
| 271 | project | ProjectErrorCode | `PROJECT_APPLICATION_SELF_APPLY_NOT_ALLOWED` | FORBIDDEN | `PROJECT-0211` | 본인이 운영하는 프로젝트에는 지원할 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:80` |
| 272 | project | ProjectErrorCode | `PROJECT_APPLICATION_DECISION_INVALID_TRANSITION` | BAD_REQUEST | `PROJECT-0212` | 현재 상태에서는 합/불 결정을 변경할 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:81` |
| 273 | project | ProjectErrorCode | `PROJECT_APPLICATION_QUOTA_EXCEEDED` | CONFLICT | `PROJECT-0213` | 해당 파트의 남은 자리를 초과하여 합격 처리할 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:83` |
| 274 | project | ProjectErrorCode | `PROJECT_APPLICATION_CANCEL_NOT_ALLOWED` | BAD_REQUEST | `PROJECT-0214` | 이미 종결된 지원서는 철회할 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:85` |
| 275 | project | ProjectErrorCode | `PROJECT_APPLICATION_CANCEL_ROUND_CLOSED` | BAD_REQUEST | `PROJECT-0215` | 매칭 차수가 종료되어 지원서를 철회할 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:86` |
| 276 | project | ProjectErrorCode | `PROJECT_MATCHING_ROUND_NOT_FOUND` | NOT_FOUND | `PROJECT-0300` | 매칭 차수를 찾을 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:54` |
| 277 | project | ProjectErrorCode | `PROJECT_MATCHING_ROUND_INVALID_PERIOD` | BAD_REQUEST | `PROJECT-0301` | 매칭 차수 기간은 startsAt < endsAt < decisionDeadline 순서여야 합니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:55` |
| 278 | project | ProjectErrorCode | `PROJECT_MATCHING_ROUND_PERIOD_OVERLAPPED` | CONFLICT | `PROJECT-0302` | 같은 지부 내에서는 매칭 차수 기간이 중복될 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:57` |
| 279 | project | ProjectErrorCode | `PROJECT_MATCHING_ROUND_ACCESS_DENIED` | FORBIDDEN | `PROJECT-0303` | 해당 매칭 차수에 대한 관리 권한이 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:59` |
| 280 | project | ProjectErrorCode | `PROJECT_MATCHING_ROUND_DELETE_CONFLICT` | CONFLICT | `PROJECT-0304` | 연관된 지원서가 있는 매칭 차수는 삭제할 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:61` |
| 281 | project | ProjectErrorCode | `PROJECT_MATCHING_ROUND_TIME_REQUIRES_CHAPTER` | BAD_REQUEST | `PROJECT-0305` | time 기준 조회는 chapterId와 함께 요청해야 합니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:63` |
| 282 | project | ProjectErrorCode | `PROJECT_MATCHING_ROUND_LOCKED` | BAD_REQUEST | `PROJECT-0306` | 매칭 차수가 종료되어 더 이상 결정을 변경할 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:65` |
| 283 | project | ProjectErrorCode | `PROJECT_MATCHING_ROUND_NOT_FINALIZABLE` | BAD_REQUEST | `PROJECT-0307` | 결정 마감 시각이 지나기 전에는 자동 선발을 실행할 수 없습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:67` |
| 284 | project | ProjectErrorCode | `PROJECT_MATCHING_ROUND_POLICY_NOT_FOUND` | INTERNAL_SERVER_ERROR | `PROJECT-0308` | 해당 매칭 종류에 대한 자동 선발 정책이 정의되지 않았습니다. | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:69` |

## schedule

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 285 | schedule | ScheduleErrorCode | `INVALID_TIME_RANGE` | BAD_REQUEST | `SCHEDULE-0006` | 시작 시간은 종료 시간보다 이전이어야 합니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:12` |
| 286 | schedule | ScheduleErrorCode | `SCHEDULE_NOT_FOUND` | NOT_FOUND | `SCHEDULE-0009` | 일정을 찾을 수 없습니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:14` |
| 287 | schedule | ScheduleErrorCode | `TAG_REQUIRED` | BAD_REQUEST | `SCHEDULE-0010` | 태그는 최소 1개 이상 선택해야 합니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:16` |
| 288 | schedule | ScheduleErrorCode | `NOT_FIRST_ATTENDANCE_REQUEST` | BAD_REQUEST | `SCHEDULE-0011` | 기존 출석 요청이 존재합니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:18` |
| 289 | schedule | ScheduleErrorCode | `NO_ATTENDANCE_RECORD` | NOT_FOUND | `SCHEDULE-0012` | 출석 요청이 존재하지 않습니다. 출석 요청을 생성하고 다시 시도해주세요. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:20` |
| 290 | schedule | ScheduleErrorCode | `INVALID_ATTENDANCE_STATUS_FOR_EXCUSE` | BAD_REQUEST | `SCHEDULE-0013` | 출석 사유 제출은 첫 요청, 결석 또는 지각 상태에서만 가능합니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:22` |
| 291 | schedule | ScheduleErrorCode | `INVALID_ATTENDANCE_STATUS_FOR_APPROVAL` | BAD_REQUEST | `SCHEDULE-0014` | 현재 출석 상태에서는 승인이 불가능합니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:25` |
| 292 | schedule | ScheduleErrorCode | `INVALID_ATTENDANCE_STATUS_FOR_REJECT` | BAD_REQUEST | `SCHEDULE-0015` | 출석 요청에 대한 거절을 할 수 없는 상태입니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:27` |
| 293 | schedule | ScheduleErrorCode | `NO_EXCUSE_REASON_GIVEN` | BAD_REQUEST | `SCHEDULE-0016` | 출석 인정을 요청하는 사유가 제공되지 않았거나 비어있습니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:29` |
| 294 | schedule | ScheduleErrorCode | `ATTENDANCE_NOT_REQUIRES_CONFIRM` | BAD_REQUEST | `SCHEDULE-0017` | 해당 출석 요청은 운영진의 승인 또는 기각을 필요로 하는 상태가 아닙니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:31` |
| 295 | schedule | ScheduleErrorCode | `SCHEDULE_ENDED` | BAD_REQUEST | `SCHEDULE-0018` | 종료된 일정에 대한 출석 요청은 허용되지 않습니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:34` |
| 296 | schedule | ScheduleErrorCode | `CHECK_IN_TOO_EARLY` | BAD_REQUEST | `SCHEDULE-0019` | 출석 가능한 시간 이전입니다. 출석 가능한 시간 이후에 다시 시도해주세요. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:36` |
| 297 | schedule | ScheduleErrorCode | `OFFLINE_SCHEDULE_REQUIRES_LOCATION` | BAD_REQUEST | `SCHEDULE-0020` | 대면 일정은 위치 정보가 필수입니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:38` |
| 298 | schedule | ScheduleErrorCode | `SCHEDULE_ATTENDANCE_POLICY_NOT_EXIST` | BAD_REQUEST | `SCHEDULE-0021` | 출석 정책이 존재하지 않아 출석 요청이 불가능한 일정입니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:40` |
| 299 | schedule | ScheduleErrorCode | `PARTICIPANT_NOT_FOUND` | BAD_REQUEST | `SCHEDULE-0022` | 일정에 대한 참석자 정보가 존재하지 않습니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:42` |
| 300 | schedule | ScheduleErrorCode | `LOCATION_NOT_VERIFIED` | BAD_REQUEST | `SCHEDULE-0023` | 사용자의 출석 인증 범위 내의 존재 여부가 확인되지 않습니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:44` |
| 301 | schedule | ScheduleErrorCode | `ONLINE_SCHEDULE_SHOULD_NOT_HAVE_LOCATION` | BAD_REQUEST | `SCHEDULE-0024` | 비대면 일정으로 변경 시 위치 정보를 포함할 수 없습니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:46` |
| 302 | schedule | ScheduleErrorCode | `NOT_ACTIVE_GISU_SCHEDULE` | BAD_REQUEST | `SCHEDULE-0025` | 현재 기수의 일정만 생성할 수 있습니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:49` |
| 303 | schedule | ScheduleErrorCode | `NOT_SCHEDULE_PARTICIPANT` | BAD_REQUEST | `SCHEDULE-0026` | 일정의 참여자가 아닙니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:52` |
| 304 | schedule | ScheduleErrorCode | `ATTENDANCE_POLICY_REQUIRED` | BAD_REQUEST | `SCHEDULE-0027` | 출석을 요하는 일정의 출석 정책은 필수입니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:54` |
| 305 | schedule | ScheduleErrorCode | `STARTED_SCHEDULE_CANT_BE_EDITED` | BAD_REQUEST | `SCHEDULE-0028` | 시작된 일정은 수정이 불가합니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:56` |
| 306 | schedule | ScheduleErrorCode | `CANNOT_CREATE_SCHEDULE` | FORBIDDEN | `SCHEDULE-0029` | 일정을 생성할 수 없습니다. 챌린저 활동 이력이 필요합니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:58` |
| 307 | schedule | ScheduleErrorCode | `EXCEEDED_MAX_PARTICIPANTS` | BAD_REQUEST | `SCHEDULE-0030` | 초대 가능한 최대 참여자 수를 초과했습니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:60` |
| 308 | schedule | ScheduleErrorCode | `CANNOT_CREATE_ATTENDANCE_REQUIRED_SCHEDULE` | FORBIDDEN | `SCHEDULE-0031` | 출석을 요하는 일정을 생성할 권한이 없습니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:62` |
| 309 | schedule | ScheduleErrorCode | `INVALID_MEMBER_INVITE` | BAD_REQUEST | `SCHEDULE-0032` | 초대하려는 참여자에 유효하지 않은 사용자가 포함되어 있습니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:64` |
| 310 | schedule | ScheduleErrorCode | `SCHEDULE_HAS_ATTENDANCE_RECORD` | BAD_REQUEST | `SCHEDULE-0033` | 출석 기록이 존재하는 일정은 삭제할 수 없습니다. | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:66` |

## storage

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 311 | storage | StorageErrorCode | `FILE_NOT_FOUND` | NOT_FOUND | `STORAGE-0001` | 파일을 찾을 수 없습니다. | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:13` |
| 312 | storage | StorageErrorCode | `FILE_UPLOAD_NOT_COMPLETED` | BAD_REQUEST | `STORAGE-0002` | 파일 업로드가 완료되지 않았습니다. | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:14` |
| 313 | storage | StorageErrorCode | `FILE_ALREADY_UPLOADED` | BAD_REQUEST | `STORAGE-0003` | 이미 업로드가 완료된 파일입니다. | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:15` |
| 314 | storage | StorageErrorCode | `INVALID_FILE_EXTENSION` | BAD_REQUEST | `STORAGE-0004` | 허용되지 않는 파일 확장자입니다. | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:18` |
| 315 | storage | StorageErrorCode | `FILE_SIZE_EXCEEDED` | BAD_REQUEST | `STORAGE-0005` | 파일 크기가 허용 범위를 초과했습니다. | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:19` |
| 316 | storage | StorageErrorCode | `INVALID_CONTENT_TYPE` | BAD_REQUEST | `STORAGE-0006` | 잘못된 Content-Type입니다. | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:20` |
| 317 | storage | StorageErrorCode | `STORAGE_UPLOAD_FAILED` | INTERNAL_SERVER_ERROR | `STORAGE-0007` | 파일 업로드에 실패했습니다. | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:23` |
| 318 | storage | StorageErrorCode | `STORAGE_DELETE_FAILED` | INTERNAL_SERVER_ERROR | `STORAGE-0008` | 파일 삭제에 실패했습니다. | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:24` |
| 319 | storage | StorageErrorCode | `STORAGE_URL_GENERATION_FAILED` | INTERNAL_SERVER_ERROR | `STORAGE-0009` | 파일 접근 URL 생성에 실패했습니다. | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:25` |
| 320 | storage | StorageErrorCode | `CDN_SIGNING_FAILED` | INTERNAL_SERVER_ERROR | `STORAGE-0010` | CDN Signed URL 생성에 실패했습니다. | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:28` |
| 321 | storage | StorageErrorCode | `NO_ENV_KEYS` | INTERNAL_SERVER_ERROR | `STORAGE-0011` | CDN이 활성화되어 있지만 관련 환경변수가 설정되어 있지 않습니다. 관리자에게 문의하세요. | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:29` |
| 322 | storage | StorageErrorCode | `INVALID_SPRING_PROFILE` | INTERNAL_SERVER_ERROR | `STORAGE-0012` | 올바르지 않은 서버 실행 환경입니다. 관리자에게 문의하세요. | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:30` |

## survey

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 323 | survey | SurveyErrorCode | `SURVEY_NOT_FOUND` | NOT_FOUND | `SURVEY-0001` | 폼을 찾을 수 없습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:12` |
| 324 | survey | SurveyErrorCode | `SURVEY_NOT_DRAFT` | CONFLICT | `SURVEY-0002` | 임시저장 상태의 폼만 편집할 수 있습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:13` |
| 325 | survey | SurveyErrorCode | `QUESTION_NOT_FOUND` | NOT_FOUND | `SURVEY-0003` | 질문을 찾을 수 없습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:14` |
| 326 | survey | SurveyErrorCode | `FORM_RESPONSE_NOT_FOUND` | NOT_FOUND | `SURVEY-0006` | 폼 응답을 찾을 수 없습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:16` |
| 327 | survey | SurveyErrorCode | `QUESTION_IS_NOT_OWNED_BY_FORM` | BAD_REQUEST | `SURVEY-0007` | 질문이 해당 폼의 질문이 아닙니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:17` |
| 328 | survey | SurveyErrorCode | `FORM_RESPONSE_FORBIDDEN` | FORBIDDEN | `SURVEY-0008` | 해당 폼 응답에 접근할 수 있는 권한이 없습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:18` |
| 329 | survey | SurveyErrorCode | `QUESTION_TYPE_MISMATCH` | BAD_REQUEST | `SURVEY-0009` | 질문 유형이 일치하지 않습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:19` |
| 330 | survey | SurveyErrorCode | `REQUIRED_QUESTION_NOT_ANSWERED` | BAD_REQUEST | `SURVEY-0010` | 필수 질문에 대한 응답이 누락되었습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:20` |
| 331 | survey | SurveyErrorCode | `INVALID_ANSWER_FORMAT` | BAD_REQUEST | `SURVEY-0011` | 응답 형식이 올바르지 않습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:21` |
| 332 | survey | SurveyErrorCode | `OTHER_OPTION_DUPLICATED` | BAD_REQUEST | `SURVEY-0012` | '기타' 선택지가 중복되었습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:22` |
| 333 | survey | SurveyErrorCode | `OPTION_NOT_IN_QUESTION` | BAD_REQUEST | `SURVEY-0013` | 선택지가 해당 질문의 선택지에 포함되지 않습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:23` |
| 334 | survey | SurveyErrorCode | `OPTION_TEXT_REQUIRED` | BAD_REQUEST | `SURVEY-0014` | '기타' 선택지의 텍스트는 필수입니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:24` |
| 335 | survey | SurveyErrorCode | `INVALID_FORM_ACTIVE_PERIOD` | BAD_REQUEST | `SURVEY-0015` | 폼의 응답 가능 기간이 올바르지 않습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:25` |
| 336 | survey | SurveyErrorCode | `INVALID_VOTE_SELECTION` | BAD_REQUEST | `SURVEY-0023` | 선택이 올바르지 않습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:27` |
| 337 | survey | SurveyErrorCode | `INVALID_VOTE_FORM_STRUCTURE` | BAD_REQUEST | `SURVEY-0025` | 투표의 질문 형식이 올바르지 않습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:29` |
| 338 | survey | SurveyErrorCode | `FORM_RESPONSE_ALREADY_EXISTS` | BAD_REQUEST | `SURVEY-0027` | 이미 제출한 응답이 있습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:31` |
| 339 | survey | SurveyErrorCode | `SURVEY_NOT_PUBLISHED` | CONFLICT | `SURVEY-0028` | 발행된 폼만 응답할 수 있습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:32` |
| 340 | survey | SurveyErrorCode | `QUESTION_OPTION_NOT_FOUND` | NOT_FOUND | `SURVEY-0029` | 선택지를 찾을 수 없습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:33` |
| 341 | survey | SurveyErrorCode | `ANSWER_NOT_FOUND` | NOT_FOUND | `SURVEY-0030` | 답변을 찾을 수 없습니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:34` |
| 342 | survey | SurveyErrorCode | `FORM_RESPONSE_NOT_DRAFT` | CONFLICT | `SURVEY-0031` | 임시저장 상태의 응답에만 가능한 작업입니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:35` |
| 343 | survey | SurveyErrorCode | `ANSWER_ALREADY_EXISTS` | BAD_REQUEST | `SURVEY-0032` | 이미 해당 질문에 대한 답변이 존재합니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:36` |
| 344 | survey | SurveyErrorCode | `SURVEY_ALREADY_PUBLISHED` | BAD_REQUEST | `SURVEY-005` | 이미 발행된 폼입니다. | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:15` |

## term

| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |
|---:|---|---|---|---|---|---|---|
| 345 | term | TermErrorCode | `TERMS_NOT_FOUND` | NOT_FOUND | `TERMS-0001` | 약관을 찾을 수 없습니다. | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:12` |
| 346 | term | TermErrorCode | `TERMS_TYPE_REQUIRED` | BAD_REQUEST | `TERMS-0002` | 약관 타입은 필수입니다. | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:13` |
| 347 | term | TermErrorCode | `TERMS_TITLE_REQUIRED` | BAD_REQUEST | `TERMS-0003` | 약관 제목은 필수입니다. | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:14` |
| 348 | term | TermErrorCode | `TERMS_CONTENT_REQUIRED` | BAD_REQUEST | `TERMS-0004` | 약관 내용은 필수입니다. | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:15` |
| 349 | term | TermErrorCode | `TERMS_VERSION_REQUIRED` | BAD_REQUEST | `TERMS-0005` | 약관 버전은 필수입니다. | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:16` |
| 350 | term | TermErrorCode | `TERMS_CONSENT_NOT_FOUND` | NOT_FOUND | `TERMS-0006` | 약관 동의 정보를 찾을 수 없습니다. | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:18` |
| 351 | term | TermErrorCode | `TERMS_CONSENT_ALREADY_EXISTS` | BAD_REQUEST | `TERMS-0007` | 이미 동의한 약관입니다. | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:19` |
| 352 | term | TermErrorCode | `MEMBER_ID_REQUIRED` | BAD_REQUEST | `TERMS-0008` | 회원 ID는 필수입니다. | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:20` |
| 353 | term | TermErrorCode | `TERM_ID_REQUIRED` | BAD_REQUEST | `TERMS-0009` | 약관 ID는 필수입니다. | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:21` |
| 354 | term | TermErrorCode | `MANDATORY_TERMS_NOT_AGREED` | BAD_REQUEST | `TERMS-0010` | 필수 약관에 모두 동의해야 합니다. | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:22` |
| 355 | term | TermErrorCode | `TERM_PERMISSION_DENIED` | FORBIDDEN | `TERMS-0011` | 해당 작업을 수행할 권한이 없습니다. | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:24` |

