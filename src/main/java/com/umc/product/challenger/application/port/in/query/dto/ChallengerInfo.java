package com.umc.product.challenger.application.port.in.query.dto;

import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import java.util.List;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;


/**
 * 챌린저 정보를 담고 있는 Info단 DTO 입니다.
 * <p>
 * 각 챌린저의 상벌점 현황을 포함하여 번환하며, 성능 상 해당 정보를 제외하고자 하는 경우 별도의 DTO를 생성해서 사용해주세요.
 */
@Builder
@Slf4j
public record ChallengerInfo(
    Long challengerId,
    Long memberId,
    Long gisuId,
    ChallengerPart part,
    List<ChallengerPointInfo> challengerPoints,
    Double totalPoints,
    ChallengerStatus challengerStatus
) {
    // 성능 상 상벌점 정보가 필요하지 않은 경우 별도의 DTO를 생성할 것.
    // 아직 만들지 않았어요.
    @Deprecated(since = "v1.5.0", forRemoval = true)
    public static ChallengerInfo from(Challenger challenger) {
        log.error("챌린저 상벌점을 포함하지 않는 생성자를 사용하고 있습니다.");

        return ChallengerInfo.builder()
            .challengerId(challenger.getId())
            .memberId(challenger.getMemberId())
            .gisuId(challenger.getGisuId())
            .part(challenger.getPart())
            .build();
    }

    public static ChallengerInfo from(Challenger challenger, List<ChallengerPointInfo> challengerPoints) {
        double totalPoints = challengerPoints.stream()
            .mapToDouble(ChallengerPointInfo::point)
            .sum();

        return ChallengerInfo.builder()
            .challengerId(challenger.getId())
            .memberId(challenger.getMemberId())
            .gisuId(challenger.getGisuId())
            .part(challenger.getPart())
            .challengerPoints(challengerPoints)
            .totalPoints(totalPoints)
            .challengerStatus(challenger.getStatus())
            .build();
    }
}
