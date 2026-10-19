package com.umc.product.global.event.application.port.out;

import java.util.Objects;
import java.util.Set;

public record OutboxEventSelection(
    Set<String> includedTypes,
    Set<String> overriddenTypes,
    String prefix,
    PrefixMatch prefixMatch
) {
    public OutboxEventSelection {
        includedTypes = Set.copyOf(includedTypes);
        overriddenTypes = Set.copyOf(overriddenTypes);
        Objects.requireNonNull(prefixMatch);
        if (prefix == null || prefix.isBlank() || !prefix.equals(prefix.strip())
            || prefix.contains("%") || prefix.contains("_") || prefix.contains("*")) {
            throw new IllegalArgumentException("접두사는 공백이나 와일드카드 없는 값이어야 합니다.");
        }
        if (!overriddenTypes.containsAll(includedTypes)) {
            throw new IllegalArgumentException("포함 타입은 명시적 분류 목록에 있어야 합니다.");
        }
        for (String eventType : overriddenTypes) {
            if (eventType.isBlank() || !eventType.equals(eventType.strip()) || eventType.contains("*")) {
                throw new IllegalArgumentException("잘못된 이벤트 타입입니다.");
            }
        }
    }

    public enum PrefixMatch {
        MATCH,
        NOT_MATCH,
        NONE
    }
}
