package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;

/**
 * 프로젝트 매칭 소속 정보.
 *
 * @param originChapter   회원 학교 기준 지부. 학교가 지부에 배정되지 않았으면 null
 * @param matchingChapter 프로젝트 매칭에 쓰이는 지부. 이동이 없으면 원지부와 같고, 둘 다 없으면 null
 * @param transferred     매칭 지부 이동 여부
 */
public record ProjectMatchingAffiliationInfo(
    Long gisuId,
    ChapterInfo originChapter,
    ChapterInfo matchingChapter,
    boolean transferred
) {
}
