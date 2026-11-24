package com.umc.product.inquiry.adapter.in.web;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryDetailResponse;
import com.umc.product.inquiry.application.port.in.command.CloseInquiryUseCase;
import com.umc.product.inquiry.application.port.in.command.dto.CloseInquiryCommand;
import com.umc.product.inquiry.application.port.in.query.GetInquiryDetailUseCase;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inquiries/admin")
@RequiredArgsConstructor
@Validated
@Tag(name = "Inquiry | Admin", description = "UMC PRODUCT 운영진 문의 관리 API")
public class InquiryAdminController {

    private final CloseInquiryUseCase closeInquiryUseCase;
    private final GetInquiryDetailUseCase getInquiryDetailUseCase;

    @PostMapping("/{inquiryId:\\d+}/close")
    @Operation(
        operationId = "INQUIRY-ADMIN-001",
        summary = "문의 종료",
        description = "UMC PRODUCT 운영진만 할 수 있다. 이미 종료된 문의면 409를 돌려준다."
    )
    public InquiryDetailResponse close(
        @PathVariable @Positive Long inquiryId,
        @CurrentMember MemberPrincipal principal
    ) {
        closeInquiryUseCase.close(new CloseInquiryCommand(inquiryId, principal.getMemberId()));
        return InquiryDetailResponse.from(getInquiryDetailUseCase.getDetail(
            new InquiryDetailQuery(inquiryId, principal.getMemberId())
        ));
    }
}
