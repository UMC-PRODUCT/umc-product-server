package com.umc.product.challenger.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerActivityPeriodUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ActivityPeriodSummary;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengerActivityPeriodService implements GetChallengerActivityPeriodUseCase {

    private static final Set<ChallengerStatus> COUNTED_STATUSES =
        Set.of(ChallengerStatus.ACTIVE, ChallengerStatus.GRADUATED);

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;

    @Override
    public ActivityPeriodSummary getActivityPeriodByMemberId(Long memberId) {
        List<ChallengerInfo> challengers = getChallengerUseCase.getAllByMemberId(memberId);
        if (challengers.isEmpty()) {
            return ActivityPeriodSummary.empty();
        }

        Set<Long> gisuIds = challengers.stream()
            .map(ChallengerInfo::gisuId)
            .collect(Collectors.toSet());

        Map<Long, GisuInfo> gisuInfoByGisuId = getGisuUseCase.getByIds(gisuIds).stream()
            .collect(Collectors.toMap(GisuInfo::gisuId, g -> g));

        return calculateActivityPeriod(challengers, gisuInfoByGisuId);
    }

    @Override
    public ActivityPeriodSummary calculateActivityPeriod(
        List<ChallengerInfo> challengers,
        Map<Long, GisuInfo> gisuInfosByGisuId
    ) {
        if (challengers == null || challengers.isEmpty()) {
            return ActivityPeriodSummary.empty();
        }

        Instant now = Instant.now();
        Map<Long, ActivityPeriodSummary.PerGisu> perGisu = new LinkedHashMap<>();

        challengers.stream()
            .filter(c -> COUNTED_STATUSES.contains(c.challengerStatus()))
            .map(ChallengerInfo::gisuId)
            .distinct()
            .map(gisuInfosByGisuId::get)
            .filter(Objects::nonNull)
            .sorted((a, b) -> Long.compare(
                a.generation() == null ? 0L : a.generation(),
                b.generation() == null ? 0L : b.generation()))
            .forEach(gisu -> perGisu.put(
                gisu.gisuId(),
                new ActivityPeriodSummary.PerGisu(gisu.gisuId(), gisu.generation(), gisu.activityDays(now))
            ));

        return ActivityPeriodSummary.of(perGisu);
    }
}
