# Member Domain

## 역할

`member` 도메인은 회원 계정, 프로필, 학교 소속, 이메일 기반 자격증명, 회원 전역 system role과 회원 검색을 관리한다.

## 책임

- 회원가입과 회원 상태를 관리한다.
- 프로필 이미지, 링크, 닉네임 등 공개 프로필을 관리한다.
- 로그인 ID와 비밀번호 자격증명 등록 여부를 관리한다.
- `SUPER_ADMIN` 같은 회원 전역 system role을 저장하고 공개 Query UseCase로 제공한다.
- 회원 검색과 요약 조회를 제공한다. 회원 검색은 챌린저 기록이 하나라도 있는 회원만 사용할 수 있다.
- 회원 프로필로 구성한 명함과 연락처·링크의 항목별 공개 여부를 관리한다.

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
