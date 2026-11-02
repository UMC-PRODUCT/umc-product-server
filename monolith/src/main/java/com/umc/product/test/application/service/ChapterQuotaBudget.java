package com.umc.product.test.application.service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand.Entry;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.Column;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.PartSummary;

/**
 * 한 지부에서 시나리오 시딩이 더 걸 수 있는 파트별 TO.
 * <p>
 * 지부 TO 상한은 승인 대기·공개 프로젝트의 파트별 TO 합계가 지부의 활동 중인 파트 인원을 넘지 않는 것이다.
 * 시딩 시작 시점의 여유분에서 출발해 프로젝트마다 고른 TO 만큼 줄여 나가므로, 시딩이 만든 프로젝트를 모두
 * 공개해도 상한을 넘지 않는다. PLAN 은 TO 를 직접 걸지 않지만 PO 1명이 PLAN 1자리를 차지한다.
 */
final class ChapterQuotaBudget {

    private final Map<ChallengerPart, Long> remaining;

    private ChapterQuotaBudget(Map<ChallengerPart, Long> remaining) {
        this.remaining = remaining;
    }

    /**
     * TO 관리 화면과 같은 집계에서 여유분을 만든다. TO 를 바꿀 수 없는 파트(매칭이 시작된 지부 등)는 제외한다.
     */
    static ChapterQuotaBudget from(ProjectQuotaManagementInfo info) {
        Set<ChallengerPart> editable = info.columns().stream()
            .filter(column -> column.canEdit().allowed())
            .map(Column::part)
            .collect(Collectors.toSet());
        Map<ChallengerPart, Long> remaining = new EnumMap<>(ChallengerPart.class);
        for (PartSummary summary : info.summary()) {
            if (summary.part() == ChallengerPart.PLAN || editable.contains(summary.part())) {
                remaining.put(summary.part(), Math.max(0L, summary.availableCount() - summary.allocatedQuota()));
            }
        }
        return new ChapterQuotaBudget(remaining);
    }

    /**
     * 프로젝트 하나에 걸 TO 를 고르고 여유분에서 뺀다. 고를 수 없으면 여유분을 바꾸지 않는다.
     */
    Allocation allocate(RandomGenerator random) {
        if (remainingOf(ChallengerPart.PLAN) < 1) {
            return new Allocation.Exhausted("지부에 PO 로 쓸 PLAN 여유 인원이 없습니다.");
        }
        List<Entry> entries = new ArrayList<>();
        for (ScenarioPartGroup group : ScenarioPartGroup.values()) {
            List<ChallengerPart> candidates = group.getParts().stream()
                .filter(part -> remainingOf(part) > 0)
                .toList();
            if (candidates.isEmpty()) {
                continue;
            }
            ChallengerPart part = candidates.get(random.nextInt(candidates.size()));
            long quota = Math.min(random.nextLong(group.getMinQuota(), group.getMaxQuota() + 1), remainingOf(part));
            entries.add(Entry.builder().part(part).quota(quota).build());
        }
        if (entries.isEmpty()) {
            return new Allocation.Exhausted("지부에 TO 를 걸 수 있는 파트 여유 인원이 없습니다.");
        }
        remaining.merge(ChallengerPart.PLAN, -1L, Long::sum);
        entries.forEach(entry -> remaining.merge(entry.part(), -entry.quota(), Long::sum));
        return new Allocation.Allocated(List.copyOf(entries));
    }

    private long remainingOf(ChallengerPart part) {
        return remaining.getOrDefault(part, 0L);
    }

    sealed interface Allocation {

        record Allocated(List<Entry> entries) implements Allocation {
        }

        record Exhausted(String reason) implements Allocation {
        }
    }
}
