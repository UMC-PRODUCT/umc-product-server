package com.umc.product.inquiry.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.port.in.query.GetInquiryFaqRankingUseCase;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqDemandInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqRankingInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqSummaryInfo;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqPort.DemandTarget;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqPort.FaqDemand;
import com.umc.product.inquiry.domain.InquiryFaq;
import com.umc.product.inquiry.domain.enums.InquiryFaqRankingPeriod;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryFaqRankingQueryService implements GetInquiryFaqRankingUseCase {

    static final int RANKING_SIZE = 5;
    static final int ANSWER_NEEDED_SIZE = 10;

    private final LoadInquiryFaqPort loadInquiryFaqPort;
    private final InquiryFaqAssembler faqAssembler;
    private final InquiryAccessPolicy accessPolicy;
    private final Clock clock;

    @Override
    public InquiryFaqRankingInfo getRanking(InquiryFaqRankingPeriod period) {
        Instant now = clock.instant();
        List<FaqDemand> demands = loadInquiryFaqPort.rankByDistinctAuthors(
            DemandTarget.PUBLIC, InquiryFaq.today(now), period.since(now), null, RANKING_SIZE
        );
        Map<Long, InquiryFaqSummaryInfo> summaries = faqAssembler.publicSummaries(
                loadInquiryFaqPort.listByIds(demands.stream().map(FaqDemand::faqId).toList())
            ).stream()
            .collect(Collectors.toMap(InquiryFaqSummaryInfo::faqId, Function.identity()));
        List<InquiryFaqRankingInfo.Item> items = IntStream.range(0, demands.size())
            .mapToObj(index -> new InquiryFaqRankingInfo.Item(
                index + 1,
                summaries.get(demands.get(index).faqId()),
                demands.get(index).authorCount()
            ))
            .toList();
        return new InquiryFaqRankingInfo(period, now, items);
    }

    @Override
    public List<InquiryFaqDemandInfo> listAnswerNeeded(Long staffMemberId) {
        accessPolicy.requireStaff(staffMemberId);
        Instant now = clock.instant();
        List<FaqDemand> demands = loadInquiryFaqPort.rankByDistinctAuthors(
            DemandTarget.DRAFT, InquiryFaq.today(now), InquiryFaqRankingPeriod.DAY.since(now), null, ANSWER_NEEDED_SIZE
        );
        Map<Long, InquiryFaq> faqs = loadInquiryFaqPort.listByIds(demands.stream().map(FaqDemand::faqId).toList())
            .stream()
            .collect(Collectors.toMap(InquiryFaq::getId, Function.identity()));
        return demands.stream()
            .map(demand -> new InquiryFaqDemandInfo(
                demand.faqId(), faqs.get(demand.faqId()).getDraft().getQuestion(), demand.authorCount()
            ))
            .toList();
    }
}
