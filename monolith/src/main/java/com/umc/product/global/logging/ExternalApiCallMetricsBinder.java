package com.umc.product.global.logging;

import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

@Component
class ExternalApiCallMetricsBinder {

    ExternalApiCallMetricsBinder(OperationalMetrics operationalMetrics) {
        ExternalApiCallLogger.setOperationalMetrics(operationalMetrics);
    }

    @PreDestroy
    void clear() {
        ExternalApiCallLogger.setOperationalMetrics(null);
    }
}
