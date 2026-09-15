package com.umc.product.notification.application.port.in.dto;


public record NotificationCommand(Long memberId, String title, String body) {
}
