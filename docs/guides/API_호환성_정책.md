# UMC PRODUCT API 호환성 정책

> 작성일: 2026-10-31

UMC PRODUCT 서버 하나를 iOS, Android, macOS 앱과 여러 웹 서비스가 함께 씁니다. 각 클라이언트는 배포일이 서로 다르고, 특히 모바일 앱은 스토어에 나간 버전을 바로 바꿀 수 없습니다. 이 문서는 서버 변경이 이미 배포된 클라이언트를 깨뜨리지 않도록 지켜야 할 규칙, 더 이상 쓰지 않는 API를 정리하는 절차, 그리고 서버 정기 릴리스 일정을 정합니다.

## 1. 적용 범위

### 대상 계약

서버와 클라이언트 사이에 오가는 다음 항목을 모두 "계약"으로 봅니다.

- REST API: 경로, HTTP 메서드, 요청·응답 필드, 쿼리 파라미터, 상태 코드, 에러 코드
- 권한: 누가 어떤 API를 호출할 수 있는지
- enum 값: 응답에 문자열로 나가는 상태·역할·파트 등의 값
- 실시간 메시지: STOMP destination, 이벤트 `type`, payload
- 푸시 알림: FCM `data` payload 키와 딥링크 형식
- 데이터 의미: 같은 응답이라도 서버가 값을 채우는 기준

### 대상 클라이언트

| 구분 | 클라이언트 | 특징 |
|---|---|---|
| 모바일 | iOS, Android | 스토어 심사와 사용자 업데이트를 거쳐야 하므로 구버전이 오래 남습니다. |
| 데스크톱 | macOS | 모바일과 같이 취급합니다. |
| 웹 | Operations, Brand 등 | 모바일·데스크톱과 같은 규칙을 적용합니다. |

## 2. 기본 원칙

계약을 바꿀 때는 다음 세 단계를 거칩니다.

1. **Expand**: 기존 계약은 그대로 두고 새 계약을 추가해 배포합니다.
2. **Migrate**: 각 클라이언트가 자기 일정에 맞춰 새 계약으로 옮깁니다.
3. **Contract**: 기존 계약을 쓰는 클라이언트가 없음을 확인한 뒤 지웁니다.

이 원칙에 따라 `develop` 브랜치는 언제 운영에 배포해도 기존 클라이언트를 깨뜨리지 않는 상태를 유지합니다. 배포일이 다른 기능을 브랜치로 나눠 모아 두지 않습니다.

서버는 새 API를 먼저 배포해 둘 수 있습니다. 새 API를 호출하는 클라이언트가 배포되기 전까지는 아무도 쓰지 않으므로, 기능의 공개 시점은 그 기능을 쓰는 클라이언트의 배포일입니다.

## 3. 변경 종류별 규칙

### API 경로와 메서드

**Rule #1: 배포된 API는 지우거나 경로·메서드를 바꾸지 않는다.**

