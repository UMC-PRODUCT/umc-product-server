package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand.Entry;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionCapabilityInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionReason;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.AllocationStatus;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.Column;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.PartSummary;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.Totals;
import com.umc.product.test.application.service.ChapterQuotaBudget.Allocation;
import com.umc.product.test.application.service.ChapterQuotaBudget.Allocation.Allocated;
import com.umc.product.test.application.service.ChapterQuotaBudget.Allocation.Exhausted;

class ChapterQuotaBudgetTest {

    @Test
    @DisplayName("여유가 바닥날 때까지 배정해도 파트별 TO 합계는 여유 인원을 넘지 않는다")
    void allocationsNeverExceedRemainingCapacity() {
        // given
        Map<ChallengerPart, Long> capacity = Map.of(
            ChallengerPart.PLAN, 100L, ChallengerPart.DESIGN, 3L,
            ChallengerPart.WEB_PRODUCT_ENGINEER, 7L, ChallengerPart.MOBILE_PRODUCT_ENGINEER, 5L);

        for (long seed = 0; seed < 50; seed++) {
            ChapterQuotaBudget budget = ChapterQuotaBudget.from(info(capacity, Set.of()));

            // when
            List<List<Entry>> allocations = allocateUntilExhausted(budget, new Random(seed));

            // then
            Map<ChallengerPart, Long> total = new EnumMap<>(ChallengerPart.class);
            allocations.stream().flatMap(List::stream)
                .forEach(entry -> total.merge(entry.part(), entry.quota(), Long::sum));
            assertThat(allocations).isNotEmpty();
            total.forEach((part, quota) -> assertThat(quota).isLessThanOrEqualTo(capacity.get(part)));
        }
    }

    @Test
    @DisplayName("한 프로젝트에 웹과 모바일 프로덕트 엔지니어 TO를 함께 걸지 않는다")
    void neverAllocatesBothProductEngineerParts() {
        // given
        ChapterQuotaBudget budget = ChapterQuotaBudget.from(info(Map.of(
            ChallengerPart.PLAN, 1_000L,
            ChallengerPart.WEB_PRODUCT_ENGINEER, 1_000L,
            ChallengerPart.MOBILE_PRODUCT_ENGINEER, 1_000L), Set.of()));
        Random random = new Random(7);
        Set<ChallengerPart> seen = new HashSet<>();

        for (int i = 0; i < 200; i++) {
            // when
            List<Entry> entries = allocated(budget.allocate(random));

            // then
            List<ChallengerPart> parts = entries.stream().map(Entry::part).toList();
            assertThat(parts.contains(ChallengerPart.WEB_PRODUCT_ENGINEER)
                && parts.contains(ChallengerPart.MOBILE_PRODUCT_ENGINEER)).isFalse();
            seen.addAll(parts);
        }
        assertThat(seen).contains(ChallengerPart.WEB_PRODUCT_ENGINEER, ChallengerPart.MOBILE_PRODUCT_ENGINEER);
    }

    @Test
    @DisplayName("여유 인원이 0 이하인 파트와 TO를 바꿀 수 없는 파트는 고르지 않는다")
    void skipsPartsWithoutRemainingOrEditableCapacity() {
        // given
        List<PartSummary> summary = List.of(
            summary(ChallengerPart.PLAN, 10, 0),
            summary(ChallengerPart.DESIGN, 2, 5),
            summary(ChallengerPart.WEB_PRODUCT_ENGINEER, 4, 4),
            summary(ChallengerPart.MOBILE_PRODUCT_ENGINEER, 20, 0),
            summary(ChallengerPart.SPRINGBOOT, 20, 0));
        ChapterQuotaBudget budget = ChapterQuotaBudget.from(
            info(summary, Set.of(ChallengerPart.SPRINGBOOT)));
        Random random = new Random(11);

        for (int i = 0; i < 5; i++) {
            // when
            List<Entry> entries = allocated(budget.allocate(random));

            // then
            assertThat(entries).extracting(Entry::part).containsExactly(ChallengerPart.MOBILE_PRODUCT_ENGINEER);
        }
    }

