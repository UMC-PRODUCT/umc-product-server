package com.umc.product.global.event.application.port.out;

public record OutboxEventSelection(String processingGroup) {

    public OutboxEventSelection {
        if (processingGroup == null || processingGroup.length() > 50 || !processingGroup.matches("[a-z][a-z0-9-]*")) {
            throw new IllegalArgumentException("처리 그룹 이름은 소문자로 시작하는 영문, 숫자, 하이픈이어야 합니다.");
        }
    }
}
