package com.umc.product.global.security.util;

import com.umc.product.global.security.annotation.Public;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class PublicEndpointCollector {

    public static Set<String> collectPublicEndpoints(RequestMappingHandlerMapping handlerMapping) {
        Set<String> publicEndpoints = new HashSet<>();

        Map<RequestMappingInfo, HandlerMethod> handlerMethods =
                handlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();

            // 메서드에 @Public이 있는지 확인
            if (AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Public.class) != null) {
                publicEndpoints.addAll(entry.getKey().getPatternValues());
            }

            // 클래스에 @Public이 있는지 확인
            if (AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Public.class) != null) {
                publicEndpoints.addAll(entry.getKey().getPatternValues());
            }
        }

        return publicEndpoints;
    }
}
