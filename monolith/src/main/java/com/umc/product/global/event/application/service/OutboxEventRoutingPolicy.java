package com.umc.product.global.event.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class OutboxEventRoutingPolicy {

    private final Set<OutboxProcessingGroup> groups;
    private final OutboxProcessingGroup defaultGroup;
    private final Map<String, OutboxProcessingGroup> eventTypes;

    public OutboxEventRoutingPolicy(
        Set<OutboxProcessingGroup> groups,
        OutboxProcessingGroup defaultGroup,
        List<Rule> rules
    ) {
        this.groups = Set.copyOf(groups);
        this.defaultGroup = Objects.requireNonNull(defaultGroup);
        if (!this.groups.contains(defaultGroup) || !this.groups.contains(OutboxProcessingGroup.REALTIME)) {
            throw new IllegalArgumentException("기본 처리 그룹과 실시간 처리 그룹이 등록되어 있어야 합니다.");
        }
        Map<String, OutboxProcessingGroup> types = new HashMap<>();
        for (Rule rule : rules) {
            if (!this.groups.contains(rule.group())) {
                throw new IllegalArgumentException("라우팅 규칙의 처리 그룹이 등록되어 있지 않습니다.");
            }
            if (types.putIfAbsent(rule.eventType(), rule.group()) != null) {
                throw new IllegalArgumentException("중복된 이벤트 라우팅 규칙입니다: " + rule.eventType());
            }
        }
        eventTypes = Map.copyOf(types);
    }

    // 명시적인 타입 분류를 우선하고, Chat은 실시간 그룹으로, 나머지는 기본 그룹으로 분류한다.
    public OutboxProcessingGroup resolve(String eventType) {
        validateEventType(eventType);
        OutboxProcessingGroup exact = eventTypes.get(eventType);
        if (exact != null) {
            return exact;
        }
        return eventType.startsWith("chat.") ? OutboxProcessingGroup.REALTIME : defaultGroup;
    }

    public Set<OutboxProcessingGroup> groups() {
        return groups;
    }

    private static void validateEventType(String eventType) {
        if (eventType == null || eventType.isBlank() || !eventType.equals(eventType.strip())
            || eventType.contains("*")) {
            throw new IllegalArgumentException("이벤트 타입은 공백이나 와일드카드가 없는 값이어야 합니다.");
        }
    }

    public record Rule(String eventType, OutboxProcessingGroup group) {

        public Rule {
            validateEventType(eventType);
            Objects.requireNonNull(group);
        }
    }
}
