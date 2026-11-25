package com.umc.product.inquiry.application.port.out;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.umc.product.inquiry.domain.InquiryFaq;
import com.umc.product.inquiry.domain.enums.InquiryFaqStatus;

public interface LoadInquiryFaqPort {

    Optional<InquiryFaq> findById(Long faqId);

    List<InquiryFaq> listByIds(Collection<Long> faqIds);

    /** 운영진 목록. 상태와 수정본 카테고리로 거르고, 최근에 바뀐 것부터 돌려준다. null이면 거르지 않는다. */
    List<InquiryFaq> listForStaff(InquiryFaqStatus status, Long categoryId);

    /** 사용자에게 보이는 대표 질문을 최근 발행 순으로 돌려준다. {@code categoryId}가 null이면 전체다. */
    List<InquiryFaq> listPublic(Long categoryId, LocalDate today);

    /** 사용자에게 보이는 대표 질문 중 질문·별칭·답변에 키워드가 들어 있는 것. 대소문자는 가리지 않는다. */
    List<InquiryFaq> searchPublic(String keyword, LocalDate today, int limit);

    Map<Long, Long> countResolutionsByFaqIds(Collection<Long> faqIds);
}
