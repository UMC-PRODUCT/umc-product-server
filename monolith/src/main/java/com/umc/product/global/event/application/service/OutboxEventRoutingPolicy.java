package com.umc.product.global.event.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.umc.product.global.event.application.port.out.OutboxEventSelection;
import com.umc.product.global.event.application.port.out.OutboxEventSelection.PrefixMatch;

public final class OutboxEventRoutingPolicy {

    private final Set<OutboxProcessingGroup> groups;
    private final OutboxProcessingGroup defaultGroup;
    private final Map<String, OutboxProcessingGroup> eventTypes;
    private final String prefix;
    private final OutboxProcessingGroup prefixGroup;

    public OutboxEventRoutingPolicy(
        Set<OutboxProcessingGroup> groups,
        OutboxProcessingGroup defaultGroup,
        List<Rule> rules,
        String prefix,
        OutboxProcessingGroup prefixGroup
    ) {
        this.groups = Set.copyOf(groups);
        this.defaultGroup = Objects.requireNonNull(defaultGroup);
        this.prefix = prefix;
        this.prefixGroup = Objects.requireNonNull(prefixGroup);
        if (prefix == null || prefix.isBlank() || !prefix.equals(prefix.strip())
            || prefix.contains("%") || prefix.contains("_") || prefix.contains("*")) {
            throw new IllegalArgumentException("접두사는 공백이나 와일드카드 없는 값이어야 합니다.");
        }
        if (defaultGroup.equals(prefixGroup)) {
            throw new IllegalArgumentException("접두사 그룹과 기본 그룹은 달라야 합니다.");
        }
        if (!this.groups.contains(defaultGroup) || !this.groups.contains(prefixGroup)) {
            throw new IllegalArgumentException("기본 처리 그룹과 접두사 처리 그룹이 등록되어 있어야 합니다.");
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

    // 명시적인 타입 분류를 우선하고 나머지는 설정된 접두사와 기본 그룹으로 분류한다.
    public OutboxProcessingGroup resolve(String eventType) {
        validateEventType(eventType);
        OutboxProcessingGroup exact = eventTypes.get(eventType);
        if (exact != null) {
            return exact;
        }
        return eventType.startsWith(prefix) ? prefixGroup : defaultGroup;
    }

    public OutboxEventSelection selectionFor(OutboxProcessingGroup group) {
        if (!groups.contains(group)) {
            throw new IllegalArgumentException("등록되지 않은 처리 그룹입니다.");
        }
        Set<String> included = eventTypes.entrySet().stream()
            .filter(entry -> entry.getValue().equals(group))
            .map(Map.Entry::getKey)
            .collect(Collectors.toUnmodifiableSet());
        PrefixMatch match = group.equals(prefixGroup) ? PrefixMatch.MATCH
            : group.equals(defaultGroup) ? PrefixMatch.NOT_MATCH : PrefixMatch.NONE;
        return new OutboxEventSelection(included, eventTypes.keySet(), prefix, match);
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
