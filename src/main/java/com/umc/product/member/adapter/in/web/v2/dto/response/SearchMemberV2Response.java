package com.umc.product.member.adapter.in.web.v2.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.global.response.PageResponse;
import com.umc.product.global.util.EmailMasker;
import com.umc.product.member.application.port.in.query.dto.SearchMemberItemV2Info;
import com.umc.product.member.application.port.in.query.dto.SearchMemberItemV2Info.Participation;
import com.umc.product.member.application.port.in.query.dto.SearchMemberItemV2Info.PrimaryChallenger;
import com.umc.product.member.application.port.in.query.dto.SearchMemberV2Result;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * GET /api/v2/member/search 응답 DTO.
 * <p>
 * 회원 1명당 1개 항목으로 반환되며, 같은 회원이 여러 기수 챌린저 이력을 가져도 별도 row로 분리되지 않습니다.
 * 대표 챌린저(primaryChallenger)는 활성 기수 챌린저를 우선 선택하고, 없으면 가장 최신 기수의 챌린저로 선택됩니다.
 * 참여 기수 요약(participations)을 함께 제공해 검색 결과 식별을 돕습니다.
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
        @Schema(description = "대표 챌린저. 활성 기수 챌린저 우선, 없으면 최신 기수", nullable = true)
        PrimaryChallengerResponse primaryChallenger,
        @Schema(description = "이 회원이 현재 활성 기수에 운영진 ChallengerRole을 하나라도 보유하는지")
        boolean isAdminInActiveGisu,
        @Schema(description = "회원이 보유한 모든 챌린저 이력 요약 (최신 기수 우선)")
        List<ParticipationResponse> participations
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
                info.primaryChallenger() == null ? null : PrimaryChallengerResponse.from(info.primaryChallenger()),
                info.isAdminInActiveGisu(),
                info.participations().stream().map(ParticipationResponse::from).toList()
            );
        }

        public SearchMemberV2ItemResponse withMaskedEmail() {
            return new SearchMemberV2ItemResponse(
                memberId, name, nickname,
                EmailMasker.mask(email),
                schoolId, schoolName, profileImageLink,
                primaryChallenger, isAdminInActiveGisu, participations
            );
        }
    }

    public record PrimaryChallengerResponse(
        Long challengerId,
        Long gisuId,
        Long generation,
        ChallengerPart part,
        ChallengerStatus challengerStatus
    ) {
        public static PrimaryChallengerResponse from(PrimaryChallenger info) {
            return new PrimaryChallengerResponse(
                info.challengerId(),
                info.gisuId(),
                info.generation(),
                info.part(),
                info.challengerStatus()
            );
        }
    }

    public record ParticipationResponse(
        Long challengerId,
        Long gisuId,
        Long generation,
        ChallengerPart part,
        ChallengerStatus challengerStatus
    ) {
        public static ParticipationResponse from(Participation info) {
            return new ParticipationResponse(
                info.challengerId(),
                info.gisuId(),
                info.generation(),
                info.part(),
                info.challengerStatus()
            );
        }
    }
}
