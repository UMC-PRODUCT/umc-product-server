package com.umc.product.inquiry.adapter.in.web;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.inquiry.adapter.in.web.dto.request.UpdateInquiryStaffAlarmSettingRequest;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryDetailResponse;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryStaffAlarmSettingResponse;
import com.umc.product.inquiry.application.port.in.command.CloseInquiryUseCase;
import com.umc.product.inquiry.application.port.in.command.UpdateInquiryStaffAlarmSettingUseCase;
import com.umc.product.inquiry.application.port.in.command.dto.CloseInquiryCommand;
import com.umc.product.inquiry.application.port.in.query.GetInquiryDetailUseCase;
import com.umc.product.inquiry.application.port.in.query.GetInquiryStaffAlarmSettingUseCase;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    private final GetInquiryStaffAlarmSettingUseCase getInquiryStaffAlarmSettingUseCase;
    private final UpdateInquiryStaffAlarmSettingUseCase updateInquiryStaffAlarmSettingUseCase;

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

    @GetMapping("/alarm-setting")
    @Operation(
        operationId = "INQUIRY-ADMIN-101",
        summary = "내 문의 알림 설정 조회",
        description = "새 문의와 작성자 메시지 푸시를 받는지 돌려준다. 바꾼 적이 없으면 켜짐이다. "
            + "UMC_PRODUCT 역할이나 SUPER_ADMIN이 있어야 한다. UMC_PRODUCT 역할이 없는 SUPER_ADMIN은 이 설정과 상관없이 "
            + "운영진 알림을 받지 않는다."
    )
    public InquiryStaffAlarmSettingResponse getAlarmSetting(@CurrentMember MemberPrincipal principal) {
        return InquiryStaffAlarmSettingResponse.from(getInquiryStaffAlarmSettingUseCase.get(principal.getMemberId()));
    }

    @PutMapping("/alarm-setting")
    @Operation(
        operationId = "INQUIRY-ADMIN-002",
        summary = "내 문의 알림 설정 변경",
        description = "새 문의와 작성자 메시지 푸시를 켜거나 끈다. 같은 값을 다시 보내도 결과는 같다. "
            + "UMC_PRODUCT 역할이나 SUPER_ADMIN이 있어야 한다. UMC_PRODUCT 역할이 없는 SUPER_ADMIN은 이 설정과 상관없이 "
            + "운영진 알림을 받지 않는다."
    )
    public InquiryStaffAlarmSettingResponse updateAlarmSetting(
        @Valid @RequestBody UpdateInquiryStaffAlarmSettingRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        return InquiryStaffAlarmSettingResponse.from(
            updateInquiryStaffAlarmSettingUseCase.update(request.toCommand(principal.getMemberId()))
        );
    }
}
