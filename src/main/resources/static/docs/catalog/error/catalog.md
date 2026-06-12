# ErrorCode Catalog

서버가 반환하는 ErrorCode를 도메인, HTTP 상태, 코드, 메시지 기준으로 정리합니다.

> 소스 기준: 각 도메인의 `*ErrorCode.java` enum을 스캔합니다. 갱신: `./gradlew generateDocumentationCatalogs`

## analytics

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 1 | analytics | `ANALYTICS-0001` | 403 FORBIDDEN | 운영진 대시보드는 권한이 있는 운영진만 볼 수 있어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | AnalyticsErrorCode | `RESOURCE_ACCESS_DENIED` | `src/main/java/com/umc/product/analytics/domain/AnalyticsErrorCode.java:13` |
| 2 | analytics | `ANALYTICS-0002` | 400 BAD_REQUEST | 지원하지 않는 정렬 조건이에요. 정렬 값을 확인해주세요. | AnalyticsErrorCode | `INVALID_SORT` | `src/main/java/com/umc/product/analytics/domain/AnalyticsErrorCode.java:15` |
| 3 | analytics | `ANALYTICS-0003` | 400 BAD_REQUEST | 조회 시작 시각은 종료 시각보다 빨라야 해요. 기간을 다시 선택해주세요. | AnalyticsErrorCode | `INVALID_PERIOD` | `src/main/java/com/umc/product/analytics/domain/AnalyticsErrorCode.java:16` |

## authentication

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 4 | authentication | `AUTHENTICATION-0001` | 400 BAD_REQUEST | 지원하지 않는 로그인 방식이에요. 다른 방식을 선택해주세요. | AuthenticationErrorCode | `OAUTH_PROVIDER_NOT_FOUND` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:22` |
| 5 | authentication | `AUTHENTICATION-0002` | 404 NOT_FOUND | 가입된 계정을 찾을 수 없어요. 회원가입을 먼저 진행해주세요. | AuthenticationErrorCode | `NO_MATCHING_MEMBER` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:23` |
| 6 | authentication | `AUTHENTICATION-0003` | 400 BAD_REQUEST | 이메일 인증 요청이 올바르지 않아요. 인증을 다시 요청해주세요. | AuthenticationErrorCode | `NO_EMAIL_VERIFICATION_METHOD_GIVEN` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:25` |
| 7 | authentication | `AUTHENTICATION-0004` | 401 UNAUTHORIZED | 이메일 인증 정보가 맞지 않아요. 인증 메일을 다시 확인해주세요. | AuthenticationErrorCode | `INVALID_EMAIL_VERIFICATION` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:27` |
| 8 | authentication | `AUTHENTICATION-0006` | 404 NOT_FOUND | 가입된 계정을 찾을 수 없어요. 회원가입을 먼저 진행해주세요. | AuthenticationErrorCode | `OAUTH_SUCCESS_BUT_NO_MEMBER` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:31` |
| 9 | authentication | `AUTHENTICATION-0007` | 503 SERVICE_UNAVAILABLE | 로그인에 필요한 정보를 받아오지 못했어요. 잠시 후 다시 시도해주세요. | AuthenticationErrorCode | `OAUTH_SUCCESS_BUT_NO_INFO` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:33` |
| 10 | authentication | `AUTHENTICATION-0008` | 400 BAD_REQUEST | OAuth 로그인에 실패했어요. 잠시 후 다시 시도해주세요. | AuthenticationErrorCode | `OAUTH_FAILURE` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:35` |
| 11 | authentication | `AUTHENTICATION-0009` | 400 BAD_REQUEST | OAuth 인증 정보가 올바르지 않아요. 다시 로그인해주세요. | AuthenticationErrorCode | `OAUTH_INVALID_ACCESS_TOKEN` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:36` |
| 12 | authentication | `AUTHENTICATION-0010` | 401 UNAUTHORIZED | OAuth 인증 정보를 확인하지 못했어요. 다시 로그인해주세요. | AuthenticationErrorCode | `OAUTH_TOKEN_VERIFICATION_FAILED` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:37` |
| 13 | authentication | `AUTHENTICATION-0011` | 401 UNAUTHORIZED | OAuth 인증 정보가 올바르지 않아요. 다시 로그인해주세요. | AuthenticationErrorCode | `INVALID_OAUTH_TOKEN` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:39` |
| 14 | authentication | `AUTHENTICATION-0012` | 401 UNAUTHORIZED | 이미 다른 계정에 연결된 OAuth 계정이에요. 연결된 계정을 확인해주세요. | AuthenticationErrorCode | `OAUTH_ALREADY_LINKED` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:40` |
| 15 | authentication | `AUTHENTICATION-0013` | 401 UNAUTHORIZED | 이미 연결된 OAuth 제공자예요. 기존 연결을 해제한 뒤 다시 시도해주세요. | AuthenticationErrorCode | `OAUTH_PROVIDER_ALREADY_LINKED` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:41` |
| 16 | authentication | `AUTHENTICATION-0014` | 404 NOT_FOUND | 연결된 OAuth 정보를 찾을 수 없어요. 다시 연결해주세요. | AuthenticationErrorCode | `MEMBER_OAUTH_NOT_FOUND` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:43` |
| 17 | authentication | `AUTHENTICATION-0015` | 403 FORBIDDEN | 이 작업을 할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | AuthenticationErrorCode | `NOT_VALID_MEMBER` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:44` |
| 18 | authentication | `AUTHENTICATION-0016` | 400 BAD_REQUEST | 계정에 연결된 유일한 OAuth는 해제할 수 없어요. 회원 탈퇴를 이용해주세요. | AuthenticationErrorCode | `OAUTH_CANNOT_UNLINK_LAST_PROVIDER` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:46` |
| 19 | authentication | `AUTHENTICATION-0017` | 400 BAD_REQUEST | 이미 인증이 끝난 이메일 인증 세션이에요. 다음 단계로 진행해주세요. | AuthenticationErrorCode | `ALREADY_VERIFIED_EMAIL` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:50` |
| 20 | authentication | `AUTHENTICATION-0018` | 400 BAD_REQUEST | 이메일 인증 세션이 만료됐어요. 새로운 인증을 요청해주세요. | AuthenticationErrorCode | `EMAIL_VERIFICATION_SESSION_EXPIRED` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:52` |
| 21 | authentication | `AUTHENTICATION-0019` | 409 CONFLICT | 이미 사용 중인 로그인 ID예요. 다른 ID를 입력해주세요. | AuthenticationErrorCode | `LOGIN_ID_ALREADY_EXISTS` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:58` |
| 22 | authentication | `AUTHENTICATION-0020` | 400 BAD_REQUEST | 로그인 ID는 영문, 숫자, ., _, -를 사용해 5~20자로 입력해주세요. | AuthenticationErrorCode | `INVALID_LOGIN_ID_FORMAT` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:59` |
| 23 | authentication | `AUTHENTICATION-0021` | 400 BAD_REQUEST | 비밀번호는 8~64자로 입력하고 영문, 숫자, 특수문자 중 2종류 이상을 포함해주세요. | AuthenticationErrorCode | `PASSWORD_POLICY_VIOLATION` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:61` |
| 24 | authentication | `AUTHENTICATION-0022` | 401 UNAUTHORIZED | 로그인 ID 또는 비밀번호가 올바르지 않아요. 다시 입력해주세요. | AuthenticationErrorCode | `INVALID_LOGIN_CREDENTIAL` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:64` |
| 25 | authentication | `AUTHENTICATION-0023` | 400 BAD_REQUEST | 선택한 OAuth 제공자는 이 인증 방식을 지원하지 않아요. 다른 로그인 방식을 사용해주세요. | AuthenticationErrorCode | `UNSUPPORTED_OAUTH_FLOW` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:72` |
| 26 | authentication | `AUTHENTICATION-0024` | 400 BAD_REQUEST | 허용되지 않은 OAuth redirect URI예요. 설정을 확인해주세요. | AuthenticationErrorCode | `INVALID_OAUTH_REDIRECT_URI` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:74` |
| 27 | authentication | `AUTHENTICATION-0025` | 400 BAD_REQUEST | 이메일 형식이 올바르지 않아요. 이메일 주소를 확인해주세요. | AuthenticationErrorCode | `INVALID_EMAIL_FORMAT` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:66` |
| 28 | authentication | `AUTHENTICATION-0026` | 409 CONFLICT | 이미 사용 중인 이메일이에요. 다른 이메일을 입력해주세요. | AuthenticationErrorCode | `EMAIL_ALREADY_EXISTS` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:68` |
| 29 | authentication | `AUTHENTICATION-0027` | 429 TOO_MANY_REQUESTS | 이메일 인증 요청이 너무 잦아요. 잠시 후 다시 시도해주세요. | AuthenticationErrorCode | `EMAIL_VERIFICATION_THROTTLED` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:54` |
| 30 | authentication | `JWT-0001` | 401 UNAUTHORIZED | 인증 정보가 올바르지 않아요. 다시 로그인해주세요. | AuthenticationErrorCode | `WRONG_JWT_SIGNATURE` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:16` |
| 31 | authentication | `JWT-0002` | 401 UNAUTHORIZED | 로그인이 만료됐어요. 다시 로그인해주세요. | AuthenticationErrorCode | `EXPIRED_JWT_TOKEN` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:17` |
| 32 | authentication | `JWT-0003` | 401 UNAUTHORIZED | 지원하지 않는 인증 정보예요. 다시 로그인해주세요. | AuthenticationErrorCode | `UNSUPPORTED_JWT` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:18` |
| 33 | authentication | `JWT-0004` | 401 UNAUTHORIZED | 인증 정보가 올바르지 않아요. 다시 로그인해주세요. | AuthenticationErrorCode | `INVALID_JWT` | `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java:19` |

## authorization

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 34 | authorization | `AUTHORIZATION-0001` | 403 FORBIDDEN | 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | AuthorizationErrorCode | `PERMISSION_DENIED` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:14` |
| 35 | authorization | `AUTHORIZATION-0002` | 403 FORBIDDEN | 이 항목에 접근할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | AuthorizationErrorCode | `RESOURCE_ACCESS_DENIED` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:16` |
| 36 | authorization | `AUTHORIZATION-0003` | 400 BAD_REQUEST | 권한 값이 올바르지 않아요. 요청 값을 확인해주세요. | AuthorizationErrorCode | `INVALID_PERMISSION` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:18` |
| 37 | authorization | `AUTHORIZATION-0004` | 500 INTERNAL_SERVER_ERROR | 권한을 확인하지 못했어요. 잠시 후 다시 시도해주세요. | AuthorizationErrorCode | `POLICY_EVALUATION_FAILED` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:19` |
| 38 | authorization | `AUTHORIZATION-0005` | 500 INTERNAL_SERVER_ERROR | 권한 확인 설정을 찾지 못했어요. 관리자에게 문의해주세요. | AuthorizationErrorCode | `NO_EVALUATOR_MATCHING_RESOURCE_TYPE` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:21` |
| 39 | authorization | `AUTHORIZATION-0006` | 500 INTERNAL_SERVER_ERROR | 지원하지 않는 권한 유형이에요. 관리자에게 문의해주세요. | AuthorizationErrorCode | `PERMISSION_TYPE_NOT_SUPPORTED_BY_RESOURCE_TYPE` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:23` |
| 40 | authorization | `AUTHORIZATION-0007` | 400 BAD_REQUEST | 권한 확인 요청이 올바르지 않아요. 요청 값을 확인해주세요. | AuthorizationErrorCode | `INVALID_INPUT_VALUE` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:25` |
| 41 | authorization | `AUTHORIZATION-0008` | 400 BAD_REQUEST | 권한을 확인할 항목 ID가 올바르지 않아요. 요청 값을 확인해주세요. | AuthorizationErrorCode | `INVALID_RESOURCE_ID_TYPE` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:26` |
| 42 | authorization | `AUTHORIZATION-0009` | 500 INTERNAL_SERVER_ERROR | 권한 확인 요청을 처리하지 못했어요. 관리자에게 문의해주세요. | AuthorizationErrorCode | `INVALID_RESOURCE_PERMISSION_GIVEN` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:28` |
| 43 | authorization | `AUTHORIZATION-0010` | 404 NOT_FOUND | 역할을 찾을 수 없어요. 역할 정보를 확인해주세요. | AuthorizationErrorCode | `CHALLENGER_ROLE_NOT_FOUND` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:30` |
| 44 | authorization | `AUTHORIZATION-0011` | 501 NOT_IMPLEMENTED | 아직 지원하지 않는 권한 확인이에요. 관리자에게 문의해주세요. | AuthorizationErrorCode | `PERMISSION_TYPE_NOT_IMPLEMENTED` | `src/main/java/com/umc/product/authorization/domain/exception/AuthorizationErrorCode.java:31` |

## challenger

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 45 | challenger | `CHALLENGER-0001` | 404 NOT_FOUND | 챌린저를 찾을 수 없어요. 선택한 챌린저를 확인해주세요. | ChallengerErrorCode | `CHALLENGER_NOT_FOUND` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:14` |
| 46 | challenger | `CHALLENGER-0002` | 409 CONFLICT | 이미 등록된 챌린저예요. 기존 기록을 확인해주세요. | ChallengerErrorCode | `CHALLENGER_ALREADY_EXISTS` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:15` |
| 47 | challenger | `CHALLENGER-0003` | 400 BAD_REQUEST | 이미 탈퇴한 챌린저예요. 다른 챌린저를 선택해주세요. | ChallengerErrorCode | `CHALLENGER_ALREADY_WITHDRAWN` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:16` |
| 48 | challenger | `CHALLENGER-0004` | 400 BAD_REQUEST | 챌린저 상태가 올바르지 않아요. 상태를 확인해주세요. | ChallengerErrorCode | `INVALID_CHALLENGER_STATUS` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:17` |
| 49 | challenger | `CHALLENGER-0005` | 400 BAD_REQUEST | 활동 중인 챌린저만 사용할 수 있어요. 챌린저 상태를 확인해주세요. | ChallengerErrorCode | `CHALLENGER_NOT_ACTIVE` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:18` |
| 50 | challenger | `CHALLENGER-0007` | 404 NOT_FOUND | 상벌점 기록을 찾을 수 없어요. 선택한 기록을 확인해주세요. | ChallengerErrorCode | `CHALLENGER_POINT_NOT_FOUND` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:19` |
| 51 | challenger | `CHALLENGER-0008` | 404 NOT_FOUND | 챌린저 수정 요청이 올바르지 않아요. 입력값을 확인해주세요. | ChallengerErrorCode | `BAD_CHALLENGER_UPDATE_REQUEST` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:20` |
| 52 | challenger | `CHALLENGER-0009` | 400 BAD_REQUEST | 일정을 만들려면 챌린저 상태가 활동 중이거나 수료여야 해요. | ChallengerErrorCode | `NOT_ALLOWED_AUTHOR` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:21` |
| 53 | challenger | `CHALLENGER-0010` | 404 NOT_FOUND | 연결된 멤버 프로필을 찾을 수 없어요. 회원 정보를 확인해주세요. | ChallengerErrorCode | `MEMBER_PROFILE_NOT_FOUND` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:22` |
| 54 | challenger | `CHALLENGER-0011` | 400 BAD_REQUEST | 커서 값이 올바르지 않아요. 목록을 처음부터 다시 조회해주세요. | ChallengerErrorCode | `INVALID_CURSOR_ID` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:23` |
| 55 | challenger | `CHALLENGER-0012` | 400 BAD_REQUEST | 이미 사용한 챌린저 기록 추가 코드예요. 새 코드를 발급받아주세요. | ChallengerErrorCode | `USED_CHALLENGER_RECORD_CODE` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:24` |
| 56 | challenger | `CHALLENGER-0013` | 400 BAD_REQUEST | 코드에 등록된 이름이 내 정보와 일치하지 않아요. 입력한 코드를 확인해주세요. | ChallengerErrorCode | `INVALID_MEMBER_NAME_FOR_RECORD` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:25` |
| 57 | challenger | `CHALLENGER-0014` | 400 BAD_REQUEST | 코드에 등록된 학교가 내 소속과 일치하지 않아요. 소속 정보를 확인해주세요. | ChallengerErrorCode | `INVALID_SCHOOL_FOR_RECORD` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:26` |
| 58 | challenger | `CHALLENGER-0015` | 400 BAD_REQUEST | 입력한 정보로 챌린저 기록을 만들 수 없어요. 값을 확인해주세요. | ChallengerErrorCode | `INVALID_CHALLENGER_RECORD_CREATE_REQUEST` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:27` |
| 59 | challenger | `CHALLENGER-0016` | 404 NOT_FOUND | 해당 기수의 챌린저 기록을 찾을 수 없어요. 기수를 확인해주세요. | ChallengerErrorCode | `NO_CHALLENGER_IN_MEMBER_GISU` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:28` |
| 60 | challenger | `CHALLENGER-0017` | 404 NOT_FOUND | 챌린저 파트를 찾을 수 없어요. 파트 값을 확인해주세요. | ChallengerErrorCode | `CHALLENGER_PART_NOT_FOUND` | `src/main/java/com/umc/product/challenger/domain/exception/ChallengerErrorCode.java:29` |

