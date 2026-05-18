package com.umc.product.member.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.global.response.PageResponse;
import com.umc.product.global.util.EmailMasker;
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

    /**
     * 검색 결과의 이메일을 일괄 마스킹한 새 응답을 반환합니다. 검색은 본인 외의 회원이 결과로 포함되므로
     * 로그인 식별자인 이메일이 평문으로 노출되지 않도록 컨트롤러 단에서 호출해 적용합니다.
     */
    public SearchMemberResponse withMaskedEmails() {
        List<SearchMemberItemResponse> masked = page.content().stream()
            .map(SearchMemberItemResponse::withMaskedEmail)
            .toList();

        PageResponse<SearchMemberItemResponse> maskedPage = new PageResponse<>(
            masked,
            page.page(),
            page.size(),
            page.totalElements(),
            page.totalPages(),
            page.hasNext(),
            page.hasPrevious()
        );
        return new SearchMemberResponse(totalCount, maskedPage);
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
        Long gisu,
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
                info.gisu(),
                info.part(),
                info.roleTypes()
            );
        }

        public SearchMemberItemResponse withMaskedEmail() {
            return new SearchMemberItemResponse(
                memberId, name, nickname,
                EmailMasker.mask(email),
                schoolId, schoolName, profileImageLink,
                challengerId, gisuId, gisu, part, roleTypes
            );
        }
    }
}
