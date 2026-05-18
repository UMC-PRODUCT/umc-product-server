package com.umc.product.curriculum.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;
import java.util.Set;

/**
 * 베스트 워크북 목록 조회 쿼리
 * <p>
 * 다중 선택 필터를 지원하며, 제공된 값들의 카르테시안 곱으로 결과를 반환합니다.
 *
 * @param gisuId       기수 ID (필수)
 * @param schoolIds    학교 ID 목록 (nullable: 미제공 시 전체)
 * @param parts        파트 목록 (nullable: 미제공 시 전체)
 * @param weekNos      주차 번호 목록 (nullable: 미제공 시 전체)
 * @param studyGroupIds 스터디 그룹 ID 목록 (nullable: 미제공 시 전체)
 * @param cursor       커서 ID (nullable: 첫 페이지 조회)
 * @param size         페이지 크기 (기본값: 20)
 */
public record GetBestWorkbooksQuery(
    Long gisuId,
    Set<Long> schoolIds,
    Set<ChallengerPart> parts,
    List<Long> weekNos,
    List<Long> studyGroupIds,
    Long cursor,
    int size
) {
    public GetBestWorkbooksQuery {
        if (size <= 0) size = 20;
    }
}