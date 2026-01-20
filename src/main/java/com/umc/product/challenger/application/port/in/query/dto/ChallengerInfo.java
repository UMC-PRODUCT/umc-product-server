package com.umc.product.challenger.application.port.in.query.dto;

import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;
import lombok.Builder;


/**
 * 챌린저 공개 정보 조회용 DTO 입니다.
 * <p>
 * 사용자에 대한 정보는 member 도메인의 API를 이용해서 조회해야 합니다.
 *
 * @param challengerId     챌린저 ID
 * @param memberId         회원 ID
 * @param gisuId           기수 정보
 * @param part             챌린저 파트
 * @param challengerPoints 챌린저 상벌점 현황
 */
@Builder
public record ChallengerInfo(
        Long challengerId,
        Long memberId,
        Long gisuId,
        ChallengerPart part,
        List<ChallengerPointInfo> challengerPoints
) {

    public static ChallengerInfo from(Challenger challenger) {
        return ChallengerInfo.builder()
                .challengerId(challenger.getId())
                .memberId(challenger.getMemberId())
                .gisuId(challenger.getGisuId())
                .part(challenger.getPart())
                .build();

    }
}
