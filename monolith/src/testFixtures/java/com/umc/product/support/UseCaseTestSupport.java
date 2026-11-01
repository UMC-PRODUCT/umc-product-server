package com.umc.product.support;

/**
 * UseCase 단위의 통합 테스트를 위한 베이스 클래스.
 * <p>
 * 현재는 {@link IntegrationTestSupport} 의 모든 구성을 그대로 상속하며, 호환성 유지 목적으로 남아 있다.
 * </p>
 * 신규 통합 테스트는 의도를 명확히 하기 위해 {@link IntegrationTestSupport} 를 직접 상속하라.
 *
 * @deprecated {@link IntegrationTestSupport} 를 사용해주세요. 본 클래스는 점진적으로 제거될 예정입니다.
 */
@Deprecated(since = "v2.0.0")
public abstract class UseCaseTestSupport extends IntegrationTestSupport {
}