## community

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 61 | community | `COMMUNITY-0001` | 404 NOT_FOUND | 게시글을 찾을 수 없어요. 목록을 새로고침해주세요. | CommunityErrorCode | `POST_NOT_FOUND` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:14` |
| 62 | community | `COMMUNITY-0002` | 404 NOT_FOUND | 댓글을 찾을 수 없어요. 목록을 새로고침해주세요. | CommunityErrorCode | `COMMENT_NOT_FOUND` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:15` |
| 63 | community | `COMMUNITY-0003` | 404 NOT_FOUND | 상장을 찾을 수 없어요. 선택한 상장을 확인해주세요. | CommunityErrorCode | `TROPHY_NOT_FOUND` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:16` |
| 64 | community | `COMMUNITY-0004` | 400 BAD_REQUEST | 게시글 제목이 올바르지 않아요. 제목을 확인해주세요. | CommunityErrorCode | `INVALID_POST_TITLE` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:18` |
| 65 | community | `COMMUNITY-0005` | 400 BAD_REQUEST | 게시글 내용이 올바르지 않아요. 내용을 확인해주세요. | CommunityErrorCode | `INVALID_POST_CONTENT` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:19` |
| 66 | community | `COMMUNITY-0006` | 400 BAD_REQUEST | 게시글 카테고리가 올바르지 않아요. 카테고리를 다시 선택해주세요. | CommunityErrorCode | `INVALID_POST_CATEGORY` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:20` |
| 67 | community | `COMMUNITY-0007` | 400 BAD_REQUEST | 게시글 지역이 올바르지 않아요. 지역을 다시 선택해주세요. | CommunityErrorCode | `INVALID_POST_REGION` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:21` |
| 68 | community | `COMMUNITY-0008` | 400 BAD_REQUEST | 번개글은 번개글 작성 화면에서 만들어주세요. | CommunityErrorCode | `CANNOT_CHANGE_TO_LIGHTNING` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:22` |
| 69 | community | `COMMUNITY-0009` | 400 BAD_REQUEST | 번개글은 일반 게시글로 바꿀 수 없어요. 새 게시글로 작성해주세요. | CommunityErrorCode | `CANNOT_CHANGE_FROM_LIGHTNING` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:23` |
| 70 | community | `COMMUNITY-0010` | 400 BAD_REQUEST | 댓글 내용이 올바르지 않아요. 내용을 확인해주세요. | CommunityErrorCode | `INVALID_COMMENT_CONTENT` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:25` |
| 71 | community | `COMMUNITY-0011` | 403 FORBIDDEN | 내가 작성한 댓글만 삭제할 수 있어요. | CommunityErrorCode | `COMMENT_NOT_OWNED` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:26` |
| 72 | community | `COMMUNITY-0012` | 400 BAD_REQUEST | 상장 주차가 올바르지 않아요. 1 이상의 숫자로 입력해주세요. | CommunityErrorCode | `INVALID_TROPHY_WEEK` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:28` |
| 73 | community | `COMMUNITY-0013` | 400 BAD_REQUEST | 상장 제목이 올바르지 않아요. 제목을 확인해주세요. | CommunityErrorCode | `INVALID_TROPHY_TITLE` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:29` |
| 74 | community | `COMMUNITY-0014` | 400 BAD_REQUEST | 상장 내용이 올바르지 않아요. 내용을 확인해주세요. | CommunityErrorCode | `INVALID_TROPHY_CONTENT` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:30` |
| 75 | community | `COMMUNITY-0015` | 400 BAD_REQUEST | 상장 링크가 올바르지 않아요. 링크를 확인해주세요. | CommunityErrorCode | `INVALID_TROPHY_URL` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:31` |
| 76 | community | `COMMUNITY-0016` | 409 CONFLICT | 이미 신고한 게시글 또는 댓글이에요. 신고 내역을 확인해주세요. | CommunityErrorCode | `REPORT_ALREADY_EXISTS` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:33` |
| 77 | community | `COMMUNITY-0017` | 400 BAD_REQUEST | 작성자 정보가 필요해요. 로그인 정보를 확인해주세요. | CommunityErrorCode | `INVALID_POST_AUTHOR` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:36` |
| 78 | community | `COMMUNITY-0018` | 400 BAD_REQUEST | 번개글이 아니에요. 일반 게시글 화면에서 수정해주세요. | CommunityErrorCode | `NOT_LIGHTNING_POST` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:37` |
| 79 | community | `COMMUNITY-0019` | 400 BAD_REQUEST | 번개글은 번개글 화면에서 작성해주세요. | CommunityErrorCode | `USE_LIGHTNING_API` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:38` |
| 80 | community | `COMMUNITY-0020` | 400 BAD_REQUEST | 번개글을 작성하려면 모임 정보를 입력해주세요. | CommunityErrorCode | `LIGHTNING_INFO_REQUIRED` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:39` |
| 81 | community | `COMMUNITY-0021` | 403 FORBIDDEN | 내가 작성한 게시글만 수정하거나 삭제할 수 있어요. | CommunityErrorCode | `POST_NOT_OWNED` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:40` |
| 82 | community | `COMMUNITY-0022` | 400 BAD_REQUEST | 모임 시간을 입력해주세요. | CommunityErrorCode | `INVALID_LIGHTNING_MEET_AT` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:43` |
| 83 | community | `COMMUNITY-0023` | 400 BAD_REQUEST | 모임 장소를 입력해주세요. | CommunityErrorCode | `INVALID_LIGHTNING_LOCATION` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:44` |
| 84 | community | `COMMUNITY-0024` | 400 BAD_REQUEST | 최대 참가자는 1명 이상으로 입력해주세요. | CommunityErrorCode | `INVALID_LIGHTNING_MAX_PARTICIPANTS` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:45` |
| 85 | community | `COMMUNITY-0025` | 400 BAD_REQUEST | 오픈 채팅 링크를 입력해주세요. | CommunityErrorCode | `INVALID_LIGHTNING_OPEN_CHAT_URL` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:46` |
| 86 | community | `COMMUNITY-0026` | 400 BAD_REQUEST | 오픈 채팅 링크는 http:// 또는 https://로 시작해야 해요. | CommunityErrorCode | `INVALID_LIGHTNING_OPEN_CHAT_URL_FORMAT` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:47` |
| 87 | community | `COMMUNITY-0027` | 400 BAD_REQUEST | 모임 시간은 현재 이후로 선택해주세요. | CommunityErrorCode | `INVALID_LIGHTNING_MEET_AT_PAST` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:48` |
| 88 | community | `COMMUNITY-0028` | 400 BAD_REQUEST | 댓글을 작성할 게시글을 선택해주세요. | CommunityErrorCode | `INVALID_COMMENT_POST_ID` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:51` |
| 89 | community | `COMMUNITY-0029` | 400 BAD_REQUEST | 댓글 작성자 챌린저 정보를 확인해주세요. | CommunityErrorCode | `INVALID_COMMENT_CHALLENGER_ID` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:52` |
| 90 | community | `COMMUNITY-0030` | 400 BAD_REQUEST | ID는 1 이상의 숫자로 입력해주세요. | CommunityErrorCode | `INVALID_ID` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:55` |
| 91 | community | `COMMUNITY-0031` | 400 BAD_REQUEST | 새 게시글을 만들려면 작성자 정보가 필요해요. 로그인 정보를 확인해주세요. | CommunityErrorCode | `POST_SAVE_REQUIRES_AUTHOR` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:58` |
| 92 | community | `COMMUNITY-0032` | 400 BAD_REQUEST | 게시글 수정 요청이 올바르지 않아요. 요청 방식을 확인해주세요. | CommunityErrorCode | `POST_UPDATE_INVALID_CALL` | `src/main/java/com/umc/product/community/domain/exception/CommunityErrorCode.java:59` |

## curriculum

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 93 | curriculum | `CURRICULUM-0001` | 404 NOT_FOUND | 커리큘럼을 찾을 수 없어요. 선택한 커리큘럼을 확인해주세요. | CurriculumErrorCode | `CURRICULUM_NOT_FOUND` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:14` |
| 94 | curriculum | `CURRICULUM-0002` | 404 NOT_FOUND | 워크북을 찾을 수 없어요. 선택한 워크북을 확인해주세요. | CurriculumErrorCode | `WORKBOOK_NOT_FOUND` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:15` |
| 95 | curriculum | `CURRICULUM-0003` | 404 NOT_FOUND | 미션을 찾을 수 없어요. 선택한 미션을 확인해주세요. | CurriculumErrorCode | `MISSION_NOT_FOUND` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:16` |
| 96 | curriculum | `CURRICULUM-0004` | 409 CONFLICT | 제출된 워크북이 있어 삭제할 수 없어요. 제출 내역을 먼저 확인해주세요. | CurriculumErrorCode | `WORKBOOK_HAS_SUBMISSIONS` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:17` |
| 97 | curriculum | `CURRICULUM-0005` | 404 NOT_FOUND | 이 커리큘럼에 포함된 워크북이 아니에요. 워크북을 다시 선택해주세요. | CurriculumErrorCode | `WORKBOOK_NOT_IN_CURRICULUM` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:18` |
| 98 | curriculum | `CURRICULUM-0006` | 404 NOT_FOUND | 챌린저 워크북을 찾을 수 없어요. 선택한 워크북을 확인해주세요. | CurriculumErrorCode | `CHALLENGER_WORKBOOK_NOT_FOUND` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:19` |
| 99 | curriculum | `CURRICULUM-0007` | 400 BAD_REQUEST | 제출 내용을 입력해주세요. | CurriculumErrorCode | `SUBMISSION_REQUIRED` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:20` |
| 100 | curriculum | `CURRICULUM-0008` | 400 BAD_REQUEST | 워크북 상태가 올바르지 않아요. 상태 값을 확인해주세요. | CurriculumErrorCode | `INVALID_WORKBOOK_STATUS` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:21` |
| 101 | curriculum | `CURRICULUM-0009` | 409 CONFLICT | 이미 해당 주차의 워크북 미션을 제출했어요. 제출 내역을 확인해주세요. | CurriculumErrorCode | `WORKBOOK_SUBMISSION_ALREADY_EXISTS` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:22` |
| 102 | curriculum | `CURRICULUM-0010` | 409 CONFLICT | 해당 기수와 파트의 커리큘럼이 이미 있어요. 기존 커리큘럼을 확인해주세요. | CurriculumErrorCode | `CURRICULUM_ALREADY_EXISTS` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:23` |
| 103 | curriculum | `CURRICULUM-0011` | 403 FORBIDDEN | 이 워크북에 접근할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | CurriculumErrorCode | `WORKBOOK_ACCESS_DENIED` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:24` |
| 104 | curriculum | `CURRICULUM-0012` | 400 BAD_REQUEST | 주차 커리큘럼 시작일은 종료일보다 빨라야 해요. 기간을 다시 선택해주세요. | CurriculumErrorCode | `INVALID_WEEKLY_CURRICULUM_PERIOD` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:26` |
| 105 | curriculum | `CURRICULUM-0013` | 400 BAD_REQUEST | 현재 상태에서는 워크북 상태를 변경할 수 없어요. 상태를 확인해주세요. | CurriculumErrorCode | `INVALID_WORKBOOK_STATUS_TRANSITION` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:27` |
| 106 | curriculum | `CURRICULUM-0014` | 404 NOT_FOUND | 주차별 커리큘럼을 찾을 수 없어요. 선택한 주차를 확인해주세요. | CurriculumErrorCode | `WEEKLY_CURRICULUM_NOT_FOUND` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:28` |
| 107 | curriculum | `CURRICULUM-0015` | 409 CONFLICT | 주차별 커리큘럼이 남아 있어 삭제할 수 없어요. 주차별 커리큘럼을 먼저 정리해주세요. | CurriculumErrorCode | `CURRICULUM_HAS_WEEKLY_CURRICULUMS` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:29` |
| 108 | curriculum | `CURRICULUM-0016` | 409 CONFLICT | 원본 워크북이 남아 있어 삭제할 수 없어요. 원본 워크북을 먼저 정리해주세요. | CurriculumErrorCode | `WEEKLY_CURRICULUM_HAS_WORKBOOKS` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:30` |
| 109 | curriculum | `CURRICULUM-0017` | 409 CONFLICT | 배포된 워크북이 있어 주차 기간을 수정할 수 없어요. 배포 상태를 확인해주세요. | CurriculumErrorCode | `WEEKLY_CURRICULUM_DATE_LOCKED` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:31` |
| 110 | curriculum | `CURRICULUM-0018` | 409 CONFLICT | 동일한 주차와 부록 여부의 주차별 커리큘럼이 이미 있어요. 기존 항목을 확인해주세요. | CurriculumErrorCode | `WEEKLY_CURRICULUM_ALREADY_EXISTS` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:32` |
| 111 | curriculum | `CURRICULUM-0019` | 400 BAD_REQUEST | 종료된 기간으로는 주차별 커리큘럼을 만들거나 수정할 수 없어요. 기간을 다시 선택해주세요. | CurriculumErrorCode | `WEEKLY_CURRICULUM_PERIOD_ALREADY_ENDED` | `src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java:33` |

## feedback

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 112 | feedback | `FEEDBACK-0001` | 404 NOT_FOUND | 피드백 양식을 찾을 수 없어요. 양식을 다시 선택해주세요. | FeedbackErrorCode | `USER_FEEDBACK_TEMPLATE_NOT_FOUND` | `src/main/java/com/umc/product/feedback/domain/exception/FeedbackErrorCode.java:15` |

## figma

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 113 | figma | `FIGMA-0001` | 404 NOT_FOUND | Figma 연결 정보를 찾을 수 없어요. Figma 파일을 다시 연결해주세요. | FigmaErrorCode | `INTEGRATION_NOT_FOUND` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:14` |
| 114 | figma | `FIGMA-0002` | 502 BAD_GATEWAY | Figma 연결에 실패했어요. 잠시 후 다시 시도해주세요. | FigmaErrorCode | `OAUTH_TOKEN_EXCHANGE_FAILED` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:15` |
| 115 | figma | `FIGMA-0003` | 502 BAD_GATEWAY | Figma 연결이 만료됐어요. 다시 연결해주세요. | FigmaErrorCode | `OAUTH_TOKEN_REFRESH_FAILED` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:16` |
| 116 | figma | `FIGMA-0004` | 502 BAD_GATEWAY | Figma 댓글을 불러오지 못했어요. 잠시 후 다시 시도해주세요. | FigmaErrorCode | `COMMENT_FETCH_FAILED` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:17` |
| 117 | figma | `FIGMA-0005` | 502 BAD_GATEWAY | Figma 파일 정보를 불러오지 못했어요. 파일 키를 확인한 뒤 다시 시도해주세요. | FigmaErrorCode | `FILE_METADATA_FETCH_FAILED` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:18` |
| 118 | figma | `FIGMA-0006` | 404 NOT_FOUND | 등록된 Figma 감시 파일이 아니에요. 감시 파일 목록을 확인해주세요. | FigmaErrorCode | `WATCHED_FILE_NOT_FOUND` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:19` |
| 119 | figma | `FIGMA-0007` | 409 CONFLICT | 이미 등록된 Figma 파일이에요. 기존 감시 파일을 확인해주세요. | FigmaErrorCode | `WATCHED_FILE_ALREADY_EXISTS` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:20` |
| 120 | figma | `FIGMA-0008` | 400 BAD_REQUEST | Figma 연결 요청이 올바르지 않아요. 연결을 처음부터 다시 시도해주세요. | FigmaErrorCode | `OAUTH_STATE_MISMATCH` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:21` |
| 121 | figma | `FIGMA-0009` | 500 INTERNAL_SERVER_ERROR | Figma 인증 정보를 저장하지 못했어요. 관리자에게 문의해주세요. | FigmaErrorCode | `TOKEN_ENCRYPTION_FAILED` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:22` |
| 122 | figma | `FIGMA-0010` | 502 BAD_GATEWAY | Discord 멘션을 보내지 못했어요. 잠시 후 다시 시도해주세요. | FigmaErrorCode | `DISCORD_MENTION_SEND_FAILED` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:23` |
| 123 | figma | `FIGMA-0013` | 404 NOT_FOUND | Figma 라우팅 도메인을 찾을 수 없어요. 등록된 도메인을 확인해주세요. | FigmaErrorCode | `ROUTING_DOMAIN_NOT_FOUND` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:24` |
| 124 | figma | `FIGMA-0014` | 409 CONFLICT | 이미 등록된 라우팅 도메인이에요. 기존 도메인을 확인해주세요. | FigmaErrorCode | `ROUTING_DOMAIN_ALREADY_EXISTS` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:25` |
| 125 | figma | `FIGMA-0015` | 404 NOT_FOUND | 이 라우팅 도메인의 멘션을 찾을 수 없어요. 멘션 설정을 확인해주세요. | FigmaErrorCode | `ROUTING_DOMAIN_MENTION_NOT_FOUND` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:26` |
| 126 | figma | `FIGMA-0016` | 412 PRECONDITION_FAILED | 등록된 라우팅 도메인이 없어요. 라우팅 도메인을 먼저 등록해주세요. | FigmaErrorCode | `ROUTING_DOMAIN_NOT_REGISTERED` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:27` |
| 127 | figma | `FIGMA-0017` | 400 BAD_REQUEST | 요약할 기간이 올바르지 않아요. 시작과 종료 시간을 확인해주세요. | FigmaErrorCode | `DIGEST_RANGE_INVALID` | `src/main/java/com/umc/product/figma/domain/exception/FigmaErrorCode.java:28` |

## global

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 128 | global | `COMMON-0001` | 500 INTERNAL_SERVER_ERROR | 요청을 처리하지 못했어요. 잠시 후 다시 시도해주세요. | CommonErrorCode | `INTERNAL_SERVER_ERROR` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:26` |
| 129 | global | `COMMON-400` | 400 BAD_REQUEST | 요청 값이 올바르지 않아요. 입력한 값을 확인해주세요. | CommonErrorCode | `BAD_REQUEST` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:28` |
| 130 | global | `COMMON-401` | 401 UNAUTHORIZED | 로그인이 필요해요. 로그인 후 다시 시도해주세요. | CommonErrorCode | `UNAUTHORIZED` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:29` |
| 131 | global | `COMMON-403` | 403 FORBIDDEN | 요청할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | CommonErrorCode | `FORBIDDEN` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:30` |
| 132 | global | `COMMON-404` | 404 NOT_FOUND | 요청한 항목을 찾을 수 없어요. 입력한 값을 확인해주세요. | CommonErrorCode | `NOT_FOUND` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:31` |
| 133 | global | `COMMON-501` | 501 NOT_IMPLEMENTED | 아직 사용할 수 없는 기능이에요. 필요한 기능이라면 서버팀에 문의해주세요. | CommonErrorCode | `NOT_IMPLEMENTED` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:32` |
| 134 | global | `ENV-0001` | 400 BAD_REQUEST | 현재 실행 환경에서는 사용할 수 없는 기능이에요. 환경 설정을 확인해주세요. | CommonErrorCode | `INVALID_ENV` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:39` |
| 135 | global | `PE-0001` | 501 NOT_IMPLEMENTED | 아직 지원하지 않는 권한 확인이에요. 관리자에게 문의해주세요. | CommonErrorCode | `PERMISSION_TYPE_NOT_IMPLEMENTED` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:42` |
| 136 | global | `SECURITY-0001` | 401 UNAUTHORIZED | 인증 정보가 없어요. 로그인 후 다시 시도해주세요. | CommonErrorCode | `SECURITY_NOT_GIVEN` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:35` |
| 137 | global | `SECURITY-0002` | 403 FORBIDDEN | 권한이 부족해요. 필요한 권한이 있다면 운영진에게 문의해주세요. | CommonErrorCode | `SECURITY_FORBIDDEN` | `src/main/java/com/umc/product/global/exception/constant/CommonErrorCode.java:36` |

