package com.umc.product.member.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.global.response.PageResponse;
import com.umc.product.member.application.port.in.query.dto.SearchMemberItemInfo;
import com.umc.product.member.application.port.in.query.dto.SearchMemberResult;
import java.util.List;

public record SearchMemberResponse(
    long totalCount,
    PageResponse<SearchMemberItemResponse> page
) {
    public static SearchMemberResponse from(SearchMemberResult result) {
        PageResponse<SearchMemberItemResponse> pageResponse =
            PageResponse.of(result.page(), SearchMemberItemResponse::from);
        return new SearchMemberResponse(
            result.page().getTotalElements(),
            pageResponse
        );
    }

    public record SearchMemberItemResponse(
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
        List<ChallengerRoleType> roleTypes
    ) {
        public static SearchMemberItemResponse from(SearchMemberItemInfo info) {
            return new SearchMemberItemResponse(
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
                info.roleTypes()
            );
        }
    }
}
