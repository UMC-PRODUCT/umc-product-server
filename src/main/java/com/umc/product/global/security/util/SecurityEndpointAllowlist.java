package com.umc.product.global.security.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.http.HttpMethod;

import com.umc.product.global.config.SecurityPathConfig;

public final class SecurityEndpointAllowlist {

    private static final List<SecurityEndpoint> RECONSENT_FLOW_ENDPOINTS = List.of(
        SecurityEndpoint.of(HttpMethod.GET, "/api/v1/terms"),
        SecurityEndpoint.of(HttpMethod.GET, "/api/v1/terms/**"),
        SecurityEndpoint.of(HttpMethod.POST, "/api/v1/terms/agreements")
    );

    public static final List<SecurityEndpoint> INFRASTRUCTURE_ENDPOINTS = Stream.concat(
            SecurityPathConfig.SECURITY_PERMIT_ALL_PATHS.stream(),
            SecurityPathConfig.SWAGGER_BLOCKED_PATHS.stream()
        )
        .map(SecurityEndpoint::any)
        .toList();

    private SecurityEndpointAllowlist() {
    }

    public static List<SecurityEndpoint> reconsentBypassEndpoints(List<SecurityEndpoint> publicEndpoints) {
        return merge(RECONSENT_FLOW_ENDPOINTS, INFRASTRUCTURE_ENDPOINTS, publicEndpoints);
    }

    @SafeVarargs
    private static List<SecurityEndpoint> merge(List<SecurityEndpoint>... endpointGroups) {
        List<SecurityEndpoint> endpoints = new ArrayList<>();
        for (List<SecurityEndpoint> endpointGroup : endpointGroups) {
            endpoints.addAll(endpointGroup);
        }
        return List.copyOf(endpoints);
    }
}