## llm

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 138 | llm | `LLM-0001` | 502 BAD_GATEWAY | AI 응답을 생성하지 못했어요. 잠시 후 다시 시도해주세요. | LlmErrorCode | `CHAT_COMPLETION_FAILED` | `src/main/java/com/umc/product/llm/domain/exception/LlmErrorCode.java:14` |
| 139 | llm | `LLM-0002` | 502 BAD_GATEWAY | AI 응답을 읽지 못했어요. 잠시 후 다시 시도해주세요. | LlmErrorCode | `CHAT_COMPLETION_INVALID_RESPONSE` | `src/main/java/com/umc/product/llm/domain/exception/LlmErrorCode.java:15` |
| 140 | llm | `LLM-0003` | 500 INTERNAL_SERVER_ERROR | AI 제공자 설정이 누락됐어요. 관리자에게 문의해주세요. | LlmErrorCode | `PROVIDER_NOT_CONFIGURED` | `src/main/java/com/umc/product/llm/domain/exception/LlmErrorCode.java:16` |

## maintenance

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 141 | maintenance | `MAINTENANCE-0001` | 503 SERVICE_UNAVAILABLE | 서비스 점검 중이에요. 점검이 끝난 뒤 다시 시도해주세요. | MaintenanceErrorCode | `SERVICE_UNDER_MAINTENANCE` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:14` |
| 142 | maintenance | `MAINTENANCE-0002` | 404 NOT_FOUND | 점검 일정을 찾을 수 없어요. 선택한 일정을 확인해주세요. | MaintenanceErrorCode | `MAINTENANCE_WINDOW_NOT_FOUND` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:15` |
| 143 | maintenance | `MAINTENANCE-0003` | 400 BAD_REQUEST | 종료 시각은 시작 시각 이후로 선택해주세요. | MaintenanceErrorCode | `INVALID_TIME_RANGE` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:16` |
| 144 | maintenance | `MAINTENANCE-0004` | 400 BAD_REQUEST | 시작 시각은 현재 시각 이후로 선택해주세요. | MaintenanceErrorCode | `START_AT_IN_PAST` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:17` |
| 145 | maintenance | `MAINTENANCE-0005` | 400 BAD_REQUEST | 도메인별 점검은 대상 도메인을 1개 이상 선택해주세요. | MaintenanceErrorCode | `TARGET_DOMAINS_REQUIRED` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:18` |
| 146 | maintenance | `MAINTENANCE-0006` | 409 CONFLICT | 다른 점검 일정과 시간이 겹쳐요. 시간을 다시 선택해주세요. | MaintenanceErrorCode | `OVERLAPPING_WINDOW` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:19` |
| 147 | maintenance | `MAINTENANCE-0007` | 400 BAD_REQUEST | 이미 종료된 점검 일정이에요. 진행 중이거나 예정된 일정을 선택해주세요. | MaintenanceErrorCode | `ALREADY_ENDED` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:20` |
| 148 | maintenance | `MAINTENANCE-0008` | 403 FORBIDDEN | 점검을 관리할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | MaintenanceErrorCode | `NOT_SUPER_ADMIN` | `src/main/java/com/umc/product/maintenance/exception/MaintenanceErrorCode.java:21` |

## member

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 149 | member | `MEMBER-0001` | 404 NOT_FOUND | 사용자를 찾을 수 없어요. 선택한 사용자를 확인해주세요. | MemberErrorCode | `MEMBER_NOT_FOUND` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:14` |
| 150 | member | `MEMBER-0002` | 409 CONFLICT | 이미 등록된 사용자예요. 기존 계정을 확인해주세요. | MemberErrorCode | `MEMBER_ALREADY_EXISTS` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:15` |
| 151 | member | `MEMBER-0003` | 409 CONFLICT | 이미 사용 중인 이메일이에요. 다른 이메일을 입력해주세요. | MemberErrorCode | `EMAIL_ALREADY_EXISTS` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:16` |
| 152 | member | `MEMBER-0004` | 400 BAD_REQUEST | 이미 탈퇴한 사용자예요. 다른 계정으로 진행해주세요. | MemberErrorCode | `MEMBER_ALREADY_WITHDRAWN` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:17` |
| 153 | member | `MEMBER-0005` | 400 BAD_REQUEST | 사용자 상태가 올바르지 않아요. 상태를 확인해주세요. | MemberErrorCode | `INVALID_MEMBER_STATUS` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:18` |
| 154 | member | `MEMBER-0006` | 400 BAD_REQUEST | 활동 중인 사용자만 이용할 수 있어요. 계정 상태를 확인해주세요. | MemberErrorCode | `MEMBER_NOT_ACTIVE` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:19` |
| 155 | member | `MEMBER-0007` | 400 BAD_REQUEST | 이미 회원가입을 완료한 사용자예요. 로그인해주세요. | MemberErrorCode | `MEMBER_ALREADY_REGISTERED` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:20` |
| 156 | member | `MEMBER-0008` | 404 NOT_FOUND | 프로필을 찾을 수 없어요. 프로필 정보를 확인해주세요. | MemberErrorCode | `MEMBER_PROFILE_NOT_FOUND` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:21` |
| 157 | member | `MEMBER-0009` | 400 BAD_REQUEST | 학교가 등록되지 않은 사용자예요. 학교 정보를 먼저 등록해주세요. | MemberErrorCode | `MEMBER_SCHOOL_NOT_ASSIGNED` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:22` |
| 158 | member | `MEMBER-0010` | 409 CONFLICT | 이미 로그인 ID와 비밀번호가 등록되어 있어요. 기존 정보로 로그인해주세요. | MemberErrorCode | `CREDENTIAL_ALREADY_REGISTERED` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:23` |
| 159 | member | `MEMBER-0011` | 400 BAD_REQUEST | 로그인 ID와 비밀번호가 등록되어 있지 않아요. 먼저 등록해주세요. | MemberErrorCode | `CREDENTIAL_NOT_REGISTERED` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:24` |
| 160 | member | `MEMBER-0012` | 400 BAD_REQUEST | 로그인 ID가 올바르지 않아요. 다시 입력해주세요. | MemberErrorCode | `INVALID_LOGIN_ID` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:25` |
| 161 | member | `MEMBER-0013` | 400 BAD_REQUEST | 비밀번호가 올바르지 않아요. 다시 입력해주세요. | MemberErrorCode | `INVALID_PASSWORD` | `src/main/java/com/umc/product/member/domain/exception/MemberErrorCode.java:26` |

## notice

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 162 | notice | `NOTICE-0001` | 404 NOT_FOUND | 공지를 찾을 수 없어요. 목록을 새로고침해주세요. | NoticeErrorCode | `NOTICE_NOT_FOUND` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:14` |
| 163 | notice | `NOTICE-0002` | 400 BAD_REQUEST | 이미 게시된 공지예요. 게시 상태를 확인해주세요. | NoticeErrorCode | `ALREADY_PUBLISHED_NOTICE` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:15` |
| 164 | notice | `NOTICE-0003` | 400 BAD_REQUEST | 공지 제목이 올바르지 않아요. 제목을 확인해주세요. | NoticeErrorCode | `INVALID_NOTICE_TITLE` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:16` |
| 165 | notice | `NOTICE-0004` | 400 BAD_REQUEST | 공지 내용이 올바르지 않아요. 내용을 확인해주세요. | NoticeErrorCode | `INVALID_NOTICE_CONTENT` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:17` |
| 166 | notice | `NOTICE-0005` | 400 BAD_REQUEST | 현재 상태에서는 공지 알림을 보낼 수 없어요. 공지 상태를 확인해주세요. | NoticeErrorCode | `INVALID_NOTICE_STATUS_FOR_REMINDER` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:18` |
| 167 | notice | `NOTICE-0006` | 400 BAD_REQUEST | 공지 작성자 정보가 필요해요. 로그인 정보를 확인해주세요. | NoticeErrorCode | `AUTHOR_REQUIRED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:19` |
| 168 | notice | `NOTICE-0007` | 400 BAD_REQUEST | 공지 대상 범위를 선택해주세요. | NoticeErrorCode | `NOTICE_SCOPE_REQUIRED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:20` |
| 169 | notice | `NOTICE-0008` | 403 FORBIDDEN | 공지 작성자만 수정할 수 있어요. | NoticeErrorCode | `NOTICE_AUTHOR_MISMATCH` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:21` |
| 170 | notice | `NOTICE-0009` | 403 FORBIDDEN | 공지를 작성할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | NoticeErrorCode | `NO_WRITE_PERMISSION` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:22` |
| 171 | notice | `NOTICE-0010` | 400 BAD_REQUEST | 공지 수신자 설정이 올바르지 않아요. 대상 설정을 확인해주세요. | NoticeErrorCode | `INVALID_TARGET_SETTING` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:26` |
| 172 | notice | `NOTICE-0011` | 404 NOT_FOUND | 공지 수신 대상을 찾을 수 없어요. 대상 설정을 다시 확인해주세요. | NoticeErrorCode | `NO_TARGET_FOUND` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:27` |
| 173 | notice | `NOTICE-0012` | 403 FORBIDDEN | 공지를 조회할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | NoticeErrorCode | `NO_READ_PERMISSION` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:24` |
| 174 | notice | `NOTICE-9999` | 501 NOT_IMPLEMENTED | 아직 사용할 수 없는 기능이에요. 필요한 기능이라면 서버팀에 문의해주세요. | NoticeErrorCode | `NOT_IMPLEMENTED_YET` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:45` |
| 175 | notice | `NOTICE-CONTENTS-0001` | 400 BAD_REQUEST | 투표를 1개 이상 선택해주세요. | NoticeErrorCode | `VOTE_IDS_REQUIRED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:30` |
| 176 | notice | `NOTICE-CONTENTS-0002` | 400 BAD_REQUEST | 이미지 링크를 1개 이상 입력해주세요. | NoticeErrorCode | `IMAGE_URLS_REQUIRED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:31` |
| 177 | notice | `NOTICE-CONTENTS-0003` | 400 BAD_REQUEST | 공지 링크를 1개 이상 입력해주세요. | NoticeErrorCode | `LINK_URLS_REQUIRED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:32` |
| 178 | notice | `NOTICE-CONTENTS-0004` | 404 NOT_FOUND | 공지 투표를 찾을 수 없어요. 투표를 다시 선택해주세요. | NoticeErrorCode | `NOTICE_VOTE_NOT_FOUND` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:33` |
| 179 | notice | `NOTICE-CONTENTS-0005` | 404 NOT_FOUND | 공지 이미지를 찾을 수 없어요. 이미지를 다시 선택해주세요. | NoticeErrorCode | `NOTICE_IMAGE_NOT_FOUND` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:34` |
| 180 | notice | `NOTICE-CONTENTS-0006` | 404 NOT_FOUND | 공지 링크를 찾을 수 없어요. 링크를 다시 선택해주세요. | NoticeErrorCode | `NOTICE_LINK_NOT_FOUND` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:35` |
| 181 | notice | `NOTICE-CONTENTS-0007` | 400 BAD_REQUEST | 공지 이미지는 최대 10장까지 등록할 수 있어요. | NoticeErrorCode | `IMAGE_LIMIT_EXCEEDED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:36` |
| 182 | notice | `NOTICE-CONTENTS-0008` | 409 CONFLICT | 이 공지에는 이미 투표가 있어요. 기존 투표를 확인해주세요. | NoticeErrorCode | `VOTE_ALREADY_EXISTS` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:37` |
| 183 | notice | `NOTICE-CONTENTS-0009` | 400 BAD_REQUEST | 투표 선택지는 2개 이상 5개 이하로 입력해주세요. | NoticeErrorCode | `INVALID_VOTE_OPTION_COUNT` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:38` |
| 184 | notice | `NOTICE-CONTENTS-0010` | 400 BAD_REQUEST | 투표 선택지에 빈 값이 있어요. 선택지 내용을 확인해주세요. | NoticeErrorCode | `INVALID_VOTE_OPTION_CONTENT` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:39` |
| 185 | notice | `NOTICE-CONTENTS-0011` | 400 BAD_REQUEST | 아직 투표 기간이 시작되지 않았어요. 시작 후 다시 시도해주세요. | NoticeErrorCode | `VOTE_NOT_STARTED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:40` |
| 186 | notice | `NOTICE-CONTENTS-0012` | 400 BAD_REQUEST | 이미 종료된 투표예요. 투표 기간을 확인해주세요. | NoticeErrorCode | `VOTE_CLOSED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:41` |
| 187 | notice | `NOTICE-CONTENTS-0013` | 400 BAD_REQUEST | 투표 선택지를 1개 이상 선택해주세요. | NoticeErrorCode | `SELECTED_OPTION_IDS_REQUIRED` | `src/main/java/com/umc/product/notice/domain/exception/NoticeErrorCode.java:42` |

