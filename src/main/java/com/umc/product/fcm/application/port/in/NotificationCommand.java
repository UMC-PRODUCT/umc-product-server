package com.umc.product.fcm.application.port.in;


public record NotificationCommand(Long memberId, String title, String body) {
}
