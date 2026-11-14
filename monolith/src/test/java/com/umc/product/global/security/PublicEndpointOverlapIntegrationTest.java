package com.umc.product.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.global.security.util.PublicEndpointCollector;
import com.umc.product.global.security.util.PublicEndpointCollector.EndpointMatcher;
import com.umc.product.support.IntegrationTestSupport;

@DisplayName("@Public 경로와 로그인 필요 경로의 겹침")
class PublicEndpointOverlapIntegrationTest extends IntegrationTestSupport {

    @Autowired
    @Qualifier("requestMappingHandlerMapping") private RequestMappingHandlerMapping handlerMapping;

    @Test
    @DisplayName("@Public 패턴은 로그인이 필요한 다른 핸들러의 경로를 permitAll 로 열지 않는다")
    void 공개_패턴이_로그인_필요_경로를_열지_않는다() {
        List<EndpointMatcher> publicEndpoints = PublicEndpointCollector.collectPublicEndpoints(handlerMapping);

        List<String> overlaps = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMapping.getHandlerMethods().entrySet()) {
            if (isPublic(entry.getValue())) {
                continue;
            }
            Set<RequestMethod> methods = entry.getKey().getMethodsCondition().getMethods();
            for (String pattern : entry.getKey().getPatternValues()) {
                PathContainer samplePath = PathContainer.parsePath(pattern.replaceAll("\\{[^}]+}", "1"));
                for (EndpointMatcher publicEndpoint : publicEndpoints) {
                    PathPattern publicPattern = PathPatternParser.defaultInstance.parse(publicEndpoint.pattern());
                    if (sameMethod(publicEndpoint.method(), methods) && publicPattern.matches(samplePath)) {
                        overlaps.add(publicEndpoint.pattern() + " -> " + methods + " " + pattern);
                    }
                }
            }
        }

        assertThat(overlaps).isEmpty();
    }

    private boolean isPublic(HandlerMethod handlerMethod) {
        return AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Public.class) != null
            || AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Public.class) != null;
    }

    private boolean sameMethod(HttpMethod publicMethod, Set<RequestMethod> methods) {
        return publicMethod == null
            || methods.isEmpty()
            || methods.stream().anyMatch(method -> method.name().equals(publicMethod.name()));
    }
}
