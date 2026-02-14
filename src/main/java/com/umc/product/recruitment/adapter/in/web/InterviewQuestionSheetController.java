package com.umc.product.recruitment.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.recruitment.adapter.in.web.dto.request.CreateInterviewSheetQuestionRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.ReorderInterviewSheetQuestionRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpdateInterviewSheetQuestionRequest;
import com.umc.product.recruitment.adapter.in.web.dto.response.CreateInterviewSheetQuestionResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.GetInterviewSheetPartsResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.GetInterviewSheetQuestionsResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.ReorderInterviewSheetQuestionResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.UpdateInterviewSheetQuestionResponse;
import com.umc.product.recruitment.application.port.in.command.CreateInterviewSheetQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteInterviewSheetQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.ReorderInterviewSheetQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdateInterviewSheetQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteInterviewSheetQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.ReorderInterviewSheetQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateInterviewSheetQuestionCommand;
import com.umc.product.recruitment.application.port.in.query.GetInterviewSheetPartsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewSheetQuestionsUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetPartsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetQuestionsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetQuestionsQuery;
import com.umc.product.recruitment.domain.enums.PartKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recruitments/{recruitmentId}/interview-sheets")
@RequiredArgsConstructor
@Tag(name = "Recruitment | 면접 질문지 관리", description = "")
public class InterviewQuestionSheetController {


    private final GetInterviewSheetQuestionsUseCase getInterviewSheetQuestionsUseCase;
    private final GetInterviewSheetPartsUseCase getInterviewSheetPartsUseCase;

    private final CreateInterviewSheetQuestionUseCase createInterviewSheetQuestionUseCase;
    private final UpdateInterviewSheetQuestionUseCase updateInterviewSheetQuestionUseCase;
    private final DeleteInterviewSheetQuestionUseCase deleteInterviewSheetQuestionUseCase;
    private final ReorderInterviewSheetQuestionUseCase reorderInterviewSheetQuestionUseCase;

    @GetMapping("/questions")
    @Operation(
        summary = "면접 질문지(사전 질문) 조회",
        description = """
            특정 모집의 사전 면접 질문지를 조회합니다.
            partKey가 없으면 COMMON을 기본으로 조회합니다.
            """
    )
    public GetInterviewSheetQuestionsResponse getQuestions(
        @PathVariable Long recruitmentId,
        @RequestParam(required = false) PartKey part,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        GetInterviewSheetQuestionsInfo info = getInterviewSheetQuestionsUseCase.get(
            new GetInterviewSheetQuestionsQuery(
                recruitmentId,
                part,
                memberPrincipal.getMemberId()
            )
        );
        return GetInterviewSheetQuestionsResponse.from(info);
    }

    @GetMapping("/parts")
    @Operation(
        summary = "면접 질문지 작성용 드롭다운 옵션 조회(파트)",
        description = "해당 모집에서 모집하는 파트 목록 + 공통(COMMON)을 내려줍니다."
    )
    public GetInterviewSheetPartsResponse getParts(
        @PathVariable Long recruitmentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var info = getInterviewSheetPartsUseCase.get(
            new GetInterviewSheetPartsQuery(recruitmentId, memberPrincipal.getMemberId())
        );
        return GetInterviewSheetPartsResponse.from(info);
    }

    @PostMapping("/questions")
    @Operation(
        summary = "면접 질문지에 질문 등록",
        description = "현재 선택한 파트(COMMON/파트)에 사전 질문을 등록합니다."
    )
    public CreateInterviewSheetQuestionResponse createQuestion(
        @PathVariable Long recruitmentId,
        @RequestBody @Valid CreateInterviewSheetQuestionRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var result = createInterviewSheetQuestionUseCase.create(
            request.toCommand(recruitmentId, memberPrincipal.getMemberId())
        );
        return CreateInterviewSheetQuestionResponse.from(result);
    }

    @PatchMapping("/questions/{questionId}")
    @Operation(
        summary = "면접 질문지에 등록된 질문 수정",
        description = "사전 면접 질문의 내용을 수정합니다."
    )
    public UpdateInterviewSheetQuestionResponse updateQuestion(
        @PathVariable Long recruitmentId,
        @PathVariable Long questionId,
        @RequestBody @Valid UpdateInterviewSheetQuestionRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var result = updateInterviewSheetQuestionUseCase.update(
            new UpdateInterviewSheetQuestionCommand(
                recruitmentId,
                questionId,
                request.questionText(),
                memberPrincipal.getMemberId()
            )
        );
        return UpdateInterviewSheetQuestionResponse.from(result);
    }

    @DeleteMapping("/questions/{questionId}")
    @Operation(
        summary = "면접 질문지에 등록된 질문 삭제",
        description = "사전 면접 질문을 삭제합니다."
    )
    public void deleteQuestion(
        @PathVariable Long recruitmentId,
        @PathVariable Long questionId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        deleteInterviewSheetQuestionUseCase.delete(
            new DeleteInterviewSheetQuestionCommand(
                recruitmentId,
                questionId,
                memberPrincipal.getMemberId()
            )
        );
    }

    @PatchMapping("/questions/reorder")
    @Operation(
        summary = "면접 질문지 질문 순서 변경",
        description = """
            특정 파트(COMMON/파트)의 질문 순서를 변경합니다.
            orderedQuestionIds는 해당 파트의 전체 질문을 포함해야 합니다.
            """
    )
    public ReorderInterviewSheetQuestionResponse reorderQuestions(
        @PathVariable Long recruitmentId,
        @RequestBody @Valid ReorderInterviewSheetQuestionRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var result = reorderInterviewSheetQuestionUseCase.reorder(
            new ReorderInterviewSheetQuestionCommand(
                recruitmentId,
                request.partKey(),
                request.orderedQuestionIds(),
                memberPrincipal.getMemberId()
            )
        );
        return ReorderInterviewSheetQuestionResponse.from(result);
    }
}
