package com.umc.product.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import com.umc.product.audit.application.port.in.annotation.Audited;

/**
 * CommandService 의 상태 변경 메서드가 {@link Audited} 를 제대로 달았는지 검사한다.
 *
 * <p>대상을 문자열로 지정하고 리플렉션으로 찾는다. 컴파일 의존이 생기지 않으므로 모듈이 나뉜 뒤에도
 * 각 모듈이 자기 도메인 spec 만 들고 검사하면 된다. 반대로 다른 모듈 클래스를 문자열로 적으면
 * 컴파일은 통과하고 실행에서만 깨지므로, spec 은 반드시 자기 모듈 것만 둔다.
 */
public final class AuditPolicyAssert {

    private AuditPolicyAssert() {
    }

    public static AuditSpec spec(
        String serviceClassName,
        String methodName,
        String domain,
        String action,
        String targetType,
        Class<?>... parameterTypes
    ) {
        return new AuditSpec(serviceClassName, methodName, domain, action, targetType, parameterTypes);
    }

    /** {@code com.umc.product.} 를 앞에 붙여 클래스를 찾는다. */
    public static Class<?> type(String shortName) {
        try {
            return Class.forName("com.umc.product." + shortName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("감사 로그 정책 테스트 타입 조회 실패: " + shortName, e);
        }
    }

    public static void assertAllAudited(List<AuditSpec> specs) {
        List<String> violations = specs.stream()
            .filter(spec -> !matchesAuditPolicy(spec))
            .map(AuditSpec::describe)
            .toList();

        assertThat(violations).isEmpty();
    }

    private static boolean matchesAuditPolicy(AuditSpec spec) {
        Audited audited = getMethod(spec).getAnnotation(Audited.class);
        return audited != null
            && audited.domain().name().equals(spec.domain())
            && audited.action().name().equals(spec.action())
            && audited.targetType().equals(spec.targetType());
    }

    private static Method getMethod(AuditSpec spec) {
        try {
            return type(spec.serviceClassName()).getMethod(spec.methodName(), spec.parameterTypes());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("감사 로그 정책 테스트 메서드 조회 실패: " + spec.describe(), e);
        }
    }

    public record AuditSpec(
        String serviceClassName,
        String methodName,
        String domain,
        String action,
        String targetType,
        Class<?>[] parameterTypes
    ) {

        String describe() {
            return "%s#%s expected domain=%s action=%s targetType=%s"
                .formatted(serviceClassName, methodName, domain, action, targetType);
        }
    }
}
