package com.umc.product.inquiry.adapter.in.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.umc.product.inquiry.application.port.in.realtime.RefreshInquiryFaqRankingUseCase;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InquiryFaqRankingRefreshScheduler {

    private final RefreshInquiryFaqRankingUseCase refreshInquiryFaqRankingUseCase;

    @Scheduled(
        fixedDelayString = "${app.inquiry.faq-ranking-refresh-interval:PT5M}",
        initialDelayString = "${app.inquiry.faq-ranking-refresh-interval:PT5M}"
    )
    public void refresh() {
        refreshInquiryFaqRankingUseCase.refresh();
    }
}
