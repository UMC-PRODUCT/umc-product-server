package com.umc.product.blog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Blog 도메인 테스트")
class BlogDomainTest {

    @Test
    @DisplayName("콘텐츠는 CMS 필드와 공개 상태를 검증하고 생성한다")
    void 콘텐츠는_CMS_필드와_공개_상태를_검증하고_생성한다() {
        BlogContent content = BlogContent.create(
            BlogContentType.ENGINEERING,
            "spring-boot-tips",
            "  제목  ",
            "요약",
            null,
            "  본문  ",
            BlogContentStatus.PUBLISHED,
            1L,
            null,
            null,
            null
        );

        assertThat(content.getTitle()).isEqualTo("제목");
        assertThat(content.getContent()).isEqualTo("본문");
        assertThat(content.isPublished()).isTrue();
        assertThat(content.getPublishedAt()).isNotNull();

        assertThatThrownBy(() -> BlogContent.create(
            BlogContentType.ENGINEERING,
            "slug",
            "",
            null,
            null,
            "본문",
            BlogContentStatus.DRAFT,
            1L,
            null,
            null,
            null
        )).isInstanceOf(BlogDomainException.class);
    }

    @Test
    @DisplayName("콘텐츠는 DRAFT 전환 시 publishedAt을 제거하고 soft delete된다")
    void 콘텐츠는_DRAFT_전환_시_publishedAt을_제거하고_soft_delete된다() {
        BlogContent content = BlogContent.create(
            BlogContentType.ENGINEERING,
            "spring-boot-tips",
            "제목",
            null,
            null,
            "본문",
            BlogContentStatus.PUBLISHED,
            1L,
            null,
            null,
            null
        );

        content.update("spring-boot-tips", "제목", null, null, "본문", BlogContentStatus.DRAFT, null, null, null);

        assertThat(content.isPublished()).isFalse();
        assertThat(content.getPublishedAt()).isNull();

        content.softDelete(1L);

        assertThat(content.isDeleted()).isTrue();
        assertThat(content.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("시리즈는 slug와 작성자를 가진다")
    void 시리즈는_slug와_작성자를_가진다() {
        BlogSeries series = BlogSeries.create(
            BlogContentType.ENGINEERING,
            "spring-series",
            "Spring Series",
            "설명",
            null,
            1L,
            null,
            null,
            null
        );

        assertThat(series.getSlug()).isEqualTo("spring-series");
        assertThat(series.isAuthor(1L)).isTrue();
        assertThat(series.canonicalPath()).isEqualTo("/series/engineering/spring-series");
    }

    @Test
    @DisplayName("시리즈 콘텐츠는 유효한 표시 순서로 생성한다")
    void 시리즈_콘텐츠는_유효한_표시_순서로_생성한다() {
        BlogSeriesContent relation = BlogSeriesContent.create(1L, 2L, 0);

        assertThat(relation.getSeriesId()).isEqualTo(1L);
        assertThat(relation.getContentId()).isEqualTo(2L);

        assertThatThrownBy(() -> BlogSeriesContent.create(1L, 2L, -1))
            .isInstanceOf(BlogDomainException.class);
    }

    @Test
    @DisplayName("해시태그는 # 제거, NFKC 정규화, 소문자 변환 후 slug를 만든다")
    void 해시태그는_정규화한다() {
        BlogHashtag hashtag = BlogHashtag.create("  #SpringBoot  ");

        assertThat(hashtag.getName()).isEqualTo("SpringBoot");
        assertThat(hashtag.getNormalizedName()).isEqualTo("springboot");
        assertThat(hashtag.getSlug()).isEqualTo("springboot");

        assertThatThrownBy(() -> BlogHashtag.create("hello world"))
            .isInstanceOf(BlogDomainException.class);
    }

    @Test
    @DisplayName("engineering, design, product, release path 값을 콘텐츠 타입으로 파싱한다")
    void engineering_design_product_release_path_값을_콘텐츠_타입으로_파싱한다() {
        assertThat(BlogContentType.fromPath("engineering")).isEqualTo(BlogContentType.ENGINEERING);
        assertThat(BlogContentType.fromPath("design")).isEqualTo(BlogContentType.DESIGN);
        assertThat(BlogContentType.fromPath("product")).isEqualTo(BlogContentType.PRODUCT);
        assertThat(BlogContentType.fromPath("release")).isEqualTo(BlogContentType.RELEASE);
    }

    @Test
    @DisplayName("지원하지 않는 콘텐츠 타입은 예외를 던진다")
    void 지원하지_않는_콘텐츠_타입은_예외를_던진다() {
        assertThatThrownBy(() -> BlogContentType.fromPath("notice"))
            .isInstanceOf(BlogDomainException.class);
    }

    @Test
    @DisplayName("댓글 내용은 trim 후 1자 이상 1000자 이하만 허용한다")
    void 댓글_내용은_trim_후_1자_이상_1000자_이하만_허용한다() {
        BlogComment comment = BlogComment.create(1L, null, 1L, false, null, "  정상 댓글  ");

        assertThat(comment.getContent()).isEqualTo("정상 댓글");

        assertThatThrownBy(() -> BlogComment.create(1L, null, 1L, false, null, "   "))
            .isInstanceOf(BlogDomainException.class);
        assertThatThrownBy(() -> BlogComment.create(1L, null, 1L, false, null, "a".repeat(1001)))
            .isInstanceOf(BlogDomainException.class);
    }

    @Test
    @DisplayName("비회원 댓글은 trim 후 1자 이상 20자 이하 닉네임이 필요하다")
    void 비회원_댓글은_trim_후_1자_이상_20자_이하_닉네임이_필요하다() {
        BlogComment comment = BlogComment.create(1L, null, null, true, "  게스트  ", "댓글");

        assertThat(comment.isAnonymous()).isTrue();
        assertThat(comment.getNickname()).isEqualTo("게스트");

        assertThatThrownBy(() -> BlogComment.create(1L, null, null, true, null, "댓글"))
            .isInstanceOf(BlogDomainException.class);
        assertThatThrownBy(() -> BlogComment.create(1L, null, null, true, "a".repeat(21), "댓글"))
            .isInstanceOf(BlogDomainException.class);
    }

    @Test
    @DisplayName("회원 댓글도 닉네임이 전달되면 20자 이하만 허용한다")
    void 회원_댓글도_닉네임이_전달되면_20자_이하만_허용한다() {
        BlogComment comment = BlogComment.create(1L, null, 1L, true, "  익명작성자  ", "댓글");

        assertThat(comment.getNickname()).isEqualTo("익명작성자");

        assertThatThrownBy(() -> BlogComment.create(1L, null, 1L, true, "a".repeat(21), "댓글"))
            .isInstanceOf(BlogDomainException.class);
    }

    @Test
    @DisplayName("삭제된 댓글은 삭제 타입별 placeholder를 표시하고 대댓글을 허용하지 않는다")
    void 삭제된_댓글은_삭제_타입별_placeholder를_표시하고_대댓글을_허용하지_않는다() {
        BlogComment userDeleted = BlogComment.create(1L, null, 1L, false, null, "댓글");
        userDeleted.deleteByUser(1L);

        assertThat(userDeleted.displayContent()).isEqualTo("삭제된 댓글이에요");
        assertThat(userDeleted.canReply()).isFalse();
        assertThatThrownBy(userDeleted::ensureNotDeleted)
            .isInstanceOf(BlogDomainException.class);

        BlogComment adminDeleted = BlogComment.create(1L, null, 1L, false, null, "댓글");
        adminDeleted.deleteByAdmin(2L);

        assertThat(adminDeleted.displayContent()).isEqualTo("관리자가 삭제한 댓글이에요");
        assertThat(adminDeleted.canReply()).isFalse();
    }

    @Test
    @DisplayName("콘텐츠 좋아요는 유효한 콘텐츠 ID와 회원 ID로 생성한다")
    void 콘텐츠_좋아요는_유효한_콘텐츠_ID와_회원_ID로_생성한다() {
        BlogContentLike like = BlogContentLike.create(1L, 2L);

        assertThat(like.getContentId()).isEqualTo(1L);
        assertThat(like.getMemberId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("콘텐츠 좋아요는 유효하지 않은 ID를 거부한다")
    void 콘텐츠_좋아요는_유효하지_않은_ID를_거부한다() {
        assertThatThrownBy(() -> BlogContentLike.create(null, 1L))
            .isInstanceOf(BlogDomainException.class);
        assertThatThrownBy(() -> BlogContentLike.create(1L, 0L))
            .isInstanceOf(BlogDomainException.class);
    }

    @Test
    @DisplayName("댓글 좋아요는 유효한 댓글 ID와 회원 ID로 생성한다")
    void 댓글_좋아요는_유효한_댓글_ID와_회원_ID로_생성한다() {
        BlogCommentLike like = BlogCommentLike.create(1L, 2L);

        assertThat(like.getCommentId()).isEqualTo(1L);
        assertThat(like.getMemberId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("댓글 좋아요는 유효하지 않은 ID를 거부한다")
    void 댓글_좋아요는_유효하지_않은_ID를_거부한다() {
        assertThatThrownBy(() -> BlogCommentLike.create(-1L, 1L))
            .isInstanceOf(BlogDomainException.class);
        assertThatThrownBy(() -> BlogCommentLike.create(1L, null))
            .isInstanceOf(BlogDomainException.class);
    }
}
