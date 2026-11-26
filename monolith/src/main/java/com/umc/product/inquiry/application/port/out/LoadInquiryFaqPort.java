package com.umc.product.inquiry.application.port.out;

import java.time.Instant;
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

    /** {@code now}에 사용자에게 보이는 대표 질문을 최근 발행 순으로 돌려준다. {@code categoryId}가 null이면 전체다. */
    List<InquiryFaq> listPublic(Long categoryId, Instant now);

    /** 사용자에게 보이는 대표 질문 중 질문·별칭·답변에 키워드가 들어 있는 것. 대소문자는 가리지 않는다. */
    List<InquiryFaq> searchPublic(String keyword, Instant now, int limit);

    Map<Long, Long> countResolutionsByFaqIds(Collection<Long> faqIds);

    /**
     * {@code since} 뒤에 접수된 문의를 대표 질문별로 묶어, 서로 다른 작성자가 많은 순으로 돌려준다. 같은 수면 최근 문의가 더
     * 최근인 쪽, 그다음 대표 질문 ID가 작은 쪽이 앞이다. 대표 질문에 연결되지 않은 문의는 세지 않는다.
     * {@code now}는 공개 여부를 가리는 시각이고 {@code categoryId}가 null이면 전체다.
     */
    List<FaqDemand> rankByDistinctAuthors(
        DemandTarget target, Instant now, Instant since, Long categoryId, int limit
    );

    /** 공개 중인 대표 질문만, 또는 한 번도 발행하지 않은 초안만 센다. */
    enum DemandTarget {
        PUBLIC,
        DRAFT
    }

    record FaqDemand(Long faqId, long authorCount, Instant latestInquiryAt) {
    }
}