## notification

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 188 | notification | `EMAIL-0004` | 500 INTERNAL_SERVER_ERROR | 이메일 본문을 만들지 못했어요. 관리자에게 문의해주세요. | EmailErrorCode | `EMAIL_TEMPLATE_RENDER_FAILED` | `src/main/java/com/umc/product/notification/domain/exception/EmailErrorCode.java:13` |
| 189 | notification | `EMAIL-0005` | 500 INTERNAL_SERVER_ERROR | 이메일을 보내지 못했어요. 잠시 후 다시 시도해주세요. | EmailErrorCode | `EMAIL_SEND_FAILED` | `src/main/java/com/umc/product/notification/domain/exception/EmailErrorCode.java:14` |
| 190 | notification | `FCM-0001` | 404 NOT_FOUND | 푸시 알림 정보를 찾을 수 없어요. 알림 설정을 다시 확인해주세요. | FcmErrorCode | `FCM_NOT_FOUND` | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:14` |
| 191 | notification | `FCM-0002` | 404 NOT_FOUND | 사용자의 푸시 알림 정보를 찾을 수 없어요. 알림 설정을 확인해주세요. | FcmErrorCode | `USER_FCM_NOT_FOUND` | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:15` |
| 192 | notification | `FCM-0003` | 500 INTERNAL_SERVER_ERROR | 푸시 알림을 보내지 못했어요. 잠시 후 다시 시도해주세요. | FcmErrorCode | `FCM_SEND_FAILED` | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:16` |
| 193 | notification | `FCM-0004` | 500 INTERNAL_SERVER_ERROR | 푸시 알림 주제를 구독하지 못했어요. 잠시 후 다시 시도해주세요. | FcmErrorCode | `TOPIC_SUBSCRIBE_FAILED` | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:17` |
| 194 | notification | `FCM-0005` | 500 INTERNAL_SERVER_ERROR | 푸시 알림 주제 구독을 해제하지 못했어요. 잠시 후 다시 시도해주세요. | FcmErrorCode | `TOPIC_UNSUBSCRIBE_FAILED` | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:18` |
| 195 | notification | `FCM-0006` | 500 INTERNAL_SERVER_ERROR | 푸시 알림 주제 메시지를 보내지 못했어요. 잠시 후 다시 시도해주세요. | FcmErrorCode | `TOPIC_SEND_FAILED` | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:19` |
| 196 | notification | `FCM-0007` | 429 TOO_MANY_REQUESTS | 푸시 알림 요청이 너무 많아요. 잠시 후 다시 시도해주세요. | FcmErrorCode | `RATE_LIMITED` | `src/main/java/com/umc/product/notification/domain/exception/FcmErrorCode.java:20` |
| 197 | notification | `WEBHOOK-0001` | 500 INTERNAL_SERVER_ERROR | 웹훅 메시지를 보내지 못했어요. 잠시 후 다시 시도해주세요. | WebhookErrorCode | `WEBHOOK_SEND_FAILED` | `src/main/java/com/umc/product/notification/domain/exception/WebhookErrorCode.java:14` |
| 198 | notification | `WEBHOOK-0002` | 400 BAD_REQUEST | 해당 플랫폼의 웹훅 설정을 찾을 수 없어요. 플랫폼 설정을 확인해주세요. | WebhookErrorCode | `WEBHOOK_ADAPTER_NOT_FOUND` | `src/main/java/com/umc/product/notification/domain/exception/WebhookErrorCode.java:15` |

