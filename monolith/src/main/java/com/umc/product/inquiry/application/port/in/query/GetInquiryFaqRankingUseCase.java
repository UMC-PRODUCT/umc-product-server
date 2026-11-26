package com.umc.product.inquiry.application.port.in.query;

import java.util.List;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqDemandInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqRankingInfo;
import com.umc.product.inquiry.domain.enums.InquiryFaqRankingPeriod;

public interface GetInquiryFaqRankingUseCase {

    /** 공개 중인 대표 질문의 인기 순위 상위 5개. 부를 때마다 지금 기준으로 계산한다. */
    InquiryFaqRankingInfo getRanking(InquiryFaqRankingPeriod period);

    /** 운영진 전용. 아직 발행하지 않은 초안 중 최근 24시간 동안 묻은 회원이 많은 순. */
    List<InquiryFaqDemandInfo> listAnswerNeeded(Long staffMemberId);
}
