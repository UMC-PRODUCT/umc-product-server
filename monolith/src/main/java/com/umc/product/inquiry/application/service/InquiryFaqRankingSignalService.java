package com.umc.product.inquiry.application.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.umc.product.inquiry.application.port.in.query.GetInquiryFaqRankingUseCase;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqRankingInfo;
import com.umc.product.inquiry.application.port.in.realtime.RefreshInquiryFaqRankingUseCase;
import com.umc.product.inquiry.application.port.in.realtime.dto.InquiryFaqRankingSignal;
import com.umc.product.inquiry.application.port.out.InquiryRealtimeBroadcastPort;
import com.umc.product.inquiry.domain.enums.InquiryFaqRankingPeriod;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryFaqRankingSignalService implements RefreshInquiryFaqRankingUseCase {

    private final GetInquiryFaqRankingUseCase getInquiryFaqRankingUseCase;
    private final InquiryRealtimeBroadcastPort broadcastPort;
    private final Map<InquiryFaqRankingPeriod, List<Entry>> lastSeen = new ConcurrentHashMap<>();

    @Override
    public void refresh() {
        for (InquiryFaqRankingPeriod period : InquiryFaqRankingPeriod.values()) {
            InquiryFaqRankingInfo ranking = getInquiryFaqRankingUseCase.getRanking(period);
            List<Entry> entries = ranking.items().stream()
                .map(item -> new Entry(item.faq().faqId(), item.askedMemberCount()))
                .toList();
            if (entries.equals(lastSeen.get(period))) {
                continue;
            }
            try {
                broadcastPort.broadcastRankingSignal(new InquiryFaqRankingSignal(period, ranking.refreshedAt()));
                lastSeen.put(period, entries);
            } catch (RuntimeException exception) {
                log.warn("문의 인기 순위 신호 전송 실패: period={}", period, exception);
            }
        }
    }

    private record Entry(Long faqId, long askedMemberCount) {
    }
}
