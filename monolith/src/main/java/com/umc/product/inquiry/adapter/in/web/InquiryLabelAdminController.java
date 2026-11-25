package com.umc.product.inquiry.adapter.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.inquiry.adapter.in.web.dto.request.SaveInquiryLabelRequest;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryLabelResponse;
import com.umc.product.inquiry.application.port.in.command.ManageInquiryLabelUseCase;
import com.umc.product.inquiry.application.port.in.query.ListInquiryLabelsUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inquiries/admin/labels")
@RequiredArgsConstructor
@Validated
@Tag(name = "Inquiry | Admin", description = "UMC PRODUCT 운영진 문의 관리 API")
public class InquiryLabelAdminController {

    private final ListInquiryLabelsUseCase listInquiryLabelsUseCase;
    private final ManageInquiryLabelUseCase manageInquiryLabelUseCase;

    @GetMapping
    @Operation(operationId = "INQUIRY-ADMIN-105", summary = "문의 라벨 목록 조회", description = "이름순으로 돌려준다.")
    public List<InquiryLabelResponse> list(@CurrentMember MemberPrincipal principal) {
        return listInquiryLabelsUseCase.listLabels(principal.getMemberId()).stream()
            .map(InquiryLabelResponse::from)
            .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "INQUIRY-ADMIN-006", summary = "문의 라벨 만들기", description = "이름이 겹치면 409다.")
    public InquiryLabelResponse create(
        @Valid @RequestBody SaveInquiryLabelRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        return InquiryLabelResponse.from(manageInquiryLabelUseCase.create(principal.getMemberId(), request.name()));
    }

    @PatchMapping("/{labelId:\\d+}")
    @Operation(operationId = "INQUIRY-ADMIN-007", summary = "문의 라벨 이름 바꾸기", description = "이름이 겹치면 409다.")
    public InquiryLabelResponse rename(
        @PathVariable @Positive Long labelId,
        @Valid @RequestBody SaveInquiryLabelRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        return InquiryLabelResponse.from(
            manageInquiryLabelUseCase.rename(principal.getMemberId(), labelId, request.name())
        );
    }

    @DeleteMapping("/{labelId:\\d+}")
    @Operation(operationId = "INQUIRY-ADMIN-008", summary = "문의 라벨 지우기", description = "붙어 있던 문의에서도 함께 빠진다.")
    public void delete(
        @PathVariable @Positive Long labelId,
        @CurrentMember MemberPrincipal principal
    ) {
        manageInquiryLabelUseCase.delete(principal.getMemberId(), labelId);
    }
}
