package com.umc.product.inquiry.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.umc.product.inquiry.domain.InquiryFaq;
import com.umc.product.inquiry.domain.InquiryFaqContent;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import({InquiryFaqPersistenceAdapter.class, InquiryFaqQueryRepository.class})
class InquiryFaqPersistenceAdapterTest {

    private static final Long STAFF_ID = 1L;

    @Autowired
    TestEntityManager em;
    @Autowired
    InquiryFaqPersistenceAdapter sut;

    @Test
    @DisplayName("고칠 때마다 버전이 하나씩 오른다")
    void 버전_증가() {
        // given
        InquiryFaq faq = sut.save(InquiryFaq.create(InquiryFaqContent.draftOf("비밀번호를 잊었어요", null), STAFF_ID));
        assertThat(faq.getVersion()).isZero();

        // when
        faq.saveDraft(InquiryFaqContent.draftOf("비밀번호를 바꾸고 싶어요", null));
        InquiryFaq saved = sut.save(faq);

        // then
        assertThat(saved.getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("읽은 뒤 다른 트랜잭션이 먼저 고친 대표 질문을 저장하면 덮어쓰지 않고 다른 운영진이 먼저 고쳤다고 거절한다")
    void 늦게_저장하면_거절() {
        // given
        InquiryFaq faq = sut.save(InquiryFaq.create(InquiryFaqContent.draftOf("비밀번호를 잊었어요", null), STAFF_ID));
        em.getEntityManager()
            .createNativeQuery("update inquiry_faq set version = version + 1 where id = ?1")
            .setParameter(1, faq.getId())
            .executeUpdate();

        // when
        faq.saveDraft(InquiryFaqContent.draftOf("덮어쓰려던 질문", null));

        // then
        assertThatThrownBy(() -> sut.save(faq))
            .isInstanceOf(InquiryDomainException.class)
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INQUIRY_FAQ_CHANGED);
    }
}
