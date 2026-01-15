package com.umc.product.recruitment.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag;
import com.umc.product.recruitment.adapter.in.web.dto.response.ActiveRecruitmentIdResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentNoticeResponse;
import com.umc.product.recruitment.application.port.in.query.GetActiveRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentNoticeUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.ActiveRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetActiveRecruitmentQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentNoticeQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recruitments")
@RequiredArgsConstructor
@Tag(name = SwaggerTag.Constants.RECRUITMENT)
public class RecruitmentController {

    private final GetActiveRecruitmentUseCase getActiveRecruitmentUseCase;
    private final GetRecruitmentNoticeUseCase getRecruitmentNoticeUseCase;

    @GetMapping("/active-id")
    @Operation(summary = "현재 모집 중인 모집 ID 조회", description = "memberId 기준으로 현재 모집 중인 recruitmentId를 조회합니다. (사용자의 학교, active 기수 기반). 현재 임시로 memberId를 파라미터로 받으며, 실 동작은 토큰 기반으로 동작 예정.")
    public ActiveRecruitmentIdResponse getActiveRecruitmentId(
            // TODO: @CurrentMember(Long memberId) ArgumentResolver 적용 후 교체 예정
            @RequestParam Long memberId
    ) {
        GetActiveRecruitmentQuery query = new GetActiveRecruitmentQuery(memberId);
        ActiveRecruitmentInfo info = getActiveRecruitmentUseCase.get(query);
        return ActiveRecruitmentIdResponse.of(info.recruitmentId());
    }

    @GetMapping("/{recruitmentId}/notice")
    @Operation(summary = "모집 공지 조회", description = "모집 안내 화면 상단에 표시할 모집 공지(모집 상세) 정보를 조회합니다.")
    public RecruitmentNoticeResponse getRecruitmentNotice(
            @PathVariable Long recruitmentId
    ) {
        GetRecruitmentNoticeQuery query = new GetRecruitmentNoticeQuery(recruitmentId);
        return RecruitmentNoticeResponse.from(getRecruitmentNoticeUseCase.get(query));
    }

}
