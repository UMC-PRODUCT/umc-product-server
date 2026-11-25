package com.umc.product.inquiry.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

class InquiryFaqContentTest {

    @Test
    @DisplayName("별칭은 앞뒤 공백을 빼고 빈 값과 중복을 지우며 처음 순서를 지킨다")
    void 별칭_정리() {
        InquiryFaqContent content = 별칭으로(Arrays.asList(" 로그인 실패 ", "Login 오류", "로그인 실패", "  ", null));

        assertThat(content.getAliases()).containsExactly("로그인 실패", "Login 오류");
    }

    @Test
    @DisplayName("별칭은 20개까지 받고 21개면 거절한다. 중복을 지운 뒤 개수로 센다")
    void 별칭_개수() {
        List<String> twenty = IntStream.rangeClosed(1, 20).mapToObj(i -> "별칭" + i).toList();
        List<String> twentyOne = IntStream.rangeClosed(1, 21).mapToObj(i -> "별칭" + i).toList();
        List<String> twentyWithDuplicate = IntStream.rangeClosed(0, 20).mapToObj(i -> "별칭" + Math.max(i, 1)).toList();

        assertThat(별칭으로(twenty).getAliases()).hasSize(20);
        assertThat(별칭으로(twentyWithDuplicate).getAliases()).hasSize(20);
        assertThatThrownBy(() -> 별칭으로(twentyOne))
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INVALID_INQUIRY_FAQ_ALIASES);
    }

    @Test
    @DisplayName("별칭 하나는 50자까지 받고 51자면 거절한다. 이모지는 한 글자로 센다")
    void 별칭_길이() {
        assertThat(별칭으로(List.of("가".repeat(50))).getAliases()).containsExactly("가".repeat(50));
        assertThat(별칭으로(List.of("😀".repeat(50))).getAliases()).hasSize(1);
        assertThatThrownBy(() -> 별칭으로(List.of("가".repeat(51))))
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INVALID_INQUIRY_FAQ_ALIASES);
    }

    @Test
    @DisplayName("질문은 앞뒤 공백을 빼고 1~200자만 받는다")
    void 질문_길이() {
        assertThat(InquiryFaqContent.draftOf("  비밀번호를 잊었어요 ", null).getQuestion()).isEqualTo("비밀번호를 잊었어요");
        assertThat(InquiryFaqContent.draftOf("가".repeat(200), null).getQuestion()).hasSize(200);
        assertThatThrownBy(() -> InquiryFaqContent.draftOf("가".repeat(201), null))
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INVALID_INQUIRY_FAQ_QUESTION);
        assertThatThrownBy(() -> InquiryFaqContent.draftOf("   ", null))
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INVALID_INQUIRY_FAQ_QUESTION);
    }

    @Test
    @DisplayName("빈 답변은 없는 답변으로 본다")
    void 빈_답변() {
        InquiryFaqContent content = InquiryFaqContent.of("질문", "  ", List.of(), null, null, null, null);

        assertThat(content.getAnswer()).isNull();
    }

    private static InquiryFaqContent 별칭으로(List<String> aliases) {
        return InquiryFaqContent.of("비밀번호를 잊었어요", null, aliases, null, null, null, null);
    }
}
