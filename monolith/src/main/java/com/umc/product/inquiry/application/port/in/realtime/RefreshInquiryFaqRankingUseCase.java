package com.umc.product.inquiry.application.port.in.realtime;

public interface RefreshInquiryFaqRankingUseCase {

    /**
     * 기간마다 순위를 다시 계산해, 이 인스턴스가 마지막으로 본 순위와 다르면 공용 주소로 "새 순위 있음" 신호를 보낸다.
     * 인스턴스마다 따로 기억하므로 신호는 중복될 수 있다.
     */
    void refresh();
}
