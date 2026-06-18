# ADR-023: 에러코드는 구조화된 정의 객체로 선언한다

## Status

Proposed

## Context

2026년 6월 기준 서버는 모든 도메인 에러코드를 `BaseCode` 구현 enum으로 선언한다. `BaseCode`는 `HttpStatus getHttpStatus()`, `String getCode()`, `String getMessage()`만 요구하며, 각 enum은 `HttpStatus`, `code`, `message` 필드를 반복해서 가진다.

- 공통 인터페이스: `src/main/java/com/umc/product/global/response/code/BaseCode.java`
- 예시 enum: `src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java`
- ErrorCode catalog 생성: `gradle/documentation-catalog.gradle.kts`
- ErrorCode catalog 서빙: `GET /api/v1/docs/error-codes`
- ErrorCode catalog 조회용 Backoffice: `umc-product-frontend`의 `error-code-catalog` feature

이번 ErrorCode catalog 작업에서는 OpenAPI/Scalar가 API 문서의 source of truth로 유지되므로 custom API catalog는 제거하고, ErrorCode만 독립 규격으로 제공하기로 했다. ErrorCode catalog v1 응답은 기존 `ApiResponse` wrapper 안에 `schemaVersion`, `service`, `generatedAt`, `totalCount`, `items`를 담으며, 각 item은 `domain`, `code`, `name`, `httpStatus`, `message`, `description`, `clientAction`, `retryable`, `severity`, `deprecated`, `replacementCode`, `owners`, `tags`, `source`를 포함한다.

초기 구현에서는 optional metadata를 코드 가까이에 두기 위해 enum constant에 `@ErrorCodeSpec`를 붙이는 방식을 도입했다. 이 방식은 Swagger/OpenAPI annotation처럼 계약 정보를 코드 근처에 둔다는 장점이 있다. 그러나 모든 에러코드에 `description`, `clientAction`, `retryable`, `severity`, `owners`, `tags` 등을 annotation으로 붙이면 enum 선언이 급격히 장황해지고, 실제 런타임 에러 정의보다 문서 메타데이터가 더 크게 보이는 문제가 생긴다.

별도 YAML sidecar 파일로 metadata를 분리하는 대안도 검토했다. 하지만 ErrorCode는 코드와 클라이언트 계약에 가까운 정보이므로 별도 파일을 추가하면 enum과 metadata 사이에 drift가 생길 수 있고, 관리 포인트가 늘어난다. 특히 `severity`, `retryable`, `deprecated`, `replacementCode`는 단순 문서 설명이 아니라 에러코드별 정책에 가까우므로 코드와 멀어지는 것이 좋지 않다.

또한 enum 단위 기본 annotation으로 `severity`나 `retryable`을 묶는 방식도 검토했지만, 같은 도메인 enum 안에서도 validation 오류, 인증 오류, 외부 시스템 장애, 서버 내부 오류의 심각도와 재시도 가능 여부는 다를 수 있다. 따라서 `severity`와 `retryable`은 enum 그룹 속성이 아니라 개별 에러코드의 속성으로 취급해야 한다.

이 프로젝트는 Hexagonal Architecture를 따른다. ErrorCode enum은 도메인 또는 global domain 계층에서 사용되므로 `application`이나 `adapter`에 의존해서는 안 된다. ErrorCode catalog를 외부에 제공하는 책임은 `documentation` 도메인의 application service와 web adapter에 두되, 각 비즈니스 도메인의 내부 model이나 repository를 직접 참조하지 않는다. 도메인 간 직접 참조 금지 규칙과 dependency direction rule을 침해하지 않아야 한다.

### 문제점

1. 현재 ErrorCode 선언은 세 개의 loose field에 의존한다. `HttpStatus`, `code`, `message`가 항상 함께 움직여야 하는 하나의 개념인데도 구조적으로 묶여 있지 않아, 포맷 검증이나 catalog 확장 지점이 enum마다 흩어진다.