바꿔야 하면 새 경로를 추가하고 기존 경로는 [지원 중단 절차](#5-지원-중단-절차)를 따릅니다.

### 응답 필드

**Rule #2: 응답 필드는 추가만 한다.**

- 필드 추가는 자유롭게 할 수 있습니다.
- 필드 삭제, 이름 변경, 타입 변경(예: 숫자 → 문자열, 객체 → 배열), 값이 있던 필드를 `null`로 바꾸는 것은 하지 않습니다.
- 모양이 크게 바뀌어야 하면 새 API를 추가합니다.

### 요청 필드

**Rule #3: 기존 API에 새 필수 요청 필드를 추가하지 않는다.**

- 새 필드는 선택값으로 추가하고, 값이 없을 때의 기본 동작을 기존과 같게 둡니다.
- 기존에 받던 값을 새 검증(`@NotBlank`, `@Size`, 허용 값 제한 등)으로 거부하는 것도 계약 변경입니다. 기존 클라이언트가 보내는 값을 먼저 확인합니다.

### enum 값

**Rule #4: 응답 enum 값은 이름을 바꾸거나 지우지 않는다. 값 추가는 대상 클라이언트에 미리 알린다.**

- 클라이언트가 모르는 값을 받으면 화면이 깨지거나 로그인이 막힐 수 있습니다. 새 값을 추가하기 전에 대상 클라이언트가 모르는 값을 안전하게 처리하는지 확인합니다.
- 구버전이 처리하지 못하면 구버전에는 기존 값으로 내려주는 방법을 먼저 검토합니다.

### 권한과 동작

**Rule #5: 기존 API의 권한을 좁히거나 동작을 바꾸지 않는다. 다른 권한·동작이 필요하면 새 API를 만든다.**

- 기존 호출자가 갑자기 403을 받거나, 같은 요청에 다른 결과를 받으면 계약 변경입니다.
- 예: 운영진 전용 검색이 필요하면 기존 검색 API의 권한을 바꾸지 않고 운영진용 API를 따로 추가합니다.

### 데이터 의미

**Rule #6: 저장된 데이터의 의미가 바뀌면, 기존 클라이언트가 보던 화면이 유지되도록 데이터를 함께 옮긴다.**

- 예: 상태 값이 둘로 나뉘면 기존 데이터를 새 기준에 맞게 마이그레이션해서, 구버전에서 이전과 같은 목록이 보이게 합니다.

### DB 마이그레이션

**Rule #7: 마이그레이션은 추가만 한다. 컬럼·테이블 삭제와 이름 변경은 Contract 단계에서 한다.**

- 운영에 적용된 마이그레이션은 되돌리기 어렵습니다.
- 운영 배포 중에는 서버가 부팅하면서 마이그레이션을 실행합니다. 큰 테이블을 전체 갱신하는 마이그레이션은 운영 데이터 양을 먼저 확인합니다.

### 실시간 메시지, 푸시, 딥링크

**Rule #8: STOMP destination, 이벤트 `type`, FCM payload 키, 딥링크 형식에도 Rule #1~#4를 똑같이 적용한다.**

- 새 이벤트 타입이나 새 메시지 타입을 보낼 때는 구버전이 그 값을 무시하거나 안전하게 표시하는지 확인합니다.

## 4. 지원 기간

지원 중단(deprecated)을 표시한 계약은 기본적으로 **지원 중단을 표시한 릴리스로부터 28일 이상 지난 첫 정기 릴리스**에서 지웁니다. 정기 릴리스 날짜가 정해져 있으므로, 지원 중단을 표시할 때 삭제 예정 릴리스 날짜를 함께 적습니다.

- 예: 2026-11-01 릴리스에서 지원 중단을 표시하면 삭제 예정은 2026-11-29 릴리스입니다.
- 클라이언트가 옮길 시간이 더 필요하면 해당 클라이언트 담당자와 합의해 삭제 예정 릴리스를 늦출 수 있습니다.
- 웹, 모바일, 데스크톱 모두 같은 기준을 적용합니다.
- 삭제 예정 릴리스가 되어도 호출이 남아 있으면 지우지 않고, 해당 클라이언트 담당자와 새 삭제 예정 릴리스를 정합니다.

### 담당자

**지원 중단을 표시한 사람이 그 계약의 담당자입니다.** 담당자는 삭제할 때까지 공지, 호출 확인, 삭제 PR을 맡습니다. 팀 DRI에게 넘기지 않습니다.

- 호출 여부는 담당자가 직접 로그로 확인합니다. 요청 로그에는 매칭된 경로(`uriTemplate`)와 토큰의 클라이언트 종류(`clientType`: `IOS`, `ANDROID`, `MACOS`)가 함께 남습니다.

## 5. 지원 중단 절차

### 1단계: 새 계약 추가 (Expand)

대체할 API나 필드를 추가하고, 기존 계약과 함께 배포합니다.

### 2단계: 코드에 표시

지원 중단은 API뿐 아니라 백엔드 코드 전반에 표시합니다. `@Deprecated`의 `since`에는 지원 중단을 표시한 릴리스 날짜를 적고, 설명에는 다음 형식의 한 줄을 넣습니다. 릴리스 PR의 CI가 이 줄을 읽어 삭제 예정 항목을 알리고 담당자를 멘션합니다.

```text
지원 중단: {대체 대상}. 삭제 예정: {YYYY-MM-DD} 릴리스, 담당자: {GitHub 아이디}
```

`@Deprecated`를 붙이면 Swagger에 지원 중단으로 표시되고, 아직 이 코드를 호출하는 곳에서는 컴파일 경고가 나므로 남은 사용처를 찾을 수 있습니다.

| 대상 | 표시 방법 | 설명을 넣는 곳 |
|---|---|---|
| API (컨트롤러 메서드) | `@Deprecated(since, forRemoval = true)` + `@Operation(deprecated = true)` | `@Operation`의 `description` |
| 요청·응답 필드 (DTO) | `@Deprecated(since, forRemoval = true)` + `@Schema(deprecated = true)` | `@Schema`의 `description` |
| 클래스, 메서드, 필드, enum 상수 | `@Deprecated(since, forRemoval = true)` | Javadoc `@deprecated` |

예: API

```java
@Deprecated(since = "2026-11-01", forRemoval = true)
@Operation(
    summary = "챌린저 검색",
    deprecated = true,
    description = """
        지원 중단: `GET /api/v1/member/search`. 삭제 예정: 2026-11-29 릴리스, 담당자: github-id
        """
)
@GetMapping("search/global")
public ChallengerSearchResponse searchGlobal(...) { ... }
```

예: 요청·응답 필드

```java
public record ProjectSummaryResponse(
    Long projectId,

    @Deprecated(since = "2026-11-01", forRemoval = true)
    @Schema(
        deprecated = true,
        description = "지원 중단: `ownerMemberId`. 삭제 예정: 2026-11-29 릴리스, 담당자: github-id"
    )
    Long ownerId,

    Long ownerMemberId
) {
}
```

예: 메서드

```java
/**
 * @deprecated 지원 중단: {@link #findActiveMembers(Long)}. 삭제 예정: 2026-11-29 릴리스, 담당자: github-id
 */
@Deprecated(since = "2026-11-01", forRemoval = true)
public List<Member> findMembers(Long gisuId) { ... }
```

예: enum 상수

```java
public enum ExampleStatus {
    ACTIVE,

    /**
     * @deprecated 지원 중단: {@link #ACTIVE}. 삭제 예정: 2026-11-29 릴리스, 담당자: github-id
     */
    @Deprecated(since = "2026-11-01", forRemoval = true)
    LEGACY_ACTIVE
}
```

### 3단계: 목록에 추가

이 문서의 [지원 중단 목록](#8-지원-중단-목록)에 담당자와 함께 한 줄을 추가합니다. 2단계와 같은 PR에서 함께 수정합니다.

### 4단계: 영향받는 팀에 공지

영향받는 클라이언트 팀 채널에 다음 다섯 가지를 남깁니다. PR 본문에도 같은 내용을 적습니다.

1. 무엇이 바뀌나요?
2. 무엇으로 옮기나요?
3. 기존 계약은 언제 지워지나요? (삭제 예정 릴리스 날짜)
4. 누가 맡나요? (지원 중단 담당자, 클라이언트별 확인 담당자)
5. 관련 PR·이슈 링크

### 5단계: 삭제 전 확인

담당자는 **삭제 예정 릴리스 바로 전 정기 릴리스** 때 다음을 합니다. 이 시점에는 릴리스 PR의 [지원 중단 항목 점검](#릴리스-pr-자동화) 댓글이 담당자를 멘션합니다.

- 로그로 아직 호출이 남아 있는지 확인합니다.
- 영향받는 팀 채널에 다음 릴리스에서 지워진다는 것을 다시 알립니다.
- 호출이 남아 있으면 이때 해당 클라이언트 담당자와 새 삭제 예정 릴리스를 정하고, 코드와 목록의 날짜를 고칩니다.

### 6단계: 삭제 (Contract)

담당자가 [지원 기간](#4-지원-기간) 조건을 확인한 뒤 기존 계약을 지우는 PR을 올립니다. 같은 PR에서 지원 중단 목록의 상태를 `삭제됨`으로 바꿉니다.

## 6. 정기 릴리스

`develop`에서 `main`으로 가는 릴리스 PR이 합쳐지면 운영 서버에 바로 배포됩니다. 변경이 오래 쌓였다가 한 번에 나가지 않도록, 서버는 정해진 주기로 배포합니다.

### 배포 주기

| 기간 | 주기 |
|---|---|
| 2026년 11월 | 매주 일요일 저녁 |
| 2026년 12월부터 | 2주마다 일요일 저녁 |

- 금요일에 릴리스 PR을 올리고, 일요일 저녁에 합칩니다.
- 일요일까지 필수 리뷰가 모이지 않으면 그 회차는 건너뛰고 다음 정기 릴리스에 배포합니다.
- 합칠 변경이 없으면 그 회차는 건너뜁니다.
- 장애나 보안 문제를 고치는 긴급 수정은 주기와 관계없이 배포할 수 있습니다. 배포한 사람은 각 팀 DRI에게 바로 알립니다. 그 밖의 이유로 정기 릴리스 사이에 따로 배포하지 않습니다.

### 클라이언트 일정 맞추기

클라이언트는 서버 정기 릴리스 일정에 맞춰 배포 일정을 잡습니다.

- 클라이언트 운영 배포에 필요한 서버 변경은, 그 배포일 전 정기 릴리스에 들어가야 합니다. 그 릴리스의 금요일 릴리스 PR 전까지 `develop`에 합칩니다.
- 연동과 QA는 dev 서버에서 합니다. `develop`에 합치면 dev 서버에 바로 배포되므로, 운영 정기 릴리스를 기다리지 않아도 됩니다.
- 모바일·데스크톱 앱은 필요한 서버 변경이 운영에 배포된 뒤 스토어에 출시합니다.

### 릴리스 PR 규칙

- 릴리스 PR 제목은 배포 날짜로 짓습니다. 예: `[Release] 2026-11-08`
- 릴리스 PR은 [자동으로 만들어집니다](#릴리스-pr-자동화). 금요일에 draft를 풀고 리뷰를 요청합니다.
- **모든 팀의 Team DRI 리뷰는 필수입니다.**
  - 클라이언트를 담당하는 팀은 팀의 Backend 담당자가 이번 릴리스에 들어간 변경 중 팀 클라이언트(iOS, Android, Web, macOS)에 영향이 있는 것을 팀 내 클라이언트 담당자와 확인합니다.
  - 클라이언트가 없는 팀(Platform, Infrastructure, Design Platform 등)은 팀이 맡은 영역(공통 모듈, 배포·DB 마이그레이션, 디자인 시스템 등)에 영향이 있는 변경을 확인합니다.
  - 확인이 끝나면 Team DRI가 Approve합니다.
- 릴리스 PR 본문에는 포함된 PR 목록과 함께, 이번 릴리스에서 바뀌는 계약을 따로 적습니다. [지원 중단 목록](#8-지원-중단-목록)에 새로 추가되거나 삭제된 항목이 여기에 해당합니다. **다음 정기 릴리스에서 삭제 예정인 항목**도 담당자와 함께 적습니다.

### 릴리스 PR 자동화

| 워크플로 | 언제 | 하는 일 |
|---|---|---|
| `Release PR` (`.github/workflows/release-pr.yml`) | `develop`에 변경이 합쳐질 때 | 열린 릴리스 PR이 없으면 다음 정기 릴리스 날짜로 `[Release] YYYY-MM-DD` draft PR을 만듭니다. 열려 있으면 본문의 "포함된 PR" 목록만 갱신하고, 사람이 쓴 "바뀌는 계약"은 그대로 둡니다. |
| `Release PR Deprecation Report` (`.github/workflows/release-pr-deprecation-report.yml`) | `main`으로 가는 PR이 열리거나 바뀔 때 | 코드의 `삭제 예정: YYYY-MM-DD 릴리스, 담당자: GitHub 아이디` 표기를 읽어 PR 댓글 하나로 알립니다. 이번 릴리스에서 지울 항목, 다음 릴리스에서 지울 항목(담당자 멘션), 담당자가 없는 항목, 삭제 예정이 적히지 않은 `@Deprecated`를 보여 줍니다. CI를 실패시키지 않습니다. |

- 다음 릴리스 날짜는 직전 `[Release] YYYY-MM-DD` PR 날짜에 배포 주기를 더해 정합니다. 배포 주기는 저장소 변수 `RELEASE_INTERVAL_DAYS`로 정하며, 매주 배포하는 기간에는 `7`, 2주마다 배포하는 기간에는 `14`로 둡니다. 변수가 없으면 `14`를 씁니다.
- 자동으로 정한 날짜가 실제 배포일과 다르면 PR 제목을 직접 고칩니다. 점검 댓글은 제목의 날짜를 기준으로 다시 계산합니다.

## 7. 예외

다음 경우에는 Expand 단계와 지원 기간 없이 계약을 바로 바꿀 수 있습니다.

### 보안·개인정보 문제

권한 없는 사용자가 데이터를 보거나 바꿀 수 있는 등 보안·개인정보 문제를 고치는 변경입니다.

- 기존 클라이언트가 깨지더라도 먼저 막습니다.
- 변경한 사람은 영향받는 팀에 바로 알리고, PR과 릴리스 PR 본문에 예외 사유를 적습니다.

### 아직 쓰는 클라이언트가 없는 계약

서버에만 배포됐고 이 계약을 호출하는 클라이언트가 아직 운영에 배포되지 않았다면, 바꿔도 깨지는 클라이언트가 없습니다.

- 개발 중인 클라이언트 담당자와 합의한 뒤 바꿉니다.
- 스토어에 출시된 앱이나 운영에 배포된 웹이 한 번이라도 호출했다면 이 예외에 해당하지 않습니다.

## 8. 지원 중단 목록

지원 중단을 공지한 계약 목록입니다. [지원 중단 절차](#5-지원-중단-절차)의 3단계, 5단계, 6단계에서 이 표를 함께 수정합니다.

- **쓰는 클라이언트**: 계약을 쓰는 클라이언트와 확인 근거를 적습니다.
- **상태**: `지원 중단` → `삭제됨`

| 계약 | 대체 | 쓰는 클라이언트 | 지원 중단 | 삭제 예정 | 담당자 | 상태 | 관련 PR·이슈 |
|---|---|---|---|---|---|---|---|
| `GET /api/v1/gisu/all` | 없음 | 없음 | 정책 이전 | 2026-11-08 릴리스 | wlgusqkr | 삭제됨 | [#1460](https://github.com/UMC-PRODUCT/umc-product-server/pull/1460) |
| `GET /api/v1/schools/link/{schoolId}` | 없음 | 없음 | 정책 이전 | 2026-11-08 릴리스 | wlgusqkr | 삭제됨 | [#1460](https://github.com/UMC-PRODUCT/umc-product-server/pull/1460) |
| `GET /api/v1/schools/gisu/{gisuId}` | `GET /api/v2/gisu?id={gisuId}&includeSchool=true` | 없음 | 정책 이전 | 2026-11-08 릴리스 | wlgusqkr | 삭제됨 | [#1460](https://github.com/UMC-PRODUCT/umc-product-server/pull/1460) |
| `GET /api/v1/schools/unassigned` | 없음 | 없음 | 정책 이전 | 2026-11-08 릴리스 | wlgusqkr | 삭제됨 | [#1460](https://github.com/UMC-PRODUCT/umc-product-server/pull/1460) |
| `PATCH /api/v1/schools/{schoolId}/assign` | `POST /api/v1/chapters/bulk` | 없음 | 정책 이전 | 2026-11-08 릴리스 | wlgusqkr | 삭제됨 | [#1460](https://github.com/UMC-PRODUCT/umc-product-server/pull/1460) |
| `PATCH /api/v1/schools/{schoolId}/unassign` | `POST /api/v1/chapters/bulk` | 없음 | 정책 이전 | 2026-11-08 릴리스 | wlgusqkr | 삭제됨 | [#1460](https://github.com/UMC-PRODUCT/umc-product-server/pull/1460) |
| `GET /api/v1/challenger/search/global` | `GET /api/v1/challenger/search/cursor`, `GET /api/v1/challenger/search/offset` | 없음 | 정책 이전 | 2026-11-08 릴리스 | wlgusqkr | 삭제됨 | [#1460](https://github.com/UMC-PRODUCT/umc-product-server/pull/1460) |
| `GET /api/v1/projects/{projectId}/statistics` | `GET /api/v1/projects/statistics/applications?projectIds={projectId}` | 없음 | 정책 이전 | 2026-11-08 릴리스 | wlgusqkr | 삭제됨 | [#1460](https://github.com/UMC-PRODUCT/umc-product-server/pull/1460) |
| `GET /api/v1/authorization/challenger-role/{challengerRoleId}` | 내 프로필 조회 | 없음 | 정책 이전 | 2026-11-08 릴리스 | wlgusqkr | 삭제됨 | [#1460](https://github.com/UMC-PRODUCT/umc-product-server/pull/1460) |
| `GET /api/v1/analytics/admin/dashboard/operations` | `GET /api/v1/analytics/admin/dashboard/operations/*` 세부 API | 없음 | 정책 이전 | 2026-11-08 릴리스 | wlgusqkr | 삭제됨 | [#1460](https://github.com/UMC-PRODUCT/umc-product-server/pull/1460) |
| `DELETE /api/v1/notification/fcm/topics/legacy` | 없음 (동작하지 않던 API) | 없음 | 정책 이전 | 2026-11-08 릴리스 | wlgusqkr | 삭제됨 | [#1460](https://github.com/UMC-PRODUCT/umc-product-server/pull/1460) |

> 2026-10-31에 정책 도입 전부터 `@Deprecated`로 남아 있던 항목을 정리했습니다. iOS(3.5.0, `develop`), Android(3.4.4, `develop-compose`), 웹(Operations, Activity, Brand, Auth), macOS 코드에서 호출하지 않는 것을 확인하고 지웠습니다. 웹에서는 OpenAPI로 생성한 타입 정의(`api.d.ts`)에만 경로가 남아 있었습니다.

### 지원 중단을 해제한 계약

아래 계약은 클라이언트가 아직 호출하고 있어 `@Deprecated`를 뺐습니다. 대체 계약으로 옮기려면 해당 클라이언트 담당자와 일정을 정한 뒤 [지원 중단 절차](#5-지원-중단-절차)를 다시 밟습니다.

| 계약 | 대체 후보 | 쓰는 클라이언트 |
|---|---|---|
| `GET /api/v1/gisu/{gisuId}` | `GET /api/v2/gisu?id={gisuId}` | iOS 홈, Android |
| `GET /api/v1/gisu/active` | `GET /api/v2/gisu?active=true` | Android |
| `GET /api/v1/chapters` | 없음 | iOS 공지 작성 |
| `GET /api/v1/chapters/with-schools` | `GET /api/v2/gisu?id={gisuId}&includeChapter=true&includeSchool=true` | iOS 공지 작성 |
| `GET /api/v1/chapters/{chapterId}` | 없음 | iOS 공지 작성, Android |
| `POST /api/v1/project/matching-rounds` | `PUT /api/v1/project/matching-rounds/chapters/{chapterId}` | iOS 운영진 화면 |
| `PATCH /api/v1/project/matching-rounds/{matchingRoundId}` | `PUT /api/v1/project/matching-rounds/chapters/{chapterId}` | iOS 운영진 화면 |
| `DELETE /api/v1/project/matching-rounds/{matchingRoundId}` | `DELETE /api/v1/project/matching-rounds/chapters/{chapterId}?type={type}` | iOS 운영진 화면 |