    @Test
    @DisplayName("파트 여유가 TO 범위 최솟값보다 작으면 남은 여유만큼만 TO를 건다")
    void capsQuotaAtRemainingCapacity() {
        // given
        ChapterQuotaBudget budget = ChapterQuotaBudget.from(info(Map.of(
            ChallengerPart.PLAN, 5L, ChallengerPart.MOBILE_PRODUCT_ENGINEER, 2L), Set.of()));

        // when
        List<Entry> entries = allocated(budget.allocate(new Random(3)));

        // then
        assertThat(entries).containsExactly(
            Entry.builder().part(ChallengerPart.MOBILE_PRODUCT_ENGINEER).quota(2L).build());
    }

    @Test
    @DisplayName("프로젝트마다 PLAN 1자리를 쓰고 PLAN 여유가 없으면 더 배정하지 않는다")
    void consumesOnePlanSeatPerProject() {
        // given
        ChapterQuotaBudget budget = ChapterQuotaBudget.from(info(Map.of(
            ChallengerPart.PLAN, 2L, ChallengerPart.DESIGN, 100L,
            ChallengerPart.WEB_PRODUCT_ENGINEER, 100L), Set.of()));
        Random random = new Random(5);

        // when
        Allocation first = budget.allocate(random);
        Allocation second = budget.allocate(random);
        Allocation third = budget.allocate(random);

        // then
        assertThat(first).isInstanceOf(Allocated.class);
        assertThat(second).isInstanceOf(Allocated.class);
        assertThat(third).isInstanceOf(Exhausted.class);
    }

    @Test
    @DisplayName("TO를 걸 파트가 하나도 없으면 PLAN 여유가 있어도 배정하지 않는다")
    void rejectsWhenNoPartHasCapacity() {
        // given
        ChapterQuotaBudget budget = ChapterQuotaBudget.from(info(Map.of(
            ChallengerPart.PLAN, 5L, ChallengerPart.DESIGN, 0L), Set.of()));

        // when
        Allocation allocation = budget.allocate(new Random(1));

        // then
        assertThat(allocation).isInstanceOf(Exhausted.class);
    }

    private static List<List<Entry>> allocateUntilExhausted(ChapterQuotaBudget budget, Random random) {
        List<List<Entry>> allocations = new ArrayList<>();
        while (budget.allocate(random) instanceof Allocated allocated) {
            allocations.add(allocated.entries());
        }
        return allocations;
    }

    private static List<Entry> allocated(Allocation allocation) {
        assertThat(allocation).isInstanceOf(Allocated.class);
        return ((Allocated) allocation).entries();
    }

    private static ProjectQuotaManagementInfo info(Map<ChallengerPart, Long> remaining, Set<ChallengerPart> locked) {
        return info(remaining.entrySet().stream()
            .map(entry -> summary(entry.getKey(), entry.getValue(), 0))
            .toList(), locked);
    }

    private static ProjectQuotaManagementInfo info(List<PartSummary> summary, Set<ChallengerPart> locked) {
        List<Column> columns = summary.stream()
            .map(part -> new Column(part.part(), part.part().name(), locked.contains(part.part())
                ? ProjectPermissionCapabilityInfo.denied(ProjectPermissionReason.PART_QUOTA_MATCHING_IN_PROGRESS)
                : ProjectPermissionCapabilityInfo.allow(), null))
            .toList();
        Totals totals = new Totals(0, 0, 0, AllocationStatus.BALANCED);
        return new ProjectQuotaManagementInfo(1L, 1L, "지부", List.of(), List.of(), columns,
            summary, summary, totals, totals, 0, 0, List.of());
    }

    private static PartSummary summary(ChallengerPart part, long available, long allocated) {
        return new PartSummary(part, available, allocated, allocated - available, AllocationStatus.BALANCED);
    }
}
