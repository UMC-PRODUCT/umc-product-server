package com.umc.product.member.adapter.out.external.challenger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.member.application.port.out.LoadBusinessCardActivityPort;
import com.umc.product.member.application.port.out.dto.BusinessCardActivity;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BusinessCardActivityAdapter implements LoadBusinessCardActivityPort {

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;

    @Override
    public List<BusinessCardActivity> listByMemberId(Long memberId) {
        // 명함에는 상벌점이 필요 없으므로 기본 정보 조회를 사용합니다.
        List<ChallengerBasicInfo> challengers = getChallengerUseCase.getAllBasicByMemberIds(Set.of(memberId))
            .getOrDefault(memberId, List.of());
        if (challengers.isEmpty()) {
            return List.of();
        }

        Set<Long> gisuIds = challengers.stream()
            .map(ChallengerBasicInfo::gisuId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, GisuInfo> gisuById = gisuIds.isEmpty()
            ? Map.of()
            : getGisuUseCase.getByIds(gisuIds).stream()
                .collect(Collectors.toMap(GisuInfo::gisuId, Function.identity()));

        return challengers.stream()
            .map(challenger -> {
                GisuInfo gisu = gisuById.get(challenger.gisuId());
                return new BusinessCardActivity(
                    challenger.gisuId(),
                    gisu == null ? null : gisu.generation(),
                    challenger.part()
                );
            })
            .toList();
    }
}