## organization

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 199 | organization | `ORGANIZATION-0001` | 400 BAD_REQUEST | 기수를 선택해주세요. | OrganizationErrorCode | `GISU_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:14` |
| 200 | organization | `ORGANIZATION-0002` | 400 BAD_REQUEST | 조직 이름을 입력해주세요. | OrganizationErrorCode | `ORGAN_NAME_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:15` |
| 201 | organization | `ORGANIZATION-0003` | 400 BAD_REQUEST | 학교를 선택해주세요. | OrganizationErrorCode | `SCHOOL_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:16` |
| 202 | organization | `ORGANIZATION-0004` | 400 BAD_REQUEST | 지부를 선택해주세요. | OrganizationErrorCode | `CHAPTER_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:17` |
| 203 | organization | `ORGANIZATION-0005` | 400 BAD_REQUEST | 기수 시작일을 선택해주세요. | OrganizationErrorCode | `GISU_START_AT_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:20` |
| 204 | organization | `ORGANIZATION-0006` | 400 BAD_REQUEST | 기수 종료일을 선택해주세요. | OrganizationErrorCode | `GISU_END_AT_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:21` |
| 205 | organization | `ORGANIZATION-0007` | 400 BAD_REQUEST | 기수 시작일은 종료일보다 빨라야 해요. 기간을 다시 선택해주세요. | OrganizationErrorCode | `GISU_PERIOD_INVALID` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:22` |
| 206 | organization | `ORGANIZATION-0008` | 400 BAD_REQUEST | 학교 이름을 입력해주세요. | OrganizationErrorCode | `SCHOOL_NAME_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:24` |
| 207 | organization | `ORGANIZATION-0009` | 400 BAD_REQUEST | 학교 이메일 도메인을 입력해주세요. | OrganizationErrorCode | `SCHOOL_DOMAIN_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:25` |
| 208 | organization | `ORGANIZATION-0010` | 400 BAD_REQUEST | 스터디 그룹 이름을 입력해주세요. | OrganizationErrorCode | `STUDY_GROUP_NAME_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:27` |
| 209 | organization | `ORGANIZATION-0011` | 400 BAD_REQUEST | 스터디 그룹 리더를 선택해주세요. | OrganizationErrorCode | `STUDY_GROUP_LEADER_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:28` |
| 210 | organization | `ORGANIZATION-0012` | 400 BAD_REQUEST | 스터디 그룹을 선택해주세요. | OrganizationErrorCode | `STUDY_GROUP_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:30` |
| 211 | organization | `ORGANIZATION-0013` | 400 BAD_REQUEST | 스터디 그룹 멤버는 1명 이상 선택해주세요. | OrganizationErrorCode | `STUDY_GROUP_MEMBER_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:31` |
| 212 | organization | `ORGANIZATION-0014` | 400 BAD_REQUEST | 스터디 그룹 멤버를 선택해주세요. | OrganizationErrorCode | `STUDY_GROUP_MEMBER_ID_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:32` |
| 213 | organization | `ORGANIZATION-0015` | 400 BAD_REQUEST | 이미 스터디 그룹에 포함된 멤버예요. 멤버 목록을 확인해주세요. | OrganizationErrorCode | `STUDY_GROUP_MEMBER_ALREADY_EXISTS` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:33` |
| 214 | organization | `ORGANIZATION-0016` | 404 NOT_FOUND | 스터디 그룹 멤버를 찾을 수 없어요. 멤버 목록을 확인해주세요. | OrganizationErrorCode | `STUDY_GROUP_MEMBER_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:34` |
| 215 | organization | `ORGANIZATION-0017` | 404 NOT_FOUND | 지부를 찾을 수 없어요. 선택한 지부를 확인해주세요. | OrganizationErrorCode | `CHAPTER_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:36` |
| 216 | organization | `ORGANIZATION-0018` | 404 NOT_FOUND | 학교를 찾을 수 없어요. 선택한 학교를 확인해주세요. | OrganizationErrorCode | `SCHOOL_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:37` |
| 217 | organization | `ORGANIZATION-0019` | 404 NOT_FOUND | 활성화된 기수를 찾을 수 없어요. 기수 설정을 확인해주세요. | OrganizationErrorCode | `GISU_IS_ACTIVE_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:38` |
| 218 | organization | `ORGANIZATION-0020` | 404 NOT_FOUND | 기수를 찾을 수 없어요. 선택한 기수를 확인해주세요. | OrganizationErrorCode | `GISU_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:39` |
| 219 | organization | `ORGANIZATION-0021` | 400 BAD_REQUEST | 파트를 선택해주세요. | OrganizationErrorCode | `PART_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:40` |
| 220 | organization | `ORGANIZATION-0022` | 400 BAD_REQUEST | 스터디 그룹 이름이 올바르지 않아요. 이름을 확인해주세요. | OrganizationErrorCode | `STUDY_GROUP_NAME_INVALID` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:41` |
| 221 | organization | `ORGANIZATION-0023` | 400 BAD_REQUEST | 스터디 그룹을 찾을 수 없어요. 선택한 그룹을 확인해주세요. | OrganizationErrorCode | `STUDY_GROUP_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:42` |
| 222 | organization | `ORGANIZATION-0024` | 400 BAD_REQUEST | 스터디 그룹 리더 또는 멤버에 존재하지 않는 챌린저가 있어요. 구성원을 확인해주세요. | OrganizationErrorCode | `STUDY_GROUP_CHALLENGER_INVALID` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:44` |
| 223 | organization | `ORGANIZATION-0025` | 400 BAD_REQUEST | 스터디 그룹 리더는 멤버로 중복 등록할 수 없어요. 구성원을 확인해주세요. | OrganizationErrorCode | `LEADER_CANNOT_BE_MEMBER` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:46` |
| 224 | organization | `ORGANIZATION-0026` | 400 BAD_REQUEST | 스터디 그룹 멤버가 중복됐어요. 멤버 목록을 확인해주세요. | OrganizationErrorCode | `STUDY_GROUP_MEMBER_DUPLICATED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:47` |
| 225 | organization | `ORGANIZATION-0027` | 404 NOT_FOUND | 학교와 지부 연결 정보를 찾을 수 없어요. 배정 정보를 확인해주세요. | OrganizationErrorCode | `NO_SUCH_CHAPTER_SCHOOL` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:48` |
| 226 | organization | `ORGANIZATION-0028` | 409 CONFLICT | 이미 존재하는 기수예요. 기존 기수를 확인해주세요. | OrganizationErrorCode | `GISU_ALREADY_EXISTS` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:49` |
| 227 | organization | `ORGANIZATION-0029` | 409 CONFLICT | 해당 기수에서 이미 다른 지부에 배정된 학교가 있어요. 학교 배정 정보를 확인해주세요. | OrganizationErrorCode | `SCHOOL_ALREADY_ASSIGNED_TO_CHAPTER` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:50` |
| 228 | organization | `ORGANIZATION-0030` | 409 CONFLICT | 해당 기수에 같은 이름의 지부가 이미 있어요. 다른 이름을 입력해주세요. | OrganizationErrorCode | `CHAPTER_NAME_DUPLICATED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:52` |
| 229 | organization | `ORGANIZATION-0031` | 403 FORBIDDEN | 스터디 그룹을 조회할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | OrganizationErrorCode | `STUDY_GROUP_ACCESS_DENIED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:54` |
| 230 | organization | `ORGANIZATION-0032` | 409 CONFLICT | 연결된 지부 또는 학교가 있어 기수를 삭제할 수 없어요. 연결 정보를 먼저 정리해주세요. | OrganizationErrorCode | `GISU_HAS_ASSOCIATED_CHAPTERS` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:56` |
| 231 | organization | `ORGANIZATION-0033` | 400 BAD_REQUEST | 스터디 그룹 파트장은 1명 이상 선택해주세요. | OrganizationErrorCode | `STUDY_GROUP_MENTOR_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:58` |
| 232 | organization | `ORGANIZATION-0034` | 400 BAD_REQUEST | 스터디 그룹 파트장을 선택해주세요. | OrganizationErrorCode | `STUDY_GROUP_MENTOR_ID_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:59` |
| 233 | organization | `ORGANIZATION-0035` | 409 CONFLICT | 다른 스터디 그룹에 이미 속한 멤버가 있어요. 멤버 목록을 확인해주세요. | OrganizationErrorCode | `STUDY_GROUP_MEMBER_ALREADY_IN_PART_STUDY` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:61` |
| 234 | organization | `ORGANIZATION-0036` | 400 BAD_REQUEST | 이미 해당 스터디에 속한 파트장이에요. 파트장 목록을 확인해주세요. | OrganizationErrorCode | `STUDY_GROUP_MENTOR_DUPLICATED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:62` |
| 235 | organization | `ORGANIZATION-0037` | 404 NOT_FOUND | 스터디 그룹 파트장 정보를 찾을 수 없어요. 파트장 목록을 확인해주세요. | OrganizationErrorCode | `STUDY_GROUP_MENTOR_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:63` |
| 236 | organization | `ORGANIZATION-0038` | 400 BAD_REQUEST | 스터디 그룹 일정에는 출석 정책이 필요해요. 출석 정책을 설정해주세요. | OrganizationErrorCode | `STUDY_GROUP_SCHEDULE_ATTENDANCE_POLICY_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:65` |
| 237 | organization | `ORGANIZATION-0039` | 400 BAD_REQUEST | UMC Product 기수는 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_GENERATION_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:68` |
| 238 | organization | `ORGANIZATION-0040` | 404 NOT_FOUND | UMC Product 기수를 찾을 수 없습니다. | OrganizationErrorCode | `UMC_PRODUCT_GENERATION_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:69` |
| 239 | organization | `ORGANIZATION-0041` | 409 CONFLICT | 이미 존재하는 UMC Product 기수입니다. | OrganizationErrorCode | `UMC_PRODUCT_GENERATION_ALREADY_EXISTS` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:70` |
| 240 | organization | `ORGANIZATION-0042` | 400 BAD_REQUEST | UMC Product 기수 시작일은 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_GENERATION_START_AT_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:71` |
| 241 | organization | `ORGANIZATION-0043` | 400 BAD_REQUEST | UMC Product 기수 종료일은 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_GENERATION_END_AT_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:73` |
| 242 | organization | `ORGANIZATION-0044` | 400 BAD_REQUEST | UMC Product 기수 시작일은 종료일보다 이전이어야 합니다. | OrganizationErrorCode | `UMC_PRODUCT_GENERATION_PERIOD_INVALID` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:75` |
| 243 | organization | `ORGANIZATION-0045` | 400 BAD_REQUEST | UMC Product 인원은 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_MEMBER_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:77` |
| 244 | organization | `ORGANIZATION-0046` | 404 NOT_FOUND | UMC Product 인원을 찾을 수 없습니다. | OrganizationErrorCode | `UMC_PRODUCT_MEMBER_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:78` |
| 245 | organization | `ORGANIZATION-0047` | 409 CONFLICT | 이미 등록된 UMC Product 인원입니다. | OrganizationErrorCode | `UMC_PRODUCT_MEMBER_ALREADY_EXISTS` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:79` |
| 246 | organization | `ORGANIZATION-0048` | 400 BAD_REQUEST | 회원 ID는 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_MEMBER_ID_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:80` |
| 247 | organization | `ORGANIZATION-0049` | 400 BAD_REQUEST | UMC Product 기능 조직 활동 기록은 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_FUNCTIONAL_MEMBERSHIP_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:81` |
| 248 | organization | `ORGANIZATION-0050` | 400 BAD_REQUEST | UMC Product 기능 조직은 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_FUNCTIONAL_UNIT_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:83` |
| 249 | organization | `ORGANIZATION-0051` | 400 BAD_REQUEST | UMC Product 직책은 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_ROLE_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:85` |
| 250 | organization | `ORGANIZATION-0052` | 400 BAD_REQUEST | UMC Product 포지션은 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_POSITION_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:86` |
| 251 | organization | `ORGANIZATION-0053` | 403 FORBIDDEN | UMC Product 관리 권한이 없습니다. | OrganizationErrorCode | `UMC_PRODUCT_ACCESS_DENIED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:87` |
| 252 | organization | `ORGANIZATION-0054` | 404 NOT_FOUND | UMC Product 기능 조직을 찾을 수 없습니다. | OrganizationErrorCode | `UMC_PRODUCT_FUNCTIONAL_UNIT_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:88` |
| 253 | organization | `ORGANIZATION-0055` | 400 BAD_REQUEST | UMC Product 기능 조직 유형은 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_FUNCTIONAL_UNIT_TYPE_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:90` |
| 254 | organization | `ORGANIZATION-0056` | 400 BAD_REQUEST | UMC Product 기능 조직 코드는 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_FUNCTIONAL_UNIT_CODE_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:92` |
| 255 | organization | `ORGANIZATION-0057` | 400 BAD_REQUEST | UMC Product 기능 조직 이름은 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_FUNCTIONAL_UNIT_NAME_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:94` |
| 256 | organization | `ORGANIZATION-0058` | 400 BAD_REQUEST | UMC Product Squad는 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_SQUAD_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:96` |
| 257 | organization | `ORGANIZATION-0059` | 404 NOT_FOUND | UMC Product Squad를 찾을 수 없습니다. | OrganizationErrorCode | `UMC_PRODUCT_SQUAD_NOT_FOUND` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:97` |
| 258 | organization | `ORGANIZATION-0060` | 400 BAD_REQUEST | UMC Product Squad 코드는 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_SQUAD_CODE_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:98` |
| 259 | organization | `ORGANIZATION-0061` | 400 BAD_REQUEST | UMC Product Squad 이름은 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_SQUAD_NAME_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:99` |
| 260 | organization | `ORGANIZATION-0062` | 400 BAD_REQUEST | UMC Product Squad 시작일은 종료일보다 이전이어야 합니다. | OrganizationErrorCode | `UMC_PRODUCT_SQUAD_PERIOD_INVALID` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:100` |
| 261 | organization | `ORGANIZATION-0063` | 400 BAD_REQUEST | UMC Product Squad 참여자는 필수입니다. | OrganizationErrorCode | `UMC_PRODUCT_SQUAD_PARTICIPANT_REQUIRED` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:102` |
| 262 | organization | `ORGANIZATION-0064` | 400 BAD_REQUEST | UMC Product 기능 조직은 자기 자신을 상위 조직으로 지정할 수 없습니다. | OrganizationErrorCode | `UMC_PRODUCT_FUNCTIONAL_UNIT_PARENT_INVALID` | `src/main/java/com/umc/product/organization/exception/OrganizationErrorCode.java:104` |

## project

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 263 | project | `PROJECT-0001` | 404 NOT_FOUND | 프로젝트를 찾을 수 없어요. 선택한 프로젝트를 확인해주세요. | ProjectErrorCode | `PROJECT_NOT_FOUND` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:15` |
| 264 | project | `PROJECT-0002` | 400 BAD_REQUEST | 이미 완료된 프로젝트예요. 프로젝트 상태를 확인해주세요. | ProjectErrorCode | `ALREADY_COMPLETED_PROJECT` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:16` |
| 265 | project | `PROJECT-0003` | 400 BAD_REQUEST | 이 프로젝트는 중단할 수 없어요. 프로젝트 상태를 확인해주세요. | ProjectErrorCode | `PROJECT_ABORT_UNAVAILABLE` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:17` |
| 266 | project | `PROJECT-0004` | 400 BAD_REQUEST | 제출된 지원서에서만 할 수 있는 작업이에요. 지원서 상태를 확인해주세요. | ProjectErrorCode | `APPLICATION_NOT_SUBMITTED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:20` |
| 267 | project | `PROJECT-0005` | 400 BAD_REQUEST | 이미 제출했거나 평가가 끝난 지원서예요. 지원서 상태를 확인해주세요. | ProjectErrorCode | `APPLICATION_SUBMIT_NOT_AVAILABLE` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:21` |
| 268 | project | `PROJECT-0006` | 404 NOT_FOUND | 프로젝트 지원 폼을 찾을 수 없어요. 선택한 프로젝트를 확인해주세요. | ProjectErrorCode | `APPLICATION_FORM_NOT_FOUND` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:29` |
| 269 | project | `PROJECT-0007` | 403 FORBIDDEN | 이 지원 폼 섹션에 접근할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | ProjectErrorCode | `APPLICATION_FORM_ACCESS_NOT_ALLOWED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:30` |
| 270 | project | `PROJECT-0008` | 409 CONFLICT | 작성 중인 프로젝트가 있어 새로 시작할 수 없어요. 기존 초안을 먼저 확인해주세요. | ProjectErrorCode | `PROJECT_DRAFT_ALREADY_IN_PROGRESS` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:40` |
| 271 | project | `PROJECT-0009` | 400 BAD_REQUEST | 현재 상태에서는 할 수 없는 작업이에요. 프로젝트 상태를 확인해주세요. | ProjectErrorCode | `PROJECT_INVALID_STATE` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:41` |
| 272 | project | `PROJECT-0010` | 400 BAD_REQUEST | 프로젝트 PO는 PLAN 파트 챌린저만 맡을 수 있어요. PO 정보를 확인해주세요. | ProjectErrorCode | `PROJECT_OWNER_NOT_PLAN_CHALLENGER` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:42` |
| 273 | project | `PROJECT-0011` | 400 BAD_REQUEST | 제출에 필요한 정보가 부족해요. 필수 항목을 확인해주세요. | ProjectErrorCode | `PROJECT_SUBMIT_VALIDATION_FAILED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:43` |
| 274 | project | `PROJECT-0012` | 403 FORBIDDEN | 이 프로젝트에 접근할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | ProjectErrorCode | `PROJECT_ACCESS_DENIED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:44` |
| 275 | project | `PROJECT-0013` | 400 BAD_REQUEST | 파트 섹션에는 파트를 1개 이상 선택해주세요. | ProjectErrorCode | `APPLICATION_FORM_POLICY_PARTS_EMPTY` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:32` |
| 276 | project | `PROJECT-0014` | 400 BAD_REQUEST | 현재 폼에 없는 섹션이에요. 섹션을 다시 선택해주세요. | ProjectErrorCode | `APPLICATION_FORM_INVALID_SECTION_ID` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:33` |
| 277 | project | `PROJECT-0015` | 400 BAD_REQUEST | 해당 섹션에 없는 질문이에요. 질문을 다시 선택해주세요. | ProjectErrorCode | `APPLICATION_FORM_INVALID_QUESTION_ID` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:34` |
| 278 | project | `PROJECT-0016` | 400 BAD_REQUEST | 해당 질문에 없는 선택지예요. 선택지를 다시 선택해주세요. | ProjectErrorCode | `APPLICATION_FORM_INVALID_OPTION_ID` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:35` |
| 279 | project | `PROJECT-0017` | 400 BAD_REQUEST | 선택형 질문에만 선택지를 추가할 수 있어요. 질문 유형을 확인해주세요. | ProjectErrorCode | `APPLICATION_FORM_OPTIONS_NOT_ALLOWED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:36` |
| 280 | project | `PROJECT-0018` | 400 BAD_REQUEST | 선택형 질문에는 선택지가 1개 이상 필요해요. 선택지를 추가해주세요. | ProjectErrorCode | `APPLICATION_FORM_OPTIONS_REQUIRED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:37` |
| 281 | project | `PROJECT-0019` | 500 INTERNAL_SERVER_ERROR | 임시저장 지원서를 운영진 응답으로 보여줄 수 없어요. 관리자에게 문의해주세요. | ProjectErrorCode | `APPLICATION_DRAFT_NOT_EXPOSABLE` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:22` |
| 282 | project | `PROJECT-0020` | 400 BAD_REQUEST | 운영진 지원자 목록에서는 임시저장 상태를 필터로 사용할 수 없어요. 다른 상태를 선택해주세요. | ProjectErrorCode | `APPLICATION_DRAFT_FILTER_NOT_ALLOWED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:24` |
| 283 | project | `PROJECT-0021` | 404 NOT_FOUND | 지원서를 찾을 수 없어요. 선택한 지원서를 확인해주세요. | ProjectErrorCode | `PROJECT_APPLICATION_NOT_FOUND` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:26` |
| 284 | project | `PROJECT-0022` | 409 CONFLICT | 프로젝트는 DRAFT 또는 PENDING_REVIEW 상태에서만 삭제할 수 있어요. 상태를 확인해주세요. | ProjectErrorCode | `PROJECT_DELETE_NOT_ALLOWED_IN_STATUS` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:46` |
| 285 | project | `PROJECT-0023` | 400 BAD_REQUEST | 프로젝트 중단 사유를 입력해주세요. | ProjectErrorCode | `PROJECT_ABORT_REASON_REQUIRED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:48` |
| 286 | project | `PROJECT-0100` | 404 NOT_FOUND | 프로젝트 멤버를 찾을 수 없어요. 멤버 목록을 확인해주세요. | ProjectErrorCode | `PROJECT_MEMBER_NOT_FOUND` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:51` |
| 287 | project | `PROJECT-0101` | 409 CONFLICT | 이미 이 프로젝트의 멤버예요. 멤버 목록을 확인해주세요. | ProjectErrorCode | `PROJECT_MEMBER_ALREADY_EXISTS` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:52` |
| 288 | project | `PROJECT-0102` | 400 BAD_REQUEST | 메인 PM은 팀원 제거가 아니라 소유권 양도로 변경해주세요. | ProjectErrorCode | `PROJECT_MAIN_PM_REMOVAL_REQUIRES_TRANSFER` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:53` |
| 289 | project | `PROJECT-0200` | 400 BAD_REQUEST | 파트 정원은 1명 이상으로 입력해주세요. | ProjectErrorCode | `PROJECT_PART_QUOTA_INVALID` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:56` |
| 290 | project | `PROJECT-0202` | 400 BAD_REQUEST | 프로젝트를 공개하려면 파트별 정원을 1개 이상 등록해주세요. | ProjectErrorCode | `PROJECT_PART_QUOTA_REQUIRED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:57` |
| 291 | project | `PROJECT-0203` | 400 BAD_REQUEST | 동일한 파트가 중복됐어요. 파트별 정원을 확인해주세요. | ProjectErrorCode | `PROJECT_PART_QUOTA_DUPLICATE` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:58` |
| 292 | project | `PROJECT-0204` | 404 NOT_FOUND | 작성 중인 지원서를 찾을 수 없어요. 지원서 목록을 확인해주세요. | ProjectErrorCode | `PROJECT_DRAFT_APPLICATION_NOT_FOUND` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:82` |
| 293 | project | `PROJECT-0205` | 403 FORBIDDEN | 이 프로젝트에 지원할 수 있는 파트가 아니에요. 지원 가능한 파트를 확인해주세요. | ProjectErrorCode | `PROJECT_APPLICATION_PART_NOT_ALLOWED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:83` |
| 294 | project | `PROJECT-0206` | 409 CONFLICT | 이미 해당 기수에 소속된 팀이 있어 지원할 수 없어요. 팀 정보를 확인해주세요. | ProjectErrorCode | `PROJECT_APPLICATION_MEMBER_ALREADY_IN_TEAM` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:84` |
| 295 | project | `PROJECT-0207` | 409 CONFLICT | 동일한 매칭 차수에 이미 제출한 지원서가 있어요. 기존 지원서를 확인해주세요. | ProjectErrorCode | `PROJECT_APPLICATION_DUPLICATE_SUBMISSION` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:85` |
| 296 | project | `PROJECT-0208` | 400 BAD_REQUEST | 현재는 해당 매칭 차수의 지원 기간이 아니에요. 지원 기간을 확인해주세요. | ProjectErrorCode | `PROJECT_APPLICATION_ROUND_NOT_OPEN` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:86` |
| 297 | project | `PROJECT-0209` | 400 BAD_REQUEST | 선택한 매칭 차수가 내 파트와 맞지 않아요. 매칭 차수를 다시 선택해주세요. | ProjectErrorCode | `PROJECT_APPLICATION_ROUND_TYPE_MISMATCH` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:87` |
| 298 | project | `PROJECT-0210` | 409 CONFLICT | 이미 작성 중인 지원서가 있어요. 기존 지원서를 이어서 작성해주세요. | ProjectErrorCode | `PROJECT_APPLICATION_ALREADY_EXISTS` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:88` |
| 299 | project | `PROJECT-0211` | 403 FORBIDDEN | 내가 운영하는 프로젝트에는 지원할 수 없어요. 다른 프로젝트를 선택해주세요. | ProjectErrorCode | `PROJECT_APPLICATION_SELF_APPLY_NOT_ALLOWED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:89` |
| 300 | project | `PROJECT-0212` | 400 BAD_REQUEST | 현재 상태에서는 합격 여부를 변경할 수 없어요. 지원서 상태를 확인해주세요. | ProjectErrorCode | `PROJECT_APPLICATION_DECISION_INVALID_TRANSITION` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:90` |
| 301 | project | `PROJECT-0213` | 409 CONFLICT | 해당 파트의 남은 자리를 초과해 합격 처리할 수 없어요. 파트 정원을 확인해주세요. | ProjectErrorCode | `PROJECT_APPLICATION_QUOTA_EXCEEDED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:92` |
| 302 | project | `PROJECT-0214` | 400 BAD_REQUEST | 이미 종결된 지원서는 철회할 수 없어요. 지원서 상태를 확인해주세요. | ProjectErrorCode | `PROJECT_APPLICATION_CANCEL_NOT_ALLOWED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:94` |
| 303 | project | `PROJECT-0215` | 400 BAD_REQUEST | 매칭 차수가 종료되어 지원서를 철회할 수 없어요. 차수 기간을 확인해주세요. | ProjectErrorCode | `PROJECT_APPLICATION_CANCEL_ROUND_CLOSED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:95` |
| 304 | project | `PROJECT-0300` | 404 NOT_FOUND | 매칭 차수를 찾을 수 없어요. 선택한 차수를 확인해주세요. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_NOT_FOUND` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:61` |
| 305 | project | `PROJECT-0301` | 400 BAD_REQUEST | 매칭 차수 기간은 시작, 종료, 결정 마감 순서여야 해요. 시간을 다시 선택해주세요. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_INVALID_PERIOD` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:62` |
| 306 | project | `PROJECT-0302` | 409 CONFLICT | 같은 지부의 다른 매칭 차수와 기간이 겹쳐요. 기간을 다시 선택해주세요. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_PERIOD_OVERLAPPED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:64` |
| 307 | project | `PROJECT-0303` | 403 FORBIDDEN | 이 매칭 차수를 관리할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_ACCESS_DENIED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:66` |
| 308 | project | `PROJECT-0304` | 409 CONFLICT | 연결된 지원서가 있는 매칭 차수는 삭제할 수 없어요. 지원서를 먼저 확인해주세요. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_DELETE_CONFLICT` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:68` |
| 309 | project | `PROJECT-0305` | 400 BAD_REQUEST | 시간 기준으로 조회하려면 지부를 함께 선택해주세요. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_TIME_REQUIRES_CHAPTER` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:70` |
| 310 | project | `PROJECT-0306` | 400 BAD_REQUEST | 매칭 차수가 종료되어 결정을 변경할 수 없어요. 차수 기간을 확인해주세요. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_LOCKED` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:72` |
| 311 | project | `PROJECT-0307` | 400 BAD_REQUEST | 결정 마감 시각이 지난 뒤 자동 선발을 실행할 수 있어요. 마감 시각을 확인해주세요. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_NOT_FINALIZABLE` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:74` |
| 312 | project | `PROJECT-0308` | 500 INTERNAL_SERVER_ERROR | 이 매칭 종류의 자동 선발 정책을 찾지 못했어요. 관리자에게 문의해주세요. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_POLICY_NOT_FOUND` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:76` |
| 313 | project | `PROJECT-0309` | 409 CONFLICT | 매칭 차수는 FIRST, SECOND, THIRD 순서로 배치하고 이전 차수 결정 마감 이후 1분 이상 간격을 둬야 해요. | ProjectErrorCode | `PROJECT_MATCHING_ROUND_PHASE_SEQUENCE_INVALID` | `src/main/java/com/umc/product/project/domain/exception/ProjectErrorCode.java:78` |