2. `@ErrorCodeSpec`를 모든 enum constant에 확산하면 유지보수성이 떨어진다. 에러코드가 늘어날수록 annotation block, import, enum constant 선언이 함께 커지고, 에러 정의를 훑어보는 비용이 커진다.

3. 별도 YAML 파일은 enum을 깨끗하게 유지하지만 source of truth가 둘로 나뉜다. 개발자가 에러코드를 추가하면서 YAML을 잊거나, YAML에는 있지만 enum에는 없는 code가 생기면 Backoffice catalog의 신뢰도가 떨어진다.

4. `severity`와 `retryable`을 도메인 enum 단위로 합치면 잘못된 기본값이 생길 수 있다. 이 두 값은 에러코드별 운영 정책에 가까우므로 개별 code의 속성으로 남겨야 한다.

5. ErrorCode catalog는 Backoffice에서 검색/필터링되는 계약 문서이므로 JSON format은 안정적으로 유지되어야 한다. 내부 선언 구조를 바꾸더라도 `/api/v1/docs/error-codes` 응답 shape는 깨지지 않아야 한다.

### 결정이 필요한 이유

ErrorCode catalog가 Backoffice에서 사용되기 시작하면 catalog JSON은 운영자가 확인하는 문서이자 프론트엔드가 소비하는 계약이 된다. 지금 per-code annotation 방식을 그대로 확산시키면 이후 모든 도메인 enum을 수정할 때 annotation 유지 비용이 함께 증가한다.

반대로 구조 개선 없이 optional metadata를 모두 비워두면 catalog는 코드 목록 이상의 의미를 갖기 어렵다. 따라서 ErrorCode 선언 자체를 먼저 구조화하고, catalog metadata를 나중에 자연스럽게 확장할 수 있는 기반을 만들어야 한다.

## Decision

우리는 ErrorCode를 `HttpStatus`, `code`, `message`의 loose field 조합이 아니라 `ErrorCodeDefinition` value object를 통해 선언하기로 결정한다.

1. `ErrorCodeDefinition`은 에러코드의 핵심 런타임 정의를 표현한다. 첫 단계에서는 `httpStatus`, `code`, `message`를 포함한다.
2. `BaseCode`는 기존 `getHttpStatus()`, `getCode()`, `getMessage()` 계약을 유지하되, `getDefinition()` 중심 구조로 점진 전환할 수 있는 bridge를 제공한다.
3. 기존 enum은 한 번에 모두 변경하지 않고 domain별로 단계적으로 `ErrorCodeDefinition` 선언 방식으로 옮긴다.
4. `@ErrorCodeSpec` 같은 per-code annotation은 기본 선언 방식으로 확산하지 않는다. 현재 catalog v1 optional fields는 값이 없으면 `null`, `false`, `[]` 기본값으로 유지한다.
5. 별도 YAML sidecar 파일은 기본 source of truth로 도입하지 않는다. ErrorCode의 계약성 metadata는 코드 근처에 두되, annotation block이 아니라 `ErrorCodeDefinition`의 확장 지점으로 둔다.
6. `severity`, `retryable`, `deprecated`, `replacementCode`는 enum 그룹 기본값이 아니라 개별 에러코드의 metadata로 취급한다. 다만 metadata를 강제 입력하지 않고, catalog generator가 `httpStatus` 기반 기본 추론값 또는 `null`을 사용할 수 있게 한다.
7. `/api/v1/docs/error-codes`와 ErrorCode Catalog v1 JSON shape는 유지한다. OpenAPI vendor extension이나 Scalar 통합은 이 결정의 범위에 포함하지 않는다.

예상 선언 형태는 다음과 같다.

```java
@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements BaseCode {

    MEMBER_NOT_FOUND(ErrorCodeDefinition.notFound(
        "MEMBER-0001",
        "사용자를 찾을 수 없어요. 선택한 사용자를 확인해주세요."
    )),

    MEMBER_ALREADY_EXISTS(ErrorCodeDefinition.conflict(
        "MEMBER-0002",
        "이미 등록된 사용자예요. 기존 계정을 확인해주세요."
    )),
    ;

    private final ErrorCodeDefinition definition;
}
```

