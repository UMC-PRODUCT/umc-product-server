package com.umc.product.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.util.PublicEndpointCollector;
import com.umc.product.global.security.util.PublicEndpointCollector.EndpointMatcher;
import com.umc.product.support.IntegrationTestSupport;

@DisplayName("@Public 핸들러 규칙")
class PublicEndpointRulesIntegrationTest extends IntegrationTestSupport {

    @Autowired
    @Qualifier("requestMappingHandlerMapping") private RequestMappingHandlerMapping handlerMapping;

    @Test
    @DisplayName("@Public 패턴은 로그인이 필요한 다른 핸들러의 경로를 permitAll 로 열지 않는다")
    void 공개_패턴이_로그인_필요_경로를_열지_않는다() {
        // Given
        List<EndpointMatcher> publicEndpoints = PublicEndpointCollector.collectPublicEndpoints(handlerMapping);

        // When
        List<String> overlaps = findOverlaps(publicEndpoints);

        // Then
        assertThat(overlaps).isEmpty();
    }

    @Test
    @DisplayName("@Public 핸들러의 @CurrentMember 는 비로그인 요청을 받도록 required = false 다")
    void 공개_핸들러의_현재_회원은_필수가_아니다() {
        // When
        List<String> requiredMemberParameters = handlerMapping.getHandlerMethods().values().stream()
            .filter(PublicEndpointCollector::isPublic)
            .flatMap(handler -> requiredCurrentMemberParameters(handler).stream())
            .toList();

        // Then
        assertThat(requiredMemberParameters).isEmpty();
    }

    private List<String> findOverlaps(List<EndpointMatcher> publicEndpoints) {
        PathPatternParser parser = handlerMapping.getPatternParser();
        return handlerMapping.getHandlerMethods().entrySet().stream()
            .filter(entry -> !PublicEndpointCollector.isPublic(entry.getValue()))
            .flatMap(entry -> overlapsOf(entry.getKey(), publicEndpoints, parser).stream())
            .toList();
    }

    private List<String> overlapsOf(RequestMappingInfo info, List<EndpointMatcher> publicEndpoints,
                                    PathPatternParser parser) {
        Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
        return info.getPatternValues().stream()
            .flatMap(pattern -> publicEndpoints.stream()
                .filter(endpoint -> sameMethod(endpoint.method(), methods))
                .filter(endpoint -> parser.parse(endpoint.pattern()).matches(samplePathOf(pattern)))
                .map(endpoint -> endpoint.pattern() + " -> " + methods + " " + pattern))
            .toList();
    }

    private PathContainer samplePathOf(String pattern) {
        return PathContainer.parsePath(pattern.replaceAll("\\{[^}]+}", "1"));
    }

    private boolean sameMethod(HttpMethod publicMethod, Set<RequestMethod> methods) {
        return publicMethod == null
            || methods.isEmpty()
            || methods.stream().anyMatch(method -> method.name().equals(publicMethod.name()));
    }

    private List<String> requiredCurrentMemberParameters(HandlerMethod handler) {
        return List.of(handler.getMethodParameters()).stream()
            .filter(parameter -> parameter.hasParameterAnnotation(CurrentMember.class))
            .filter(parameter -> parameter.getParameterAnnotation(CurrentMember.class).required())
            .map(parameter -> handler.getShortLogMessage() + " #" + parameter.getParameterIndex())
            .toList();
    }
}
