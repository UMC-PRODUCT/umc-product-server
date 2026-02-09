package com.umc.product.notification.application.port.in.dto;

public record TopicNotificationCommand(String topic, String title, String body) {
}
