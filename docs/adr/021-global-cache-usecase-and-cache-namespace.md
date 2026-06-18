# ADR-021: 전역 CacheUseCase와 CacheNamespace 도입

## Status

Accepted

## Context

Figma 댓글 분류 L1 캐시는 Caffeine을 서비스 내부에서 직접 생성해 사용하고 있었다. 이 구조는 단일 인스턴스에서는 단순하지만 다음 문제가 있었다.

- Redis 같은 분산 캐시로 바꾸려면 application service를 수정해야 한다.
- cache key, TTL, maximum size, metric 이름이 서비스 구현 안에 흩어진다.
- positive cache와 negative cache처럼 도메인 의미가 있는 값 표현을 기술 캐시 구현과 분리하기 어렵다.

또한 전역 cache usecase를 그대로 모든 application service가 가져다 쓰면 편해 보이지만, 도메인별 캐시 정책이 application service 전반에 흩어질 위험이 있다.

## Decision

전역 캐시는 `global/cache`에 둔다.

- `CacheUseCase`: 공통 get/put/evict API.
- `CacheStorePort`: Caffeine, Redis 등 저장소 adapter 교체 지점.
- `CacheNamespace`: namespace 값과 metric 이름의 중앙 registry.
- `CacheSpec`: namespace, value type, TTL, maximum size를 함께 전달하는 실행 스펙.
- 기본 adapter는 `CaffeineCacheStoreAdapter`로 시작한다.

도메인 의미가 있는 캐시는 전역 `CacheUseCase`를 직접 쓰지 않고 도메인 wrapper를 둔다.

```text
Domain Application Service
  -> DomainCachePort
      -> DomainCacheAdapter
          -> CacheUseCase
              -> CacheStorePort
```

Figma 댓글 분류는 `FigmaClassificationCachePort`와 `FigmaClassificationCacheAdapter`를 둔다. 이 adapter는 `ClassificationCacheValue`로 positive/negative cache를 표현하고, `CacheNamespace.FIGMA_CLASSIFICATION`을 사용한다.

순수 기술 캐시처럼 도메인 정책이 없는 경우에는 application service가 `CacheUseCase`를 직접 사용할 수 있다. 단, namespace는 반드시 `CacheNamespace`에 등록해야 한다.

## Rationale

도메인 wrapper를 두는 이유는 wrapper가 “캐시 저장소를 감추는 껍데기”라서가 아니다. wrapper는 캐시의 도메인 정책을 한 곳에 모은다.

- 어떤 key를 쓸지
- TTL과 maximum size를 어떤 설정에서 가져올지
- null, miss, negative hit을 어떻게 구분할지
- Redis 전환 시 어떤 value shape으로 직렬화할지
- metric 이름을 어떤 namespace로 유지할지

이 정책이 여러 application service에 흩어지면 Redis 전환 시 `CacheStorePort`만 바꾸는 장점이 사라진다. 반대로 wrapper를 두면 저장소 adapter는 기술 책임만 갖고, 도메인별 의미는 각 도메인 adapter가 유지한다.

`CacheNamespace`를 중앙 enum으로 둔 이유는 도메인별 ErrorCode와 비슷하다. cache namespace는 시스템 전체에서 유일해야 하며, 충돌하면 서로 다른 캐시가 같은 저장소 key space를 공유할 수 있다. 중앙 registry는 namespace 충돌을 코드 리뷰와 테스트에서 잡기 위한 장치다.

## Consequences

### Positive

- Caffeine에서 Redis로 전환할 때 `CacheStorePort` 구현체를 추가하거나 교체하면 된다.
- 도메인 정책이 있는 캐시는 wrapper 단위로 테스트할 수 있다.
- metric 이름을 유지한 채 내부 저장소를 바꿀 수 있다.
- namespace 충돌을 `CacheNamespace.validateUniqueValues()` 테스트로 확인할 수 있다.

### Negative

- 단순 캐시 하나를 추가할 때도 namespace 등록과 wrapper 여부 판단이 필요하다.
- wrapper가 무의미한 pass-through가 되면 클래스 수만 늘어난다.

## Usage Rule

1. 도메인 의미가 있는 캐시는 `DomainCachePort`를 만든다.
2. 단순 기술 캐시는 `CacheUseCase` 직접 사용을 허용한다.
3. 모든 cache namespace는 `CacheNamespace`에 등록한다.
4. Redis 전환 시 도메인 wrapper는 유지하고 `CacheStorePort` 구현체를 교체한다.
5. wrapper끼리 충돌하지 않도록 namespace 값은 도메인/기능 단위로 작성한다. 예: `figma.classification`.

## References

- [Figma LLM 캐시 구조 분석](../guides/Figma_LLM_캐시_구조_분석.md)
- [ADR-019: 이벤트 손실 방지를 위한 Transactional Event Outbox 도입](./019-introduce-transactional-event-outbox.md)
