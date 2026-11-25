package com.umc.product.inquiry.application.port.in.query;

import java.util.List;
import java.util.Optional;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqDetailInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqSummaryInfo;

/**
 * 사용자에게 보이는 대표 질문. 발행 중이고 종료일이 지나지 않은 것만 다룬다.
 */
public interface GetInquiryFaqUseCase {

    /** 최근 발행 순. {@code categoryId}가 null이면 전체다. */
    List<InquiryFaqSummaryInfo> listPublic(Long categoryId);

    InquiryFaqDetailInfo getPublic(Long faqId);

    /** 질문·별칭·답변 부분 일치. 대소문자는 가리지 않는다. */
    List<InquiryFaqSummaryInfo> search(String keyword);

    /** 문의 등록 화면의 "먼저 확인해 보세요". 그 분류에서 가장 많이 묻는 것, 없으면 가장 최근에 발행한 것이다. */
    Optional<InquiryFaqSummaryInfo> suggest(Long categoryId);
}
