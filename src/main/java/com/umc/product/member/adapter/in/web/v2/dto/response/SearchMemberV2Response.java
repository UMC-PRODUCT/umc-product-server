package com.umc.product.member.adapter.in.web.v2.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.global.response.PageResponse;
import com.umc.product.global.util.EmailMasker;
import com.umc.product.member.application.port.in.query.dto.SearchMemberItemV2Info;
import com.umc.product.member.application.port.in.query.dto.SearchMemberV2Result;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * GET /api/v2/member/search 응답 DTO. v1 필드에 더해 챌린저 상태와 활성 기수 운영진 여부를 추가합니다.
 */
public record SearchMemberV2Response(
    long totalCount,
    PageResponse<SearchMemberV2ItemResponse> page
) {
    public static SearchMemberV2Response from(SearchMemberV2Result result) {
        PageResponse<SearchMemberV2ItemResponse> pageResponse =
            PageResponse.of(result.page(), SearchMemberV2ItemResponse::from);
        return new SearchMemberV2Response(
            result.page().getTotalElements(),
            pageResponse
        );
    }

    /**
     * 검색 결과의 이메일을 일괄 마스킹한 새 응답을 반환합니다. 검색은 본인 외의 회원이 결과로 포함되므로
     * 로그인 식별자인 이메일이 평문으로 노출되지 않도록 컨트롤러 단에서 호출해 적용합니다.
     */
    public SearchMemberV2Response withMaskedEmails() {
        List<SearchMemberV2ItemResponse> masked = page.content().stream()
            .map(SearchMemberV2ItemResponse::withMaskedEmail)
            .toList();

        PageResponse<SearchMemberV2ItemResponse> maskedPage = new PageResponse<>(
            masked,
            page.page(),
            page.size(),
            page.totalElements(),
            page.totalPages(),
            page.hasNext(),
            page.hasPrevious()
        );
        return new SearchMemberV2Response(totalCount, maskedPage);
    }

    public record SearchMemberV2ItemResponse(
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
        public static SearchMemberV2ItemResponse from(SearchMemberItemV2Info info) {
            return new SearchMemberV2ItemResponse(
                info.memberId(),
                info.name(),
                info.nickname(),
                info.email(),
                info.schoolId(),
                info.schoolName(),
                info.profileImageLink(),
                info.challengerId(),
                info.gisuId(),
                info.gisu(),
                info.part(),
                info.challengerStatus(),
                info.roleTypes(),
                info.isAdminInActiveGisu()
            );
        }

        public SearchMemberV2ItemResponse withMaskedEmail() {
            return new SearchMemberV2ItemResponse(
                memberId, name, nickname,
                EmailMasker.mask(email),
                schoolId, schoolName, profileImageLink,
                challengerId, gisuId, generation, part, challengerStatus, roleTypes, isAdminInActiveGisu
            );
        }
    }
}
