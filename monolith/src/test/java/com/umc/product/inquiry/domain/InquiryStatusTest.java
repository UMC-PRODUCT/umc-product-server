package com.umc.product.inquiry.domain;

import static com.umc.product.inquiry.domain.enums.InquiryParticipant.AUTHOR;
import static com.umc.product.inquiry.domain.enums.InquiryParticipant.STAFF;
import static com.umc.product.inquiry.domain.enums.InquiryStatus.CLOSED;
import static com.umc.product.inquiry.domain.enums.InquiryStatus.IN_PROGRESS;
import static com.umc.product.inquiry.domain.enums.InquiryStatus.RECEIVED;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.umc.product.inquiry.domain.enums.InquiryParticipant;
import com.umc.product.inquiry.domain.enums.InquiryStatus;

class InquiryStatusTest {

    @ParameterizedTest(name = "{0} 상태에서 {1} 메시지가 오면 {2}")
    @CsvSource({
        "RECEIVED, STAFF, IN_PROGRESS",
        "RECEIVED, AUTHOR, RECEIVED",
        "IN_PROGRESS, STAFF, IN_PROGRESS",
        "IN_PROGRESS, AUTHOR, IN_PROGRESS",
        "CLOSED, STAFF, IN_PROGRESS",
        "CLOSED, AUTHOR, IN_PROGRESS"
    })
    @DisplayName("메시지를 보낸 사람에 따라 다음 상태가 정해진다")
    void 메시지에_따른_상태_전이(InquiryStatus current, InquiryParticipant sender, InquiryStatus expected) {
        // when
        InquiryStatus next = current.afterMessageFrom(sender);

        // then
        assertThat(next).isEqualTo(expected);
    }

    @Test
    @DisplayName("종료되지 않은 문의만 종료할 수 있다")
    void 종료_가능_상태() {
        assertThat(RECEIVED.isClosable()).isTrue();
        assertThat(IN_PROGRESS.isClosable()).isTrue();
        assertThat(CLOSED.isClosable()).isFalse();
    }

    @Test
    @DisplayName("모든 상태와 보낸 사람 조합에 다음 상태가 있다")
    void 전이_표는_모든_조합을_덮는다() {
        for (InquiryStatus status : InquiryStatus.values()) {
            assertThat(status.afterMessageFrom(AUTHOR)).isNotNull();
            assertThat(status.afterMessageFrom(STAFF)).isNotNull();
        }
    }
}
