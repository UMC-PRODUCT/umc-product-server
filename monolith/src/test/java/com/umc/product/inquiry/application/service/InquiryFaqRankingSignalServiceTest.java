package com.umc.product.inquiry.application.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.inquiry.application.port.in.query.GetInquiryFaqRankingUseCase;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqRankingInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqSummaryInfo;
import com.umc.product.inquiry.application.port.in.realtime.dto.InquiryFaqRankingSignal;
import com.umc.product.inquiry.application.port.out.InquiryRealtimeBroadcastPort;
import com.umc.product.inquiry.domain.enums.InquiryFaqRankingPeriod;

@ExtendWith(MockitoExtension.class)
@DisplayName("인기 순위 신호")
class InquiryFaqRankingSignalServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-25T00:00:00Z");

    @Mock
    GetInquiryFaqRankingUseCase getInquiryFaqRankingUseCase;
    @Mock
    InquiryRealtimeBroadcastPort broadcastPort;

    @InjectMocks
    InquiryFaqRankingSignalService signalService;

    @Test
    @DisplayName("순위가 그대로면 다음 주기에 신호를 다시 보내지 않는다")
    void 그대로면_안_보냄() {
        // given
        순위(InquiryFaqRankingPeriod.DAY, 1L, 3);
        순위(InquiryFaqRankingPeriod.WEEK, 1L, 5);

        // when
        signalService.refresh();
        signalService.refresh();

        // then
        verify(broadcastPort, times(1))
            .broadcastRankingSignal(new InquiryFaqRankingSignal(InquiryFaqRankingPeriod.DAY, NOW));
        verify(broadcastPort, times(1))
            .broadcastRankingSignal(new InquiryFaqRankingSignal(InquiryFaqRankingPeriod.WEEK, NOW));
    }

    @Test
    @DisplayName("신호 보내기에 실패하면 본 순위를 기억하지 않아, 순위가 그대로여도 다음 주기에 다시 보낸다")
    void 실패하면_다음_주기에_다시_보냄() {
        // given
        순위(InquiryFaqRankingPeriod.DAY, 1L, 3);
        순위(InquiryFaqRankingPeriod.WEEK, 1L, 5);
        InquiryFaqRankingSignal daySignal = new InquiryFaqRankingSignal(InquiryFaqRankingPeriod.DAY, NOW);
        willThrow(new IllegalStateException("broker down")).given(broadcastPort).broadcastRankingSignal(daySignal);

        signalService.refresh();
        reset(broadcastPort);

        // when
        signalService.refresh();

        // then
        verify(broadcastPort).broadcastRankingSignal(daySignal);
        verify(broadcastPort, never()).broadcastRankingSignal(
            new InquiryFaqRankingSignal(InquiryFaqRankingPeriod.WEEK, NOW)
        );
    }

    private void 순위(InquiryFaqRankingPeriod period, Long faqId, long askedMemberCount) {
        given(getInquiryFaqRankingUseCase.getRanking(period)).willReturn(new InquiryFaqRankingInfo(
            period,
            NOW,
            List.of(new InquiryFaqRankingInfo.Item(
                1, new InquiryFaqSummaryInfo(faqId, "질문", null, null), askedMemberCount
            ))
        ));
    }
}
