package com.umc.product.techblog.application.port.in.command.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.techblog.domain.TechBlogDomainException;

@DisplayName("TechBlog 댓글 생성 Command 테스트")
class CreateTechBlogCommentCommandTest {

    @Test
    @DisplayName("닉네임은 trim 후 20자 이하만 허용한다")
    void 닉네임은_trim_후_20자_이하만_허용한다() {
        CreateTechBlogCommentCommand command = CreateTechBlogCommentCommand.of(
            "blog",
            "spring-boot-tips",
            null,
            1L,
            true,
            "  익명작성자  ",
            "댓글"
        );

        assertThat(command.nickname()).isEqualTo("익명작성자");

        assertThatThrownBy(() -> CreateTechBlogCommentCommand.of(
            "blog",
            "spring-boot-tips",
            null,
            1L,
            true,
            "a".repeat(21),
            "댓글"
        )).isInstanceOf(TechBlogDomainException.class);
    }
}