`BaseCode`는 전환 기간 동안 기존 enum과 신규 enum을 모두 수용한다.

```java
public interface BaseCode {

    default ErrorCodeDefinition getDefinition() {
        return ErrorCodeDefinition.of(getHttpStatus(), getCode(), getMessage());
    }

    default HttpStatus getHttpStatus() {
        return getDefinition().httpStatus();
    }

    default String getCode() {
        return getDefinition().code();
    }

    default String getMessage() {
        return getDefinition().message();
    }
}
```

기존 enum은 Lombok이 생성한 `getHttpStatus()`, `getCode()`, `getMessage()`가 default method를 override하므로 그대로 동작한다. 신규 enum은 `definition` 필드의 `getDefinition()`만 제공하면 기존 호출부가 계속 `getHttpStatus()`, `getCode()`, `getMessage()`를 사용할 수 있다.

### 단계적 진행 / PR 분할

- **Phase 1 (현재 ErrorCode catalog PR 보정)**: `GET /api/v1/docs/error-codes`와 catalog v1 JSON format은 유지한다. per-code annotation 확산을 막기 위해 `@ErrorCodeSpec`, `ErrorCodeRetryable`, `ErrorCodeSeverity`를 기본 경로에서 제거하거나 더 이상 사용하지 않도록 정리한다. optional fields는 기본값으로 생성한다.
- **Phase 2 (별도 PR)**: `ErrorCodeDefinition`을 도입하고 `BaseCode`에 점진 전환 bridge를 추가한다. `DocumentationErrorCode` 또는 작은 enum 하나를 기준으로 신규 선언 방식을 검증한다.
- **Phase 3 (별도 PR들)**: 도메인별 ErrorCode enum을 `ErrorCodeDefinition` 기반 선언으로 순차 migration한다. 각 PR은 catalog 생성 결과가 의미적으로 동일한지 검증한다.
- **Phase 4 (별도 PR)**: `ErrorCodeDefinition`에 개별 code metadata 확장 지점을 추가한다. `severity`, `retryable`, `deprecated`, `replacementCode`, `clientAction`, `description`은 필요한 code부터 명시하고, 나머지는 catalog 기본값을 유지한다.
- **Phase 5 (시점 미정)**: 모든 enum migration 후 legacy loose field 선언을 금지하는 validation을 추가한다. 이후 `BaseCode`에서 bridge 성격의 fallback을 제거할지 결정한다.

## Alternatives Considered

### 대안 A: 현행 loose field 구조 유지

각 enum이 계속 `private final HttpStatus httpStatus`, `private final String code`, `private final String message`를 직접 가진다.

장점:

- 변경량이 가장 작다.
- 기존 호출부와 Lombok getter 구조를 그대로 유지할 수 있다.
- catalog generator의 현재 source parsing 구조를 크게 바꾸지 않아도 된다.

단점:

- ErrorCode의 핵심 속성이 구조화되지 않는다.
- 포맷 검증, prefix 검증, catalog metadata 확장 지점이 계속 분산된다.
- optional metadata를 추가하려면 annotation이나 별도 파일 같은 보조 구조가 필요해진다.

선택하지 않은 이유:

- ErrorCode catalog를 운영 문서로 제공하기 시작하는 시점에는 ErrorCode 자체의 선언 구조를 먼저 안정화해야 한다. loose field 구조는 장기 확장 지점이 약하다.

### 대안 B: per-code annotation 확산

모든 enum constant에 `@ErrorCodeSpec`를 붙여 `description`, `clientAction`, `retryable`, `severity`, `owners`, `tags` 등을 선언한다.

장점:

- Swagger/OpenAPI annotation처럼 metadata가 코드 바로 옆에 위치한다.
- enum constructor를 바꾸지 않고 optional metadata를 추가할 수 있다.
- 특정 code에만 metadata를 붙이는 것이 쉽다.

단점:

