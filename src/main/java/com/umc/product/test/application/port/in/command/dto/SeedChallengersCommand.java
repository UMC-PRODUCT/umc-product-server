package com.umc.product.test.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

/**
 * 챌린저 분포 시딩 Command. ADR-017 참조.
 *
 * @param gisuId                대상 기수 (null 이면 활성 기수)
 * @param countPerPartPerSchool 한 (Chapter, School, Part) 셀당 생성할 챌린저 수
 * @param parts                 시딩할 파트 목록 (null/empty 면 ADMIN 제외 전 파트)
 * @param chapterIds            대상 Chapter 목록 (null/empty 면 해당 기수 전체)
 */
public record SeedChallengersCommand(
    Long gisuId,
    int countPerPartPerSchool,
    List<ChallengerPart> parts,
    List<Long> chapterIds
) {
}
