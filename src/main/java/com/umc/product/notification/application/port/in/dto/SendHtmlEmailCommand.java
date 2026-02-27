package com.umc.product.notification.application.port.in.dto;

public record SendHtmlEmailCommand(
        String to,
        String subject,
        String htmlContent
) {
}
