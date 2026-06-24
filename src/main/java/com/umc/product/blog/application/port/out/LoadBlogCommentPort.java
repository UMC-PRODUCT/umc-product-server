package com.umc.product.blog.application.port.out;

import java.util.List;
import java.util.Optional;

import com.umc.product.blog.domain.BlogComment;
import com.umc.product.blog.domain.BlogCommentSort;

public interface LoadBlogCommentPort {

    Optional<BlogComment> findById(Long commentId);

    BlogComment getByIdAndContentId(Long commentId, Long contentId);

    List<BlogComment> listTopLevel(Long contentId, BlogCommentSort sort, Long cursor, int size);

    List<BlogComment> listRepliesByParentIds(List<Long> parentCommentIds);

    boolean existsVisibleReply(Long parentCommentId);
}
