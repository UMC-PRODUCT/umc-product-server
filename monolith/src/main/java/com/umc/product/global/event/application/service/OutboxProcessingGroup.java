package com.umc.product.global.event.application.service;

public record OutboxProcessingGroup(String name) {

    public static final OutboxProcessingGroup REALTIME = new OutboxProcessingGroup("realtime");
    public static final OutboxProcessingGroup BACKGROUND = new OutboxProcessingGroup("background");

    public OutboxProcessingGroup {
        if (name == null || !name.matches("[a-z][a-z0-9-]*")) {
            throw new IllegalArgumentException("처리 그룹 이름은 소문자로 시작하는 영문, 숫자, 하이픈이어야 합니다.");
        }
    }
}
