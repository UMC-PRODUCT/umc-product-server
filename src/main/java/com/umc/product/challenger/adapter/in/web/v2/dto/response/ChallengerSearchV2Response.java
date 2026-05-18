package com.umc.product.challenger.adapter.in.web.v2.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.global.response.PageResponse;
import com.umc.product.global.util.EmailMasker;
import com.umc.product.member.application.port.in.query.dto.ChallengerSearchItemV2Info;
import com.umc.product.member.application.port.in.query.dto.ChallengerSearchV2Result;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * GET /api/v2/challenger/search 응답 DTO. 챌린저 단위 페이지네이션이며,
 * 같은 회원이 여러 기수 챌린저 이력을 가지면 별도 row로 분리되어 반환됩니다.
 */
public record ChallengerSearchV2Response(
    long totalCount,
    PageResponse<ChallengerSearchV2ItemResponse> page
) {
    public static ChallengerSearchV2Response from(ChallengerSearchV2Result result) {
        PageResponse<ChallengerSearchV2ItemResponse> pageResponse =
            PageResponse.of(result.page(), ChallengerSearchV2ItemResponse::from);
        return new ChallengerSearchV2Response(
            result.page().getTotalElements(),
            pageResponse
        );
    }

    /**
     * 검색 결과의 이메일을 일괄 마스킹한 새 응답을 반환합니다. 검색은 본인 외의 회원이 결과로 포함되므로
     * 로그인 식별자인 이메일이 평문으로 노출되지 않도록 컨트롤러 단에서 호출해 적용합니다.
     */
    public ChallengerSearchV2Response withMaskedEmails() {
        List<ChallengerSearchV2ItemResponse> masked = page.content().stream()
            .map(ChallengerSearchV2ItemResponse::withMaskedEmail)
            .toList();

        PageResponse<ChallengerSearchV2ItemResponse> maskedPage = new PageResponse<>(
            masked,
            page.page(),
            page.size(),
            page.totalElements(),
            page.totalPages(),
            page.hasNext(),
            page.hasPrevious()
        );
        return new ChallengerSearchV2Response(totalCount, maskedPage);
    }

    public record ChallengerSearchV2ItemResponse(
        Long memberId,
        String name,
        String nickname,
        String email,
        Long schoolId,
        String schoolName,
        String profileImageLink,
        Long challengerId,
        Long gisuId,
        Long generation,
        ChallengerPart part,
        @Schema(description = "검색 결과 행의 챌린저 상태")
        ChallengerStatus challengerStatus,
        List<ChallengerRoleType> roleTypes,
        @Schema(description = "이 회원이 현재 활성 기수에 운영진 ChallengerRole을 하나라도 보유하는지")
        boolean isAdminInActiveGisu
    ) {
        public static ChallengerSearchV2ItemResponse from(ChallengerSearchItemV2Info info) {
            return new ChallengerSearchV2ItemResponse(
                info.memberId(),
                info.name(),
                info.nickname(),
                info.email(),
                info.schoolId(),
                info.schoolName(),
                info.profileImageLink(),
                info.challengerId(),
                info.gisuId(),
                info.generation(),
                info.part(),
                info.challengerStatus(),
                info.roleTypes(),
                info.isAdminInActiveGisu()
            );
        }

        public ChallengerSearchV2ItemResponse withMaskedEmail() {
            return new ChallengerSearchV2ItemResponse(
                memberId, name, nickname,
                EmailMasker.mask(email),
                schoolId, schoolName, profileImageLink,
                challengerId, gisuId, generation, part, challengerStatus, roleTypes, isAdminInActiveGisu
            );
        }
    }
}
