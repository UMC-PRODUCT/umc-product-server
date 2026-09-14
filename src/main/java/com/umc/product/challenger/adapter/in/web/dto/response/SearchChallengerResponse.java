package com.umc.product.challenger.adapter.in.web.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerItemInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerResult;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerTrack;
import com.umc.product.global.response.PageResponse;

import lombok.Builder;

@Builder
public record SearchChallengerResponse(
    PageResponse<SearchChallengerItemResponse> page,
    List<PartCountResponse> partCounts,
    List<TrackCountResponse> trackCounts
) {
    public static SearchChallengerResponse from(SearchChallengerResult result) {
        return new SearchChallengerResponse(
            PageResponse.of(result.page(), SearchChallengerItemResponse::from),
            PartCountResponse.from(result.partCounts()),
            TrackCountResponse.from(result.trackCounts())
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
        List<ChallengerTrack> tracks,
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
                .tracks(info.tracks())
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

    /**
     * TRACK 학습 기수용 트랙별 인원 수. sortOrder 순으로 항상 5개 트랙을 노출한다.
     */
    public record TrackCountResponse(
        ChallengerTrack track,
        long count
    ) {
        private static final List<ChallengerTrack> ORDER = List.of(
            ChallengerTrack.PLAN,
            ChallengerTrack.DESIGN,
            ChallengerTrack.WEB_PRODUCT_ENGINEER,
            ChallengerTrack.MOBILE_PRODUCT_ENGINEER,
            ChallengerTrack.INFRA_PLUS
        );

        public static List<TrackCountResponse> from(Map<ChallengerTrack, Long> counts) {
            List<TrackCountResponse> responses = new ArrayList<>(ORDER.size());
            for (ChallengerTrack track : ORDER) {
                responses.add(new TrackCountResponse(track, counts.getOrDefault(track, 0L)));
            }
            return responses;
        }
    }
}
