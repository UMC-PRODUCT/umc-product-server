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
        summary = "서류 평가 가능한 모집 목록 조회",
        description = """
            운영진이 서류 평가를 진행(수정)할 수 있는 모집 목록을 조회합니다.
            서류 결과 발표(DOC_RESULT_AT) 이전 단계의 모집만 노출합니다.
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
