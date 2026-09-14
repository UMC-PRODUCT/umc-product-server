package com.umc.product.challenger.adapter.in.web.dto.response;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerItemInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerResult;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.global.response.PageResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record SearchChallengerResponse(
    PageResponse<SearchChallengerItemResponse> page,
    List<PartCountResponse> partCounts
) {
    public static SearchChallengerResponse from(SearchChallengerResult result) {
        return new SearchChallengerResponse(
            PageResponse.of(result.page(), SearchChallengerItemResponse::from),
            PartCountResponse.from(result.partCounts())
        );
    }

    @Builder
    public record SearchChallengerItemResponse(
        Long challengerId,
        Long memberId,
        Long gisuId,
        Long generation, // TODO: FE 적용 완료한거 보고 삭제하도록 함
        Long gisu,
        ChallengerPart part,
        String name,
        String nickname,
        String schoolName,
        Double pointSum,
        String profileImageLink,
        List<ChallengerRoleType> roleTypes
    ) {
        public static SearchChallengerItemResponse from(SearchChallengerItemInfo info) {
            return SearchChallengerItemResponse.builder()
                .challengerId(info.challengerId())
                .memberId(info.memberId())
                .gisuId(info.gisuId())
                .generation(info.generation())
                .gisu(info.generation())
                .part(info.part())
                .name(info.name())
                .nickname(info.nickname())
                .schoolName(info.schoolName())
                .pointSum(info.pointSum())
                .profileImageLink(info.profileImageLink())
                .roleTypes(info.roleTypes())
                .build();
        }
    }

    public record PartCountResponse(
        ChallengerPart part,
        long count
    ) {
        private static final List<ChallengerPart> ORDER = List.of(
            ChallengerPart.PLAN,
            ChallengerPart.DESIGN,
            ChallengerPart.WEB,
            ChallengerPart.ANDROID,
            ChallengerPart.IOS,
            ChallengerPart.NODEJS,
            ChallengerPart.SPRINGBOOT
        );

        public static List<PartCountResponse> from(Map<ChallengerPart, Long> counts) {
            List<PartCountResponse> responses = new ArrayList<>(ORDER.size());
            for (ChallengerPart part : ORDER) {
                responses.add(new PartCountResponse(part, counts.getOrDefault(part, 0L)));
            }
            return responses;
        }
    }
}
