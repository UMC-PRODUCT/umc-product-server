package com.umc.product.recruitment.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.recruitment.adapter.in.web.dto.DocumentEvaluationRecruitmentListResponse;
import com.umc.product.recruitment.application.port.in.query.GetDocumentEvaluationRecruitmentListUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.DocumentEvaluationRecruitmentListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetDocumentEvaluationRecruitmentListQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recruitments")
@RequiredArgsConstructor
@Tag(name = "Recruitment | 서류 평가", description = "")
public class DocumentEvaluationRecruitmentController {

    private final GetDocumentEvaluationRecruitmentListUseCase getDocumentEvaluationRecruitmentListUseCase;

    @GetMapping("/document-evaluations")
    @Operation(
        summary = "서류 평가 중 및 진행 중인 모집 목록 조회",
        description = """
            운영진이 서류 평가를 진행하거나 완료된 내역을 조회할 수 있는 모집 목록을 조회합니다.

            최종 결과 발표(FINAL_RESULT) 이전 단계의 모집을 모두 노출하며,
            서류 결과 발표(DOC_RESULT_AT) 시점을 기준으로 [평가 중]과 [평가 완료]로 구분하여 반환합니다.
            """
    )
    public DocumentEvaluationRecruitmentListResponse getRecruitments(
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        DocumentEvaluationRecruitmentListInfo info =
            getDocumentEvaluationRecruitmentListUseCase.get(
                new GetDocumentEvaluationRecruitmentListQuery(memberPrincipal.getMemberId())
            );

        return DocumentEvaluationRecruitmentListResponse.from(info);
    }

}
