package com.umc.product.inquiry.adapter.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.inquiry.adapter.in.web.dto.request.CreateInquiryRequest;
import com.umc.product.inquiry.adapter.in.web.dto.request.SendInquiryMessageRequest;
import com.umc.product.inquiry.adapter.in.web.dto.response.CreateInquiryResponse;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryCategoryResponse;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryDetailResponse;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryMessagePageResponse;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryMessageResponse;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquirySummaryPageResponse;
import com.umc.product.inquiry.application.port.in.command.CreateInquiryUseCase;
import com.umc.product.inquiry.application.port.in.command.SendInquiryMessageUseCase;
import com.umc.product.inquiry.application.port.in.query.GetInquiryDetailUseCase;
import com.umc.product.inquiry.application.port.in.query.GetInquiryMessagesUseCase;
import com.umc.product.inquiry.application.port.in.query.ListInquiryCategoriesUseCase;
import com.umc.product.inquiry.application.port.in.query.ListMyInquiriesUseCase;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailQuery;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessagesQuery;
import com.umc.product.inquiry.application.port.in.query.dto.MyInquiriesQuery;
import com.umc.product.inquiry.domain.enums.InquiryStatusGroup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
@Validated
@Tag(name = "Inquiry", description = "UMC PRODUCT 팀 문의 접수와 대화 API")
public class InquiryController {

    private final ListInquiryCategoriesUseCase listInquiryCategoriesUseCase;
    private final CreateInquiryUseCase createInquiryUseCase;
    private final ListMyInquiriesUseCase listMyInquiriesUseCase;
    private final GetInquiryDetailUseCase getInquiryDetailUseCase;
    private final GetInquiryMessagesUseCase getInquiryMessagesUseCase;
    private final SendInquiryMessageUseCase sendInquiryMessageUseCase;

    @GetMapping("/categories")
    @Operation(operationId = "INQUIRY-101", summary = "문의 유형 목록 조회", description = "활성 유형을 표시 순서대로 돌려준다.")
    public List<InquiryCategoryResponse> listCategories() {
        return listInquiryCategoriesUseCase.listActive().stream()
            .map(InquiryCategoryResponse::from)
            .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        operationId = "INQUIRY-001",
        summary = "문의 등록",
        description = """
            문의를 등록하고 내용을 첫 메시지로 남긴다.
            같은 `clientRequestId`로 다시 요청하면 내용 비교 없이 기존 문의 ID를 돌려준다. 제목·본문이 달라도 새로 만들지 않는다.
            첨부파일은 storage 업로드 시 `category`에 `INQUIRY_ATTACHMENT`를 전달해 올린 파일만 쓸 수 있다.
            """
    )
    public CreateInquiryResponse create(
        @Valid @RequestBody CreateInquiryRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        return CreateInquiryResponse.from(createInquiryUseCase.create(request.toCommand(principal.getMemberId())));
    }

    @GetMapping("/me")
    @Operation(
        operationId = "INQUIRY-102",
        summary = "내 문의 목록 조회",
        description = "OPEN은 접수·진행 중, CLOSED는 종료된 문의다. 마지막 메시지 최신순이며 cursor는 이전 응답의 nextCursor를 그대로 넘긴다."
    )
    public InquirySummaryPageResponse listMine(
        @RequestParam(defaultValue = "OPEN") InquiryStatusGroup status,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
        @CurrentMember MemberPrincipal principal
    ) {
        return InquirySummaryPageResponse.from(listMyInquiriesUseCase.listMine(
            new MyInquiriesQuery(principal.getMemberId(), status, cursor, size)
        ));
    }

    @GetMapping("/{inquiryId:\\d+}")
    @Operation(operationId = "INQUIRY-103", summary = "문의 상세 조회", description = "작성자 또는 UMC PRODUCT 운영진만 볼 수 있다.")
    public InquiryDetailResponse getDetail(
        @PathVariable @Positive Long inquiryId,
        @CurrentMember MemberPrincipal principal
    ) {
        return InquiryDetailResponse.from(getInquiryDetailUseCase.getDetail(
            new InquiryDetailQuery(inquiryId, principal.getMemberId())
        ));
    }

    @GetMapping("/{inquiryId:\\d+}/messages")
    @Operation(
        operationId = "INQUIRY-104",
        summary = "문의 메시지 조회",
        description = "최신 메시지부터 돌려준다. 이전 메시지는 nextCursor를 cursor로 넘겨 이어서 조회한다. "
            + "보낸 사람은 누가 보든 실명으로 보인다."
    )
    public InquiryMessagePageResponse getMessages(
        @PathVariable @Positive Long inquiryId,
        @RequestParam(required = false) @Positive Long cursor,
        @RequestParam(defaultValue = "30") @Min(1) @Max(100) int size,
        @CurrentMember MemberPrincipal principal
    ) {
        return InquiryMessagePageResponse.from(getInquiryMessagesUseCase.getMessages(
            new InquiryMessagesQuery(inquiryId, principal.getMemberId(), cursor, size)
        ));
    }

    @PostMapping("/{inquiryId:\\d+}/messages")
    @Operation(
        operationId = "INQUIRY-002",
        summary = "문의 메시지 전송",
        description = """
            작성자 또는 UMC PRODUCT 운영진이 메시지를 보낸다. 종료된 문의에 보내면 다시 진행 중이 된다.
            같은 `clientMessageId`로 다시 보내면 메시지를 새로 만들지 않고 처음 결과를 돌려준다.
            """
    )
    public InquiryMessageResponse sendMessage(
        @PathVariable @Positive Long inquiryId,
        @Valid @RequestBody SendInquiryMessageRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        return InquiryMessageResponse.from(sendInquiryMessageUseCase.send(
            request.toCommand(inquiryId, principal.getMemberId())
        ));
    }
}