## schedule

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 314 | schedule | `SCHEDULE-0006` | 400 BAD_REQUEST | 시작 시간은 종료 시간보다 빨라야 해요. 시간을 다시 선택해주세요. | ScheduleErrorCode | `INVALID_TIME_RANGE` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:14` |
| 315 | schedule | `SCHEDULE-0009` | 404 NOT_FOUND | 일정을 찾을 수 없어요. 선택한 일정을 확인해주세요. | ScheduleErrorCode | `SCHEDULE_NOT_FOUND` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:16` |
| 316 | schedule | `SCHEDULE-0010` | 400 BAD_REQUEST | 태그를 1개 이상 선택해주세요. | ScheduleErrorCode | `TAG_REQUIRED` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:18` |
| 317 | schedule | `SCHEDULE-0011` | 400 BAD_REQUEST | 이미 출석 요청이 있어요. 기존 요청을 확인해주세요. | ScheduleErrorCode | `NOT_FIRST_ATTENDANCE_REQUEST` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:20` |
| 318 | schedule | `SCHEDULE-0012` | 404 NOT_FOUND | 출석 요청이 없어요. 출석 요청을 먼저 생성해주세요. | ScheduleErrorCode | `NO_ATTENDANCE_RECORD` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:22` |
| 319 | schedule | `SCHEDULE-0013` | 400 BAD_REQUEST | 첫 요청, 결석 또는 지각 상태에서만 출석 사유를 제출할 수 있어요. 출석 상태를 확인해주세요. | ScheduleErrorCode | `INVALID_ATTENDANCE_STATUS_FOR_EXCUSE` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:24` |
| 320 | schedule | `SCHEDULE-0014` | 400 BAD_REQUEST | 현재 출석 상태에서는 승인할 수 없어요. 출석 상태를 확인해주세요. | ScheduleErrorCode | `INVALID_ATTENDANCE_STATUS_FOR_APPROVAL` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:27` |
| 321 | schedule | `SCHEDULE-0015` | 400 BAD_REQUEST | 현재 출석 상태에서는 거절할 수 없어요. 출석 상태를 확인해주세요. | ScheduleErrorCode | `INVALID_ATTENDANCE_STATUS_FOR_REJECT` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:29` |
| 322 | schedule | `SCHEDULE-0016` | 400 BAD_REQUEST | 출석 인정을 요청하려면 사유를 입력해주세요. | ScheduleErrorCode | `NO_EXCUSE_REASON_GIVEN` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:31` |
| 323 | schedule | `SCHEDULE-0017` | 400 BAD_REQUEST | 운영진 확인이 필요한 출석 요청이 아니에요. 출석 상태를 확인해주세요. | ScheduleErrorCode | `ATTENDANCE_NOT_REQUIRES_CONFIRM` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:33` |
| 324 | schedule | `SCHEDULE-0018` | 400 BAD_REQUEST | 종료된 일정에는 출석을 요청할 수 없어요. 일정 시간을 확인해주세요. | ScheduleErrorCode | `SCHEDULE_ENDED` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:36` |
| 325 | schedule | `SCHEDULE-0019` | 400 BAD_REQUEST | 아직 출석할 수 있는 시간이 아니에요. 출석 가능 시간 이후에 다시 시도해주세요. | ScheduleErrorCode | `CHECK_IN_TOO_EARLY` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:38` |
| 326 | schedule | `SCHEDULE-0020` | 400 BAD_REQUEST | 대면 일정에는 위치 정보가 필요해요. 위치를 입력해주세요. | ScheduleErrorCode | `OFFLINE_SCHEDULE_REQUIRES_LOCATION` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:40` |
| 327 | schedule | `SCHEDULE-0021` | 400 BAD_REQUEST | 출석 정책이 없는 일정이에요. 출석 정책을 먼저 설정해주세요. | ScheduleErrorCode | `SCHEDULE_ATTENDANCE_POLICY_NOT_EXIST` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:42` |
| 328 | schedule | `SCHEDULE-0022` | 400 BAD_REQUEST | 일정 참석자 정보를 찾을 수 없어요. 참석자 목록을 확인해주세요. | ScheduleErrorCode | `PARTICIPANT_NOT_FOUND` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:44` |
| 329 | schedule | `SCHEDULE-0023` | 400 BAD_REQUEST | 출석 인증 범위 안에 있는지 확인하지 못했어요. 위치를 확인한 뒤 다시 시도해주세요. | ScheduleErrorCode | `LOCATION_NOT_VERIFIED` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:46` |
| 330 | schedule | `SCHEDULE-0024` | 400 BAD_REQUEST | 비대면 일정에는 위치 정보를 포함할 수 없어요. 위치 정보를 제거해주세요. | ScheduleErrorCode | `ONLINE_SCHEDULE_SHOULD_NOT_HAVE_LOCATION` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:48` |
| 331 | schedule | `SCHEDULE-0025` | 400 BAD_REQUEST | 현재 기수의 일정만 만들 수 있어요. 기수를 확인해주세요. | ScheduleErrorCode | `NOT_ACTIVE_GISU_SCHEDULE` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:51` |
| 332 | schedule | `SCHEDULE-0026` | 400 BAD_REQUEST | 일정 참여자만 출석할 수 있어요. 참여자 목록을 확인해주세요. | ScheduleErrorCode | `NOT_SCHEDULE_PARTICIPANT` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:54` |
| 333 | schedule | `SCHEDULE-0027` | 400 BAD_REQUEST | 출석이 필요한 일정에는 출석 정책을 설정해주세요. | ScheduleErrorCode | `ATTENDANCE_POLICY_REQUIRED` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:56` |
| 334 | schedule | `SCHEDULE-0028` | 400 BAD_REQUEST | 이미 시작된 일정은 수정할 수 없어요. 일정 시간을 확인해주세요. | ScheduleErrorCode | `STARTED_SCHEDULE_CANT_BE_EDITED` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:58` |
| 335 | schedule | `SCHEDULE-0029` | 403 FORBIDDEN | 일정을 만들려면 챌린저 활동 이력이 필요해요. 활동 기록을 확인해주세요. | ScheduleErrorCode | `CANNOT_CREATE_SCHEDULE` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:60` |
| 336 | schedule | `SCHEDULE-0030` | 400 BAD_REQUEST | 초대 가능한 참여자 수를 초과했어요. 참여자를 줄여주세요. | ScheduleErrorCode | `EXCEEDED_MAX_PARTICIPANTS` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:62` |
| 337 | schedule | `SCHEDULE-0031` | 403 FORBIDDEN | 출석이 필요한 일정을 만들 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | ScheduleErrorCode | `CANNOT_CREATE_ATTENDANCE_REQUIRED_SCHEDULE` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:64` |
| 338 | schedule | `SCHEDULE-0032` | 400 BAD_REQUEST | 초대할 수 없는 참여자가 포함되어 있어요. 참여자 목록을 확인해주세요. | ScheduleErrorCode | `INVALID_MEMBER_INVITE` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:67` |
| 339 | schedule | `SCHEDULE-0033` | 400 BAD_REQUEST | 출석 기록이 있는 일정은 삭제할 수 없어요. 출석 기록을 먼저 확인해주세요. | ScheduleErrorCode | `SCHEDULE_HAS_ATTENDANCE_RECORD` | `src/main/java/com/umc/product/schedule/domain/exception/ScheduleErrorCode.java:69` |

## storage

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 340 | storage | `STORAGE-0001` | 404 NOT_FOUND | 파일을 찾을 수 없어요. 선택한 파일을 확인해주세요. | StorageErrorCode | `FILE_NOT_FOUND` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:15` |
| 341 | storage | `STORAGE-0002` | 400 BAD_REQUEST | 파일 업로드가 아직 끝나지 않았어요. 업로드를 완료한 뒤 다시 시도해주세요. | StorageErrorCode | `FILE_UPLOAD_NOT_COMPLETED` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:16` |
| 342 | storage | `STORAGE-0003` | 400 BAD_REQUEST | 이미 업로드가 끝난 파일이에요. 파일 정보를 확인해주세요. | StorageErrorCode | `FILE_ALREADY_UPLOADED` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:17` |
| 343 | storage | `STORAGE-0004` | 400 BAD_REQUEST | 지원하지 않는 파일 형식이에요. 다른 파일을 선택해주세요. | StorageErrorCode | `INVALID_FILE_EXTENSION` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:22` |
| 344 | storage | `STORAGE-0005` | 400 BAD_REQUEST | 파일 크기가 너무 커요. 더 작은 파일을 선택해주세요. | StorageErrorCode | `FILE_SIZE_EXCEEDED` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:23` |
| 345 | storage | `STORAGE-0006` | 400 BAD_REQUEST | 파일 형식 정보가 올바르지 않아요. 파일을 다시 선택해주세요. | StorageErrorCode | `INVALID_CONTENT_TYPE` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:24` |
| 346 | storage | `STORAGE-0007` | 500 INTERNAL_SERVER_ERROR | 파일을 업로드하지 못했어요. 잠시 후 다시 시도해주세요. | StorageErrorCode | `STORAGE_UPLOAD_FAILED` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:27` |
| 347 | storage | `STORAGE-0008` | 500 INTERNAL_SERVER_ERROR | 파일을 삭제하지 못했어요. 잠시 후 다시 시도해주세요. | StorageErrorCode | `STORAGE_DELETE_FAILED` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:28` |
| 348 | storage | `STORAGE-0009` | 500 INTERNAL_SERVER_ERROR | 파일 접근 링크를 만들지 못했어요. 잠시 후 다시 시도해주세요. | StorageErrorCode | `STORAGE_URL_GENERATION_FAILED` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:29` |
| 349 | storage | `STORAGE-0010` | 500 INTERNAL_SERVER_ERROR | CDN 접근 링크를 만들지 못했어요. 관리자에게 문의해주세요. | StorageErrorCode | `CDN_SIGNING_FAILED` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:32` |
| 350 | storage | `STORAGE-0011` | 500 INTERNAL_SERVER_ERROR | CDN 설정이 누락됐어요. 관리자에게 문의해주세요. | StorageErrorCode | `NO_ENV_KEYS` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:33` |
| 351 | storage | `STORAGE-0012` | 500 INTERNAL_SERVER_ERROR | 서버 실행 환경이 올바르지 않아요. 관리자에게 문의해주세요. | StorageErrorCode | `INVALID_SPRING_PROFILE` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:34` |
| 352 | storage | `STORAGE-0013` | 403 FORBIDDEN | 파일을 삭제할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | StorageErrorCode | `FILE_DELETE_FORBIDDEN` | `src/main/java/com/umc/product/storage/domain/exception/StorageErrorCode.java:18` |

## survey

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 353 | survey | `SURVEY-0001` | 404 NOT_FOUND | 폼을 찾을 수 없어요. 선택한 폼을 확인해주세요. | SurveyErrorCode | `SURVEY_NOT_FOUND` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:14` |
| 354 | survey | `SURVEY-0002` | 409 CONFLICT | 임시저장 상태의 폼만 편집할 수 있어요. 폼 상태를 확인해주세요. | SurveyErrorCode | `SURVEY_NOT_DRAFT` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:15` |
| 355 | survey | `SURVEY-0003` | 404 NOT_FOUND | 질문을 찾을 수 없어요. 선택한 질문을 확인해주세요. | SurveyErrorCode | `QUESTION_NOT_FOUND` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:16` |
| 356 | survey | `SURVEY-0006` | 404 NOT_FOUND | 폼 응답을 찾을 수 없어요. 응답 목록을 확인해주세요. | SurveyErrorCode | `FORM_RESPONSE_NOT_FOUND` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:18` |
| 357 | survey | `SURVEY-0007` | 400 BAD_REQUEST | 이 폼에 포함된 질문이 아니에요. 질문을 다시 선택해주세요. | SurveyErrorCode | `QUESTION_IS_NOT_OWNED_BY_FORM` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:19` |
| 358 | survey | `SURVEY-0008` | 403 FORBIDDEN | 이 폼 응답에 접근할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | SurveyErrorCode | `FORM_RESPONSE_FORBIDDEN` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:20` |
| 359 | survey | `SURVEY-0009` | 400 BAD_REQUEST | 질문 유형이 맞지 않아요. 질문 유형을 확인해주세요. | SurveyErrorCode | `QUESTION_TYPE_MISMATCH` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:22` |
| 360 | survey | `SURVEY-0010` | 400 BAD_REQUEST | 필수 질문에 답변해주세요. | SurveyErrorCode | `REQUIRED_QUESTION_NOT_ANSWERED` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:23` |
| 361 | survey | `SURVEY-0011` | 400 BAD_REQUEST | 응답 형식이 올바르지 않아요. 답변을 확인해주세요. | SurveyErrorCode | `INVALID_ANSWER_FORMAT` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:24` |
| 362 | survey | `SURVEY-0012` | 400 BAD_REQUEST | '기타' 선택지가 중복됐어요. 선택지를 확인해주세요. | SurveyErrorCode | `OTHER_OPTION_DUPLICATED` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:25` |
| 363 | survey | `SURVEY-0013` | 400 BAD_REQUEST | 해당 질문에 없는 선택지예요. 선택지를 다시 선택해주세요. | SurveyErrorCode | `OPTION_NOT_IN_QUESTION` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:26` |
| 364 | survey | `SURVEY-0014` | 400 BAD_REQUEST | '기타' 선택지의 내용을 입력해주세요. | SurveyErrorCode | `OPTION_TEXT_REQUIRED` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:27` |
| 365 | survey | `SURVEY-0015` | 400 BAD_REQUEST | 폼 응답 가능 기간이 올바르지 않아요. 기간을 다시 선택해주세요. | SurveyErrorCode | `INVALID_FORM_ACTIVE_PERIOD` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:28` |
| 366 | survey | `SURVEY-0023` | 400 BAD_REQUEST | 투표 선택이 올바르지 않아요. 선택지를 확인해주세요. | SurveyErrorCode | `INVALID_VOTE_SELECTION` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:30` |
| 367 | survey | `SURVEY-0025` | 400 BAD_REQUEST | 투표 질문 형식이 올바르지 않아요. 투표 구성을 확인해주세요. | SurveyErrorCode | `INVALID_VOTE_FORM_STRUCTURE` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:32` |
| 368 | survey | `SURVEY-0027` | 400 BAD_REQUEST | 이미 제출한 응답이 있어요. 제출 내역을 확인해주세요. | SurveyErrorCode | `FORM_RESPONSE_ALREADY_EXISTS` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:34` |
| 369 | survey | `SURVEY-0028` | 409 CONFLICT | 발행된 폼에만 응답할 수 있어요. 폼 상태를 확인해주세요. | SurveyErrorCode | `SURVEY_NOT_PUBLISHED` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:35` |
| 370 | survey | `SURVEY-0029` | 404 NOT_FOUND | 선택지를 찾을 수 없어요. 선택지를 다시 확인해주세요. | SurveyErrorCode | `QUESTION_OPTION_NOT_FOUND` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:36` |
| 371 | survey | `SURVEY-0030` | 404 NOT_FOUND | 답변을 찾을 수 없어요. 응답 내용을 확인해주세요. | SurveyErrorCode | `ANSWER_NOT_FOUND` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:37` |
| 372 | survey | `SURVEY-0031` | 409 CONFLICT | 임시저장 상태의 응답에서만 할 수 있는 작업이에요. 응답 상태를 확인해주세요. | SurveyErrorCode | `FORM_RESPONSE_NOT_DRAFT` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:38` |
| 373 | survey | `SURVEY-0032` | 400 BAD_REQUEST | 이미 해당 질문에 대한 답변이 있어요. 기존 답변을 수정해주세요. | SurveyErrorCode | `ANSWER_ALREADY_EXISTS` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:39` |
| 374 | survey | `SURVEY-005` | 400 BAD_REQUEST | 이미 발행된 폼이에요. 폼 상태를 확인해주세요. | SurveyErrorCode | `SURVEY_ALREADY_PUBLISHED` | `src/main/java/com/umc/product/survey/domain/exception/SurveyErrorCode.java:17` |