- enum constant마다 annotation block이 커져 가독성이 떨어진다.
- `severity`, `retryable`처럼 에러 정책에 가까운 값이 core definition 밖 annotation에 분리된다.
- SOURCE retention annotation을 Gradle source parser가 읽어야 하므로 parser 규칙과 Java 작성 방식의 결합이 커진다.

선택하지 않은 이유:

- 초기에는 빠르게 catalog optional fields를 채울 수 있지만, 모든 ErrorCode enum으로 확산되면 선언부가 문서 annotation 중심으로 변한다. 장기적으로는 ErrorCode 구조를 개선하지 못하고 annotation 유지 비용만 늘어난다.

### 대안 C: YAML sidecar metadata

`src/main/resources/docs/error-codes.yml` 같은 별도 파일에서 code별 `description`, `clientAction`, `retryable`, `severity`, `owners`, `tags`를 관리한다.

장점:

- Java enum은 간결하게 유지된다.
- 운영 문서성 필드를 대량으로 편집하기 쉽다.
- 비개발자가 metadata를 관리해야 하는 요구가 생기면 확장성이 있다.

단점:

- enum과 YAML이라는 두 source of truth가 생긴다.
- 에러코드를 추가할 때 YAML 갱신을 잊기 쉽다.
- `retryable`, `severity`, `deprecated`, `replacementCode`처럼 계약에 가까운 값이 코드와 멀어진다.

선택하지 않은 이유:

- 현재 요구는 비개발자 편집이 아니라 서버 코드에서 정의한 ErrorCode를 Backoffice에서 안정적으로 확인하는 것이다. 이 상황에서는 별도 YAML이 관리 부담을 줄이기보다 drift 가능성을 키운다.

### 대안 D: enum 단위 metadata 기본값

`@ErrorCodeCatalogGroup` 같은 enum-level annotation으로 `owners`, `tags`, `severity`, `retryable` 기본값을 지정하고, 예외 code만 override한다.

장점:

- per-code annotation 반복을 줄일 수 있다.
- `owners`, `tags`처럼 도메인 단위로 묶기 쉬운 값에는 효과적이다.

단점:

- `severity`, `retryable`은 같은 enum 안에서도 code별로 달라질 수 있다.
- 기본값이 강하게 적용되면 운영자가 catalog에서 잘못된 retry 정책이나 심각도를 볼 수 있다.
- override 누락이 리뷰에서 잘 드러나지 않을 수 있다.

선택하지 않은 이유:

- `owners`, `tags`에는 쓸 수 있어도 `severity`, `retryable`에는 부적절하다. 이번 결정의 핵심은 ErrorCode 자체를 구조화하는 것이므로 그룹 annotation을 기본 모델로 삼지 않는다.

### 대안 E: OpenAPI vendor extension 통합

OpenAPI schema나 vendor extension에 ErrorCode catalog를 넣고 Scalar 같은 OpenAPI 기반 도구에서 함께 노출한다.

장점:

- API 문서와 ErrorCode 문서를 하나의 문서 도구에서 볼 수 있다.
- OpenAPI ecosystem의 렌더링 도구를 활용할 수 있다.

단점:

- ErrorCode는 특정 endpoint response schema만이 아니라 서비스 전체의 독립 catalog다.
- OpenAPI vendor extension은 Backoffice 검색/필터링용 안정 JSON 계약으로 쓰기 어렵다.
- 이미 API 문서는 OpenAPI/Scalar가 담당하고 있어 custom API catalog 제거 방향과 충돌한다.

선택하지 않은 이유:

- API catalog와 ErrorCode catalog의 책임을 분리하기로 했다. ErrorCode는 독립 JSON manifest와 Backoffice viewer로 제공하는 것이 더 명확하다.

### 대안 F: `ErrorCodeDefinition` value object

`HttpStatus`, `code`, `message`를 하나의 value object로 묶고, `BaseCode`가 이 정의 객체 중심으로 동작하도록 점진 전환한다.

장점:

