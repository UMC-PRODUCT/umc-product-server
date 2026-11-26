package com.umc.product.inquiry.adapter.in.web;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryFaqDetailResponse;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryFaqRankingResponse;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryFaqSuggestionResponse;
import com.umc.product.inquiry.adapter.in.web.dto.response.InquiryFaqSummaryResponse;
import com.umc.product.inquiry.application.port.in.command.MarkInquiryFaqResolvedUseCase;
import com.umc.product.inquiry.application.port.in.query.GetInquiryFaqRankingUseCase;
import com.umc.product.inquiry.application.port.in.query.GetInquiryFaqUseCase;
import com.umc.product.inquiry.domain.enums.InquiryFaqRankingPeriod;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inquiries/faqs")
@RequiredArgsConstructor
@Validated
@Tag(name = "Inquiry | FAQ", description = "로그인 사용자가 보는 대표 질문(FAQ) API")
public class InquiryFaqController {

    private final GetInquiryFaqUseCase getInquiryFaqUseCase;
    private final MarkInquiryFaqResolvedUseCase markInquiryFaqResolvedUseCase;
    private final GetInquiryFaqRankingUseCase getInquiryFaqRankingUseCase;

    @GetMapping
    @Operation(
        operationId = "INQUIRY-FAQ-101",
        summary = "대표 질문 목록 조회",
        description = "발행 중이고 종료일이 지나지 않은 대표 질문을 최근 발행 순으로 돌려준다. categoryId가 없으면 전체다."
    )
    public List<InquiryFaqSummaryResponse> list(@RequestParam(required = false) @Positive Long categoryId) {
        return getInquiryFaqUseCase.listPublic(categoryId).stream().map(InquiryFaqSummaryResponse::from).toList();
    }

    @GetMapping("/{faqId:\\d+}")
    @Operation(
        operationId = "INQUIRY-FAQ-102",
        summary = "대표 질문 상세 조회",
        description = "공개 중이 아니면 404다. 검증 조직은 늘 UMC PRODUCT, 수정일은 마지막 발행 시각이다."
    )
    public InquiryFaqDetailResponse get(@PathVariable @Positive Long faqId) {
        return InquiryFaqDetailResponse.from(getInquiryFaqUseCase.getPublic(faqId));
    }

    @GetMapping("/search")
    @Operation(
        operationId = "INQUIRY-FAQ-103",
        summary = "대표 질문 검색",
        description = "공개 중인 대표 질문의 질문·별칭·답변에 키워드가 들어 있으면 찾는다. 대소문자는 가리지 않고 최대 20개다."
    )
    public List<InquiryFaqSummaryResponse> search(@RequestParam @NotNull @Size(max = 100) String keyword) {
        return getInquiryFaqUseCase.search(keyword).stream().map(InquiryFaqSummaryResponse::from).toList();
    }

    @GetMapping("/suggestion")
    @Operation(
        operationId = "INQUIRY-FAQ-104",
        summary = "먼저 확인해 보세요",
        description = "문의 등록 화면에서 고른 분류의 대표 질문 하나. 최근 7일 가장 많이 물은 것, 없으면 가장 최근에 발행한 것이다."
    )
    public InquiryFaqSuggestionResponse suggest(@RequestParam @NotNull @Positive Long categoryId) {
        return new InquiryFaqSuggestionResponse(
            getInquiryFaqUseCase.suggest(categoryId).map(InquiryFaqSummaryResponse::from).orElse(null)
        );
    }

    @GetMapping("/rankings")
    @Operation(
        operationId = "INQUIRY-FAQ-105",
        summary = "대표 질문 인기 순위",
        description = """
            공개 중인 대표 질문 상위 5개. DAY는 최근 24시간, WEEK는 최근 7일 동안 그 대표 질문에 연결된 문의를 남긴
            서로 다른 회원 수(askedMemberCount)로 센다. 같은 사람이 여러 번 물어도 한 번이다. 기준은 문의 접수 시각이다.
            같은 수면 최근 문의가 더 최근인 쪽이 앞이다. 부를 때마다 지금 기준으로 계산하고 refreshedAt은 계산 시각이다.
            순위가 바뀌면 /topic/inquiries/ranking 으로 "새 순위 있음" 신호가 온다.
            """
    )
    public InquiryFaqRankingResponse rankings(@RequestParam(defaultValue = "DAY") InquiryFaqRankingPeriod period) {
        return InquiryFaqRankingResponse.from(getInquiryFaqRankingUseCase.getRanking(period));
    }

    @PostMapping("/{faqId:\\d+}/resolution")
    @Operation(
        operationId = "INQUIRY-FAQ-001",
        summary = "해결됐어요",
        description = "회원마다 대표 질문 하나에 한 번만 센다. 다시 보내도 결과가 같다. 수는 운영진에게만 보인다."
    )
    public void markResolved(
        @PathVariable @Positive Long faqId,
        @CurrentMember MemberPrincipal principal
    ) {
        markInquiryFaqResolvedUseCase.markResolved(principal.getMemberId(), faqId);
    }
}
