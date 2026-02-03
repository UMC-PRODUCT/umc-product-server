package com.umc.product.recruitment.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpdateFinalStatusRequest;
import com.umc.product.recruitment.adapter.in.web.dto.response.FinalSelectionApplicationListResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.UpdateFinalStatusResponse;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.SortOption;
import com.umc.product.recruitment.application.port.in.command.UpdateFinalStatusUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateFinalStatusCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateFinalStatusResult;
import com.umc.product.recruitment.application.port.in.query.GetFinalSelectionListUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.FinalSelectionApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetFinalSelectionApplicationListQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recruitments/{recruitmentId}")
@Tag(name = SwaggerTag.Constants.FINAL_SELECTION)
public class RecruitmentFinalSelectionController {

    private final UpdateFinalStatusUseCase updateFinalStatusUseCase;
    private final GetFinalSelectionListUseCase getFinalSelectionListUseCase;

    @PatchMapping("/applications/{applicationId}/final-status")
    @Operation(
            summary = "최종 선발 단건 합격/합격 취소",
            description = """
                    특정 지원서의 최종 합격 상태를 변경합니다.
                    - 합격 처리: status=PASS, selectedPart 필수
                    - 합격 취소: status=WAIT, selectedPart 미전송 또는 null
                    """
    )
    public UpdateFinalStatusResponse updateFinalStatus(
            @PathVariable Long recruitmentId,
            @PathVariable Long applicationId,
            @RequestBody @Valid UpdateFinalStatusRequest request,
            @CurrentMember MemberPrincipal memberPrincipal
    ) {
        UpdateFinalStatusResult result = updateFinalStatusUseCase.update(
                new UpdateFinalStatusCommand(
                        recruitmentId,
                        applicationId,
                        request.decision(),
                        request.selectedPart(),
                        memberPrincipal.getMemberId()
                )
        );
        return UpdateFinalStatusResponse.from(result);
    }

    @GetMapping("/final-selections")
    @Operation(
            summary = "최종 선발 리스트 조회",
            description = """
                    최종 선발(선정/미선정) 리스트를 조회합니다.
                    part 필터 및 정렬(sort)을 지원합니다.
                    """
    )
    public FinalSelectionApplicationListResponse getFinalSelections(
            @PathVariable Long recruitmentId,
            @RequestParam(required = false, defaultValue = "ALL") PartOption part,
            @RequestParam(required = false, defaultValue = "SCORE_DESC") SortOption sort,
            @CurrentMember MemberPrincipal memberPrincipal
    ) {
        FinalSelectionApplicationListInfo info = getFinalSelectionListUseCase.get(
                new GetFinalSelectionApplicationListQuery(recruitmentId, part, sort, memberPrincipal.getMemberId())
        );
        return FinalSelectionApplicationListResponse.from(info);
    }
}