- ErrorCode 핵심 속성을 하나의 개념으로 표현한다.
- 기존 `BaseCode` 호출부를 유지하면서 enum 선언을 점진 migration할 수 있다.
- 포맷 검증, prefix 검증, catalog metadata 확장을 한 지점에 모을 수 있다.
- metadata를 코드 근처에 두되 annotation block이나 YAML에 의존하지 않는다.

단점:

- 모든 enum을 한 번에 migration하지 않더라도 generator와 테스트가 legacy/new 선언을 모두 이해해야 한다.
- 초기에 `BaseCode` default bridge가 다소 복잡해진다.
- 팀이 새 선언 규칙을 익혀야 한다.

선택한 이유:

- 코드 근접성, catalog 안정성, 장기 확장성을 가장 균형 있게 만족한다. annotation 확산과 YAML drift를 피하면서도 ErrorCode 자체의 모델링을 개선한다.

## Consequences

### Positive

- ErrorCode가 `HttpStatus`, `code`, `message`의 느슨한 묶음이 아니라 명시적인 정의 객체가 된다.
- 기존 exception handling과 API response 호출부는 `BaseCode` getter를 계속 사용할 수 있다.
- ErrorCode catalog generator가 점진적으로 더 구조화된 입력을 읽을 수 있다.
- optional metadata를 도입하더라도 per-code annotation을 반복하지 않고 definition 확장으로 처리할 수 있다.
- `/api/v1/docs/error-codes` 응답 shape를 유지하므로 Backoffice 변경을 최소화할 수 있다.

### Negative

- `BaseCode`에 legacy/new enum을 모두 수용하는 bridge가 생기므로 인터페이스가 일시적으로 복잡해진다.
- 도메인별 enum migration PR이 여러 개 필요하다.
- catalog generator는 전환 기간 동안 기존 constructor 방식과 신규 `ErrorCodeDefinition` 방식을 모두 처리해야 한다.
- source line 정보를 유지하려면 여전히 source scanning 또는 parser 기반 보조 로직이 필요하다.

### Neutral / Trade-offs

- metadata를 즉시 모두 채우기보다 구조를 먼저 안정화한다. 따라서 catalog v1은 당분간 일부 optional fields가 `null`, `false`, `[]`로 남을 수 있다.
- YAML을 도입하지 않으므로 비개발자 편집성은 낮다. 대신 code review와 테스트를 통해 ErrorCode 계약을 관리한다.
- `severity`, `retryable`을 code별 속성으로 두기 때문에 반복이 일부 생길 수 있다. 이 반복은 enum-level 기본값으로 숨기지 않고 명시성으로 감수한다.

## Implementation Notes

### 변경 영역 요약

1. **도메인 / Global Code** (`com.umc.product.global.response.code.*`): `ErrorCodeDefinition`을 추가하고 `BaseCode`에 점진 전환 bridge를 추가한다. static factory method는 `of`, `badRequest`, `unauthorized`, `forbidden`, `notFound`, `conflict`, `internalServerError`처럼 자주 쓰는 HTTP status 중심으로 제공한다.
2. **도메인 ErrorCode enum** (`com.umc.product.<domain>.*ErrorCode`): 기존 `HttpStatus, code, message` constructor를 유지하면서, domain별로 `ErrorCodeDefinition` 필드 선언으로 migration한다.
3. **문서 생성 Gradle task** (`gradle/documentation-catalog.gradle.kts`): legacy enum constructor와 신규 `ErrorCodeDefinition` declaration을 모두 인식하도록 generator를 갱신한다. catalog v1 JSON schema는 유지한다.
4. **문서 서빙** (`com.umc.product.documentation.*`): `GET /api/v1/docs/error-codes` controller/service 계약은 유지한다.
5. **설정 / 정적 리소스** (`src/main/resources/static/docs/catalog/error/*`): generated ErrorCode catalog, markdown, schema, static viewer는 유지한다. custom API catalog 산출물은 다시 도입하지 않는다.
6. **테스트** (`src/test/...`): `BaseCode` legacy/new bridge 테스트, `ErrorCodeDefinition` validation 테스트, catalog generator fixture 테스트, `/api/v1/docs/error-codes` controller/service 테스트를 유지 또는 추가한다.

