# Member Domain

## 역할

`member` 도메인은 회원 계정, 프로필, 학교 소속, 이메일 기반 자격증명, 회원 전역 system role과 회원 검색을 관리한다.

## 책임

- 회원가입과 회원 상태를 관리한다.
- 지원자 이메일로 과거 탈부 기록을 조회하기 위한 식별 정보를 보존한다.
- 프로필 이미지, 링크, 닉네임 등 공개 프로필을 관리한다.
- 로그인 ID와 비밀번호 자격증명 등록 여부를 관리한다.
- `SUPER_ADMIN` 같은 회원 전역 system role을 저장하고 공개 Query UseCase로 제공한다.
- 회원 검색과 요약 조회를 제공한다. 회원 검색은 챌린저 기록이 하나라도 있는 회원만 사용할 수 있다.
- 회원 프로필로 구성한 명함과 연락처·링크의 항목별 공개 여부를 관리한다.

## 본인 프로필과 프로덕트 권한

- `GET /api/v1/member/me`와 `GET /api/v2/member/me`는 `isUmcProduct`로 프로덕트 문의 답변·관리 가능 여부를 반환한다.
- 권한 도메인의 `CheckChallengerAuthorityUseCase.isUmcProduct(...)` 판정을 사용한다. `UMC_PRODUCT` 또는 `SUPER_ADMIN`이면 `true`이고, 기수 소속이나 챌린저 이력은 필요하지 않다.
- v1은 같은 본인 프로필 응답을 쓰는 수정 API에도 이 값을 포함한다. 타인 프로필 조회에는 이 필드를 포함하지 않는다.
- 앱 진입은 클라이언트에서 기존 챌린저 진입 조건 또는 `isUmcProduct == true`로 처리한다. 로그인·자동 로그인·회원가입 직후에 같은 조건을 적용해야 하며, 서버 필드 추가만으로 배포된 앱의 이동 조건이 바뀌지는 않는다.
- 클라이언트에 이 필드를 추가할 때 누락된 값은 `false`로 처리한다. 구버전 서버 응답도 읽을 수 있도록 iOS는 `decodeIfPresent`와 기본값, Android는 nullable 값의 기본값 처리를 사용한다.
- 이 필드는 프로덕트팀 소속 명단을 뜻하지 않는다. `SUPER_ADMIN`도 포함하며, 다른 기능의 권한이나 챌린저 이력을 추가하지 않는다.

## 명함

명함은 별도로 저장하지 않는 회원 프로필의 projection이다. 프로필 사진·연락처·링크·공개 여부를 수정하면 바로 명함에 반영된다.

| 화면 | API | 내려주는 정보 |
|------|-----|---------------|
| 마이페이지 명함 | `GET /api/v1/member/me/business-card` (MEMBER-104) | 이름·닉네임·학교·사진, 최신 기수·파트, 공개한 연락처, 공개했고 http(s) 형식인 링크 |
| 명함 수정 화면 조회 | `GET /api/v1/member/me/business-card/edit` (MEMBER-105) | 사진·이름·학교, 최신 기수·파트, 연동 소셜, 연락처와 5개 링크(공개 여부 포함) |
| 명함 수정 | `PATCH /api/v1/member/me/business-card` (MEMBER-006) | 요청의 `contact`, `links` 는 수정 화면 조회 응답과 같은 모양. 응답은 수정 화면 조회와 같다 |

- 연락처(`contact.email`)는 로그인 이메일(`member.email`)과 별개인 선택 입력값이다. `member_profile.contact_email` 에 저장하며, 입력하지 않으면 명함에 연락처를 표시하지 않는다. 로그인 이메일로 대신 채우지 않는다.
- 공개 여부의 초기값은 모두 공개다. 저장 위치는 `member_profile` 의 `*_public` 컬럼이다.
- 기수·파트는 챌린저 활동 이력 중 가장 최신 기수의 기록을 쓴다. 현재 활동 중이 아니어도 표시하며, 활동 이력이 없으면 비워 둔다.
- 수정 화면에서 바꿀 수 있는 항목은 사진·연락처·링크뿐이다. 명함 수정 요청은 링크 전체를 교체하고, `isPublic` 은 연락처와 각 링크에 필수다.
- 기존 API(`GET /api/v1/member/me`, `GET /api/v2/member/me`, `PATCH /api/v1/member/profile/links`)는 홈·로그인 세션·웹·macOS·배포된 앱이 쓰고 있어 응답과 요청 형태를 바꾸지 않았다. 기존 링크 수정 API로 저장해도 공개 여부와 연락처는 유지된다.
- 다른 회원 조회(`GET /api/v1/member/profile/{memberId}`, MEMBER-101)는 비공개로 설정한 링크를 null로 내려준다. 응답 형태는 그대로이며 로그인 이메일은 기존과 같이 항상 제외한다.

## 경계

회원은 사용자 정체성과 회원 전역 system role을 소유하지만 권한 정책을 직접 평가하지 않는다. 권한 평가는 `authorization`, 챌린저 활동 기록은 `challenger`, 조직 배정은 `organization`, 약관 동의는 `term` 도메인의 책임이다. 다른 도메인 정보는 ID와 공개 UseCase로 연결한다.

명함의 활동 이력·연동 소셜처럼 다른 도메인 데이터를 읽을 때는 member 소유의 outbound port(`LoadBusinessCardActivityPort`, `LoadMemberOAuthProviderPort`)를 두고, 다른 도메인 UseCase 호출은 `adapter/out/external/{도메인}` 어댑터에서만 한다. 다른 도메인 계약이 바뀌면 어댑터만 수정한다.

## UX Writing Notes

회원 오류는 사용자가 직접 해결하는 경우가 많다. `로그인해주세요`, `다른 이메일을 입력해주세요`, `학교 정보를 먼저 등록해주세요`처럼 행동을 분명히 쓴다.
