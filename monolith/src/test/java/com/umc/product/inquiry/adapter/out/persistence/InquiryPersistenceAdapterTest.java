package com.umc.product.inquiry.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.InquiryCategory;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import({InquiryPersistenceAdapter.class, InquiryQueryRepository.class})
class InquiryPersistenceAdapterTest {

    private static final Long AUTHOR_ID = 1L;

    @Autowired
    TestEntityManager em;
    @Autowired
    InquiryPersistenceAdapter sut;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        categoryId = em.persistAndFlush(InquiryCategory.create("저장 테스트", 99)).getId();
    }

    @Test
    @DisplayName("같은 작성자의 같은 등록 요청이 겹치면 문의 등록 충돌로 바꾼다")
    void 등록_요청_중복() {
        // given
        UUID clientRequestId = UUID.randomUUID();
        sut.create(문의(clientRequestId, 10L));

        // when & then
        assertThatThrownBy(() -> sut.create(문의(clientRequestId, 11L)))
            .isInstanceOf(InquiryDomainException.class)
            .extracting("baseCode").isEqualTo(InquiryErrorCode.INQUIRY_REQUEST_CONFLICT);
    }

    @Test
    @DisplayName("다른 제약 위반은 등록 충돌로 바꾸지 않고 그대로 던진다")
    void 다른_제약_위반() {
        // given
        sut.create(문의(UUID.randomUUID(), 10L));

        // when & then
        assertThatThrownBy(() -> sut.create(문의(UUID.randomUUID(), 10L)))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Inquiry 문의(UUID clientRequestId, Long chatRoomId) {
        return Inquiry.create(
            AUTHOR_ID, categoryId, null, "로그인이 안 돼요", chatRoomId, clientRequestId, null, Instant.now()
        );
    }
}