### 실행 계획

1. **현재 catalog PR 정리**
   - `@ErrorCodeSpec`, `ErrorCodeRetryable`, `ErrorCodeSeverity`를 전 에러코드에 붙이는 방향을 중단한다.
   - 현재 PR에 annotation이 포함되어 있다면 제거하거나 사용하지 않는 상태로 정리한다.
   - `generatedAt: null`, missing metadata 기본값, `/api/v1/docs/error-codes` 응답 shape는 유지한다.
   - 검증: `./gradlew generateDocumentationCatalogs`, `./gradlew checkDocumentationCatalogs`, `./gradlew validateDocumentationCatalogs -PstrictDocumentationCatalogs=true`, 관련 documentation 테스트, `./gradlew bootJar`.

2. **`ErrorCodeDefinition` 기반 도입 PR**
   - `ErrorCodeDefinition` record 또는 final value object를 추가한다.
   - code format, blank message, null `HttpStatus`에 대한 생성 시점 validation을 넣는다.
   - `BaseCode`에 `getDefinition()` bridge를 추가해 기존 enum과 신규 enum이 공존하게 한다.
   - `DocumentationErrorCode`처럼 범위가 작은 enum 하나를 신규 방식으로 migration한다.
   - 검증: 기존 exception handling 테스트, 신규 `BaseCode` bridge 단위 테스트, catalog 생성 결과 비교.

3. **Catalog generator 전환 PR**
   - generator가 `ErrorCodeDefinition.of(...)`, `notFound(...)`, `badRequest(...)` 같은 factory declaration을 읽도록 갱신한다.
   - legacy constructor parsing은 전환 기간 동안 유지한다.
   - source `enumName`, `file`, `line` 추출은 계속 유지한다.
   - strict validation에 중복 code, code prefix format, schemaVersion, required field 누락을 포함한다.

4. **도메인별 migration PR**
   - 변경량을 줄이기 위해 `authentication`, `member`, `common`, 나머지 도메인 순으로 작게 나눈다.
   - 각 PR에서 catalog JSON의 `code`, `httpStatus`, `message`, `source`가 의미적으로 동일한지 확인한다.
   - migration 중에는 프론트엔드 contract 변경을 만들지 않는다.

5. **Metadata 확장 PR**
   - `severity`, `retryable`, `deprecated`, `replacementCode`를 `ErrorCodeDefinition`의 개별 code metadata로 추가한다.
   - `owners`, `tags`, `description`, `clientAction`은 강제 필드가 아니라 optional documentation metadata로 유지한다.
   - `severity`와 `retryable`은 enum-level default로 묶지 않는다.
   - `httpStatus` 기반 추론값을 쓸 경우 catalog에서 추론값과 명시값을 구분할 필요가 있는지 별도 검토한다.

6. **Legacy 선언 금지 PR**
   - 모든 enum migration 후 strict validation에서 legacy `HttpStatus, code, message` constructor 선언을 금지한다.
   - `BaseCode` bridge fallback 제거 여부를 판단한다. 외부 호출부 안정성을 위해 제거하지 않고 유지할 수도 있다.

### 롤백 시 주의할 점

- `/api/v1/docs/error-codes` 응답 shape는 Backoffice와 연결되어 있으므로 내부 선언 구조 rollback과 API rollback을 분리한다.
- `ErrorCodeDefinition` migration은 domain별 PR로 나누어, 문제가 생기면 해당 domain enum 변경만 되돌린다.
- generated catalog JSON은 대량 diff가 발생할 수 있으므로 rollback 시 generator 변경과 generated artifact 변경을 같은 커밋 단위로 관리한다.

## References

- Backend PR: [UMC-PRODUCT/umc-product-server#967](https://github.com/UMC-PRODUCT/umc-product-server/pull/967)
- Frontend PR: [UMC-PRODUCT/umc-product-frontend#20](https://github.com/UMC-PRODUCT/umc-product-frontend/pull/20)
