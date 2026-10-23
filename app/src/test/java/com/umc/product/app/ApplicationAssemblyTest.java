package com.umc.product.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.umc.product.support.IntegrationTestSupport;

/**
 * 배포 대상 모듈이 실제로 조립됐는지 본다.
 *
 * <p>모듈별 테스트는 자기 모듈만 본다. {@code :blog:test} 는 blog 가 {@code :app} 의 의존에서 빠져도
 * 그대로 통과한다. 그러면 테스트는 전부 초록인데 배포된 서버에는 blog 가 없다.
 *
 * <p>모듈을 추가하면 이 테스트의 목록에도 넣는다. 그래야 {@code app/build.gradle.kts} 등록을 빠뜨린 것이
 * 배포 전에 드러난다.
 */
@DisplayName("애플리케이션 조립")
class ApplicationAssemblyTest extends IntegrationTestSupport {

    /** 모듈마다 대표 빈 하나. 이름이 아니라 타입 존재로 확인한다. */
    private static final Map<String, String> MODULE_BEANS = Map.of(
        "monolith", "com.umc.product.member.application.port.in.query.GetMemberUseCase",
        "blog", "com.umc.product.blog.application.port.in.query.GetBlogContentUseCase"
    );

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("배포 대상 모듈의 빈이 모두 등록된다")
    void registersBeansFromEveryDeployedModule() {
        List<String> missing = MODULE_BEANS.entrySet().stream()
            .filter(entry -> applicationContext.getBeanNamesForType(typeOf(entry.getValue())).length == 0)
            .map(entry -> "%s -> %s".formatted(entry.getKey(), entry.getValue()))
            .toList();

        assertThat(missing)
            .as("모듈이 app/build.gradle.kts 의 의존에서 빠졌는지 확인한다")
            .isEmpty();
    }

    @Test
    @DisplayName("blog 권한 평가기가 authorization 에 수집된다")
    void registersBlogPermissionEvaluators() {
        Object[] evaluators = applicationContext
            .getBeanNamesForType(typeOf("com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator"));

        assertThat(evaluators)
            .as("blog 는 authorization 의 out-port 를 구현한다. 컴파일로는 수집 여부가 검증되지 않는다")
            .isNotEmpty();

        assertThat(applicationContext.getBeansOfType(typeOf(
            "com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator")).keySet())
            .anyMatch(name -> name.toLowerCase().contains("blog"));
    }

    private Class<?> typeOf(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("조립 검증 대상 타입을 찾지 못했다: " + className, e);
        }
    }
}