## term

| 순번 | 도메인 | Code | HTTP Status | Message | Enum | Constant | Source |
|---:|---|---|---|---|---|---|---|
| 375 | term | `TERMS-0001` | 404 NOT_FOUND | 약관을 찾을 수 없어요. 선택한 약관을 확인해주세요. | TermErrorCode | `TERMS_NOT_FOUND` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:14` |
| 376 | term | `TERMS-0002` | 400 BAD_REQUEST | 약관 타입을 선택해주세요. | TermErrorCode | `TERMS_TYPE_REQUIRED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:15` |
| 377 | term | `TERMS-0003` | 400 BAD_REQUEST | 약관 제목을 입력해주세요. | TermErrorCode | `TERMS_TITLE_REQUIRED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:16` |
| 378 | term | `TERMS-0004` | 400 BAD_REQUEST | 약관 내용을 입력해주세요. | TermErrorCode | `TERMS_CONTENT_REQUIRED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:17` |
| 379 | term | `TERMS-0005` | 400 BAD_REQUEST | 약관 버전을 입력해주세요. | TermErrorCode | `TERMS_VERSION_REQUIRED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:18` |
| 380 | term | `TERMS-0006` | 404 NOT_FOUND | 약관 동의 정보를 찾을 수 없어요. 동의 내역을 확인해주세요. | TermErrorCode | `TERMS_CONSENT_NOT_FOUND` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:20` |
| 381 | term | `TERMS-0007` | 400 BAD_REQUEST | 이미 동의한 약관이에요. 동의 내역을 확인해주세요. | TermErrorCode | `TERMS_CONSENT_ALREADY_EXISTS` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:21` |
| 382 | term | `TERMS-0008` | 400 BAD_REQUEST | 회원을 선택해주세요. | TermErrorCode | `MEMBER_ID_REQUIRED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:22` |
| 383 | term | `TERMS-0009` | 400 BAD_REQUEST | 약관을 선택해주세요. | TermErrorCode | `TERM_ID_REQUIRED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:23` |
| 384 | term | `TERMS-0010` | 400 BAD_REQUEST | 필수 약관에 모두 동의해주세요. | TermErrorCode | `MANDATORY_TERMS_NOT_AGREED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:24` |
| 385 | term | `TERMS-0011` | 403 FORBIDDEN | 약관을 관리할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요. | TermErrorCode | `TERM_PERMISSION_DENIED` | `src/main/java/com/umc/product/term/domain/exception/TermErrorCode.java:26` |

