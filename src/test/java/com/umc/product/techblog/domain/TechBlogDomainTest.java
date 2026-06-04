package com.umc.product.techblog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TechBlog 도메인 테스트")
class TechBlogDomainTest {

    @Test
    @DisplayName("blog와 release path 값을 콘텐츠 타입으로 파싱한다")
    void blog와_release_path_값을_콘텐츠_타입으로_파싱한다() {
        assertThat(TechBlogContentType.fromPath("blog")).isEqualTo(TechBlogContentType.BLOG);
        assertThat(TechBlogContentType.fromPath("release")).isEqualTo(TechBlogContentType.RELEASE);
    }

    @Test
    @DisplayName("지원하지 않는 콘텐츠 타입은 예외를 던진다")
    void 지원하지_않는_콘텐츠_타입은_예외를_던진다() {
        assertThatThrownBy(() -> TechBlogContentType.fromPath("notice"))
            .isInstanceOf(TechBlogDomainException.class);
    }

    @Test
    @DisplayName("댓글 내용은 trim 후 1자 이상 1000자 이하만 허용한다")
    void 댓글_내용은_trim_후_1자_이상_1000자_이하만_허용한다() {
        TechBlogComment comment = TechBlogComment.create(1L, null, 1L, false, null, "  정상 댓글  ");

        assertThat(comment.getContent()).isEqualTo("정상 댓글");

        assertThatThrownBy(() -> TechBlogComment.create(1L, null, 1L, false, null, "   "))
            .isInstanceOf(TechBlogDomainException.class);
        assertThatThrownBy(() -> TechBlogComment.create(1L, null, 1L, false, null, "a".repeat(1001)))
            .isInstanceOf(TechBlogDomainException.class);
    }

    @Test
    @DisplayName("비회원 댓글은 trim 후 1자 이상 20자 이하 닉네임이 필요하다")
    void 비회원_댓글은_trim_후_1자_이상_20자_이하_닉네임이_필요하다() {
        TechBlogComment comment = TechBlogComment.create(1L, null, null, true, "  게스트  ", "댓글");

        assertThat(comment.isAnonymous()).isTrue();
        assertThat(comment.getNickname()).isEqualTo("게스트");

        assertThatThrownBy(() -> TechBlogComment.create(1L, null, null, true, null, "댓글"))
            .isInstanceOf(TechBlogDomainException.class);
        assertThatThrownBy(() -> TechBlogComment.create(1L, null, null, true, "a".repeat(21), "댓글"))
            .isInstanceOf(TechBlogDomainException.class);
    }

    @Test
    @DisplayName("회원 댓글도 닉네임이 전달되면 20자 이하만 허용한다")
    void 회원_댓글도_닉네임이_전달되면_20자_이하만_허용한다() {
        TechBlogComment comment = TechBlogComment.create(1L, null, 1L, true, "  익명작성자  ", "댓글");

        assertThat(comment.getNickname()).isEqualTo("익명작성자");

        assertThatThrownBy(() -> TechBlogComment.create(1L, null, 1L, true, "a".repeat(21), "댓글"))
            .isInstanceOf(TechBlogDomainException.class);
    }

    @Test
    @DisplayName("삭제된 댓글은 삭제 타입별 placeholder를 표시하고 대댓글을 허용하지 않는다")
    void 삭제된_댓글은_삭제_타입별_placeholder를_표시하고_대댓글을_허용하지_않는다() {
        TechBlogComment userDeleted = TechBlogComment.create(1L, null, 1L, false, null, "댓글");
        userDeleted.deleteByUser(1L);

        assertThat(userDeleted.displayContent()).isEqualTo("삭제된 댓글입니다");
        assertThat(userDeleted.canReply()).isFalse();
        assertThatThrownBy(userDeleted::ensureNotDeleted)
            .isInstanceOf(TechBlogDomainException.class);

        TechBlogComment adminDeleted = TechBlogComment.create(1L, null, 1L, false, null, "댓글");
        adminDeleted.deleteByAdmin(2L);

        assertThat(adminDeleted.displayContent()).isEqualTo("관리자에 의해서 삭제된 댓글입니다");
        assertThat(adminDeleted.canReply()).isFalse();
    }

    @Test
    @DisplayName("콘텐츠 좋아요는 유효한 콘텐츠 ID와 회원 ID로 생성한다")
    void 콘텐츠_좋아요는_유효한_콘텐츠_ID와_회원_ID로_생성한다() {
        TechBlogContentLike like = TechBlogContentLike.create(1L, 2L);

        assertThat(like.getContentId()).isEqualTo(1L);
        assertThat(like.getMemberId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("콘텐츠 좋아요는 유효하지 않은 ID를 거부한다")
    void 콘텐츠_좋아요는_유효하지_않은_ID를_거부한다() {
        assertThatThrownBy(() -> TechBlogContentLike.create(null, 1L))
            .isInstanceOf(TechBlogDomainException.class);
        assertThatThrownBy(() -> TechBlogContentLike.create(1L, 0L))
            .isInstanceOf(TechBlogDomainException.class);
    }

    @Test
    @DisplayName("댓글 좋아요는 유효한 댓글 ID와 회원 ID로 생성한다")
    void 댓글_좋아요는_유효한_댓글_ID와_회원_ID로_생성한다() {
        TechBlogCommentLike like = TechBlogCommentLike.create(1L, 2L);

        assertThat(like.getCommentId()).isEqualTo(1L);
        assertThat(like.getMemberId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("댓글 좋아요는 유효하지 않은 ID를 거부한다")
    void 댓글_좋아요는_유효하지_않은_ID를_거부한다() {
        assertThatThrownBy(() -> TechBlogCommentLike.create(-1L, 1L))
            .isInstanceOf(TechBlogDomainException.class);
        assertThatThrownBy(() -> TechBlogCommentLike.create(1L, null))
            .isInstanceOf(TechBlogDomainException.class);
    }
}
