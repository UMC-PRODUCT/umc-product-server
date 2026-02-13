package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;

public record SearchChallengerCursorRequest(
        @Parameter(description = "이전 페이지의 마지막 챌린저 ID. 첫 페이지 조회 시 null")
        Long cursor,

        @Parameter(description = "한 페이지에 조회할 항목 수. 기본값 20, 최대 50")
        Integer size,

        @Parameter(description = "특정 챌린저 ID로 정확히 검색")
        Long challengerId,

        @Parameter(description = "이름으로 부분 검색")
        String name,

        @Parameter(description = "닉네임으로 부분 검색")
        String nickname,

        @Parameter(description = "소속 학교 ID로 필터링")
        Long schoolId,

        @Parameter(description = "소속 지부 ID로 필터링")
        Long chapterId,

        @Parameter(description = "파트별 필터링 (PLAN, DESIGN, WEB, ANDROID, IOS, NODEJS, SPRINGBOOT)")
        ChallengerPart part,

        @Parameter(description = "기수 ID로 필터링")
        Long gisuId
) {
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    public SearchChallengerQuery toQuery() {
        return new SearchChallengerQuery(
                challengerId,
                name,
                nickname,
                schoolId,
                chapterId,
                part,
                gisuId,
                List.of(ChallengerStatus.ACTIVE)
        );
    }

    public int getSize() {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
