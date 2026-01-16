package com.umc.product.notification.application.port.in;


public record NotificationCommand(Long memberId, String title, String body) {
}
