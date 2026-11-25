package com.umc.product.inquiry.adapter.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.inquiry.adapter.in.web.dto.request.ArchiveInquiryFaqRequest;
import com.umc.product.inquiry.adapter.in.web.dto.request.CreateInquiryFaqRequest;
import com.umc.product.inquiry.adapter.in.web.dto.request.PublishInquiryFaqRequest;
import com.umc.product.inquiry.adapter.in.web.dto.request.SaveInquiryFaqDraftRequest;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryFaqAudienceResponse;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryFaqDemandResponse;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryFaqStaffDetailResponse;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryFaqStaffSummaryResponse;
import com.umc.product.inquiry.application.port.in.command.ManageInquiryFaqUseCase;
import com.umc.product.inquiry.application.port.in.query.GetInquiryFaqForStaffUseCase;
import com.umc.product.inquiry.application.port.in.query.GetInquiryFaqRankingUseCase;
import com.umc.product.inquiry.domain.enums.InquiryFaqStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inquiries/admin")
@RequiredArgsConstructor
@Validated
@Tag(name = "Inquiry | Admin FAQ", description = "UMC PRODUCT 운영진 대표 질문(FAQ) 관리 API")
public class InquiryFaqAdminController {

    private final GetInquiryFaqForStaffUseCase getInquiryFaqForStaffUseCase;
    private final ManageInquiryFaqUseCase manageInquiryFaqUseCase;
    private final GetInquiryFaqRankingUseCase getInquiryFaqRankingUseCase;

    @GetMapping("/faqs")
    @Operation(
        operationId = "INQUIRY-ADMIN-FAQ-101",
        summary = "대표 질문 목록 조회",
        description = "상태와 수정본 카테고리로 거르고 최근에 바뀐 것부터 돌려준다. 해결 수는 운영진에게만 보인다."
    )
    public List<InquiryFaqStaffSummaryResponse> list(
        @RequestParam(required = false) InquiryFaqStatus status,
        @RequestParam(required = false) @Positive Long categoryId,
        @CurrentMember MemberPrincipal principal
    ) {
        return getInquiryFaqForStaffUseCase.listForStaff(principal.getMemberId(), status, categoryId).stream()
            .map(InquiryFaqStaffSummaryResponse::from)
            .toList();
    }

    @GetMapping("/faqs/{faqId:\\d+}")
    @Operation(operationId = "INQUIRY-ADMIN-FAQ-102", summary = "대표 질문 상세 조회", description = "수정본과 발행본을 함께 돌려준다.")
    public InquiryFaqStaffDetailResponse get(
        @PathVariable @Positive Long faqId,
        @CurrentMember MemberPrincipal principal
    ) {
        return detail(principal, faqId);
    }

    @GetMapping("/faq-audiences")
    @Operation(operationId = "INQUIRY-ADMIN-FAQ-103", summary = "대표 질문 대상 목록 조회", description = "활성 대상을 표시 순서대로 돌려준다.")
    public List<InquiryFaqAudienceResponse> listAudiences(@CurrentMember MemberPrincipal principal) {
        return getInquiryFaqForStaffUseCase.listAudiences(principal.getMemberId()).stream()
            .map(InquiryFaqAudienceResponse::from)
            .toList();
    }

    @GetMapping("/faqs/answer-needed")
    @Operation(
        operationId = "INQUIRY-ADMIN-FAQ-104",
        summary = "답변 준비 필요",
        description = "아직 발행하지 않은 초안 중 최근 24시간 동안 연결된 문의를 남긴 회원이 많은 순으로 10개. 공개 순위에는 나오지 않는다."
    )
    public List<InquiryFaqDemandResponse> listAnswerNeeded(@CurrentMember MemberPrincipal principal) {
        return getInquiryFaqRankingUseCase.listAnswerNeeded(principal.getMemberId()).stream()
            .map(InquiryFaqDemandResponse::from)
            .toList();
    }

    @PostMapping("/faqs")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "INQUIRY-ADMIN-FAQ-001", summary = "대표 질문 초안 만들기", description = "질문만 있으면 초안으로 저장한다.")
    public InquiryFaqStaffDetailResponse create(
        @Valid @RequestBody CreateInquiryFaqRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        return detail(principal, manageInquiryFaqUseCase.create(request.toCommand(principal.getMemberId())));
    }

    @PutMapping("/faqs/{faqId:\\d+}/draft")
    @Operation(
        operationId = "INQUIRY-ADMIN-FAQ-002",
        summary = "대표 질문 초안 저장",
        description = """
            수정본을 보낸 내용으로 통째로 바꾼다. 발행 중이면 기존 발행본이 그대로 공개되고 hasUnpublishedChanges가 true가 된다.
            expectedVersion에는 화면에서 본 version을 넣는다. 그사이 다른 운영진이 고쳤으면 409 INQUIRY-0029이고 덮어쓰지 않으니,
            상세를 다시 불러와 재시도한다.
            """
    )
    public InquiryFaqStaffDetailResponse saveDraft(
        @PathVariable @Positive Long faqId,
        @Valid @RequestBody SaveInquiryFaqDraftRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        manageInquiryFaqUseCase.saveDraft(request.toCommand(principal.getMemberId(), faqId));
        return detail(principal, faqId);
    }

    @PostMapping("/faqs/{faqId:\\d+}/publish")
    @Operation(
        operationId = "INQUIRY-ADMIN-FAQ-003",
        summary = "대표 질문 검증 후 발행",
        description = """
            수정본을 발행본으로 만든다. 검증 체크 3개가 모두 true여야 하고, 답변·카테고리·대상이 있어야 한다.
            발행 직전에 카테고리·대상이 아직 쓰이는지 다시 확인한다.
            보관된 대표 질문도 다시 발행할 수 있다. UMC_PRODUCT 운영진 누구나 할 수 있다.
            expectedVersion이 지금 version과 다르면 409 INQUIRY-0029다.
            """
    )
    public InquiryFaqStaffDetailResponse publish(
        @PathVariable @Positive Long faqId,
        @Valid @RequestBody PublishInquiryFaqRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        manageInquiryFaqUseCase.publish(request.toCommand(principal.getMemberId(), faqId));
        return detail(principal, faqId);
    }

    @PostMapping("/faqs/{faqId:\\d+}/archive")
    @Operation(
        operationId = "INQUIRY-ADMIN-FAQ-004",
        summary = "대표 질문 보관",
        description = """
            발행 중인 대표 질문만 보관할 수 있다. 공개·검색·추천·순위에서 바로 빠지고 연결된 문의는 그대로다.
            expectedVersion이 지금 version과 다르면 409 INQUIRY-0029다.
            """
    )
    public InquiryFaqStaffDetailResponse archive(
        @PathVariable @Positive Long faqId,
        @Valid @RequestBody ArchiveInquiryFaqRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        manageInquiryFaqUseCase.archive(request.toCommand(principal.getMemberId(), faqId));
        return detail(principal, faqId);
    }

    private InquiryFaqStaffDetailResponse detail(MemberPrincipal principal, Long faqId) {
        return InquiryFaqStaffDetailResponse.from(
            getInquiryFaqForStaffUseCase.getForStaff(principal.getMemberId(), faqId)
        );
    }
}
