package com.umc.product.inquiry.adapter.in.web;

import java.util.List;
import java.util.Set;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.inquiry.adapter.in.web.dto.request.ChangeInquiryAssigneeRequest;
import com.umc.product.inquiry.adapter.in.web.dto.request.ChangeInquiryTeamRequest;
import com.umc.product.inquiry.adapter.in.web.dto.request.ReplaceInquiryLabelsRequest;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryStaffDetailResponse;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryStaffMemberResponse;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryStaffPageResponse;
import com.umc.product.inquiry.application.port.in.command.ChangeInquiryAssigneeUseCase;
import com.umc.product.inquiry.application.port.in.command.ChangeInquiryTeamUseCase;
import com.umc.product.inquiry.application.port.in.command.ReplaceInquiryLabelsUseCase;
import com.umc.product.inquiry.application.port.in.query.GetInquiryStaffDetailUseCase;
import com.umc.product.inquiry.application.port.in.query.ListInquiryAssigneeCandidatesUseCase;
import com.umc.product.inquiry.application.port.in.query.SearchStaffInquiriesUseCase;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailQuery;
import com.umc.product.inquiry.application.port.in.query.dto.StaffInquiriesQuery;
import com.umc.product.inquiry.domain.enums.InquiryStaffTab;
import com.umc.product.inquiry.domain.enums.InquiryStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inquiries/admin")
@RequiredArgsConstructor
@Validated
@Tag(name = "Inquiry | Admin", description = "UMC PRODUCT 운영진 문의 관리 API")
public class InquiryStaffController {

    private final SearchStaffInquiriesUseCase searchStaffInquiriesUseCase;
    private final GetInquiryStaffDetailUseCase getInquiryStaffDetailUseCase;
    private final ListInquiryAssigneeCandidatesUseCase listInquiryAssigneeCandidatesUseCase;
    private final ChangeInquiryAssigneeUseCase changeInquiryAssigneeUseCase;
    private final ChangeInquiryTeamUseCase changeInquiryTeamUseCase;
    private final ReplaceInquiryLabelsUseCase replaceInquiryLabelsUseCase;

    @GetMapping
    @Operation(
        operationId = "INQUIRY-ADMIN-102",
        summary = "운영 문의 목록 조회",
        description = """
            탭(전체·내 담당·미배정)과 탭별 개수를 함께 돌려준다. 탭 말고 다른 필터는 개수에도 똑같이 적용한다.
            status·categoryId는 그중 하나에 해당하면, labelId는 그중 하나라도 붙어 있으면 고른다. 여러 번 넘길 수 있다.
            담당자가 UMC_PRODUCT 권한을 잃은 문의는 미배정으로 본다.
            마지막 메시지 최신순이며 cursor는 이전 응답의 nextCursor를 그대로 넘긴다.
            """
    )
    public InquiryStaffPageResponse search(
        @RequestParam(defaultValue = "ALL") InquiryStaffTab tab,
        @RequestParam(name = "status", required = false) Set<InquiryStatus> statuses,
        @RequestParam(name = "categoryId", required = false) Set<@Positive Long> categoryIds,
        @RequestParam(name = "labelId", required = false) Set<@Positive Long> labelIds,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
        @CurrentMember MemberPrincipal principal
    ) {
        return InquiryStaffPageResponse.from(searchStaffInquiriesUseCase.search(new StaffInquiriesQuery(
            principal.getMemberId(), tab, statuses, categoryIds, labelIds, cursor, size
        )));
    }

    @GetMapping("/{inquiryId:\\d+}")
    @Operation(
        operationId = "INQUIRY-ADMIN-103",
        summary = "운영 문의 상세 조회",
        description = "질문자용 상세에 담당자, 라벨, 처리 이력을 더해 돌려준다."
    )
    public InquiryStaffDetailResponse getDetail(
        @PathVariable @Positive Long inquiryId,
        @CurrentMember MemberPrincipal principal
    ) {
        return staffDetail(inquiryId, principal);
    }

    @GetMapping("/assignee-candidates")
    @Operation(
        operationId = "INQUIRY-ADMIN-104",
        summary = "담당자 후보 목록 조회",
        description = "UMC_PRODUCT 역할을 직접 가진 회원 전원을 이름순으로 돌려준다. SUPER_ADMIN만 가진 회원은 없다."
    )
    public List<InquiryStaffMemberResponse> listAssigneeCandidates(@CurrentMember MemberPrincipal principal) {
        return listInquiryAssigneeCandidatesUseCase.listCandidates(principal.getMemberId()).stream()
            .map(InquiryStaffMemberResponse::from)
            .toList();
    }

    @PutMapping("/{inquiryId:\\d+}/assignee")
    @Operation(
        operationId = "INQUIRY-ADMIN-003",
        summary = "담당자 지정·변경·해제",
        description = """
            expectedAssigneeMemberId에는 화면에서 본 지금 담당자를 넣는다. 그사이 담당자가 바뀌었으면 409 INQUIRY-0015다.
            덮어쓰기는 없으니 상세를 다시 불러와 재시도한다. 내가 맡을 때는 assigneeMemberId에 내 회원 ID를 넣는다.
            다른 사람을 지정하면 지정된 사람에게 푸시가 간다.
            """
    )
    public InquiryStaffDetailResponse changeAssignee(
        @PathVariable @Positive Long inquiryId,
        @Valid @RequestBody ChangeInquiryAssigneeRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        changeInquiryAssigneeUseCase.changeAssignee(request.toCommand(inquiryId, principal.getMemberId()));
        return staffDetail(inquiryId, principal);
    }

    @PutMapping("/{inquiryId:\\d+}/team")
    @Operation(
        operationId = "INQUIRY-ADMIN-004",
        summary = "받는 팀 변경",
        description = "질문자 화면에도 바뀐 팀이 보인다. 변경은 처리 이력에 남는다."
    )
    public InquiryStaffDetailResponse changeTeam(
        @PathVariable @Positive Long inquiryId,
        @Valid @RequestBody ChangeInquiryTeamRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        changeInquiryTeamUseCase.changeTeam(request.toCommand(inquiryId, principal.getMemberId()));
        return staffDetail(inquiryId, principal);
    }

    @PutMapping("/{inquiryId:\\d+}/labels")
    @Operation(
        operationId = "INQUIRY-ADMIN-005",
        summary = "문의 라벨 적용",
        description = "문의의 라벨을 보낸 목록으로 통째로 바꾼다. 질문자에게는 라벨이 보이지 않는다."
    )
    public InquiryStaffDetailResponse replaceLabels(
        @PathVariable @Positive Long inquiryId,
        @Valid @RequestBody ReplaceInquiryLabelsRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        replaceInquiryLabelsUseCase.replaceLabels(request.toCommand(inquiryId, principal.getMemberId()));
        return staffDetail(inquiryId, principal);
    }

    private InquiryStaffDetailResponse staffDetail(Long inquiryId, MemberPrincipal principal) {
        return InquiryStaffDetailResponse.from(getInquiryStaffDetailUseCase.getStaffDetail(
            new InquiryDetailQuery(inquiryId, principal.getMemberId())
        ));
    }
}
