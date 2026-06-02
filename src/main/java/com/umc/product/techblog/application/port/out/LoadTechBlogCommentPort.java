package com.umc.product.techblog.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.umc.product.techblog.domain.TechBlogComment;
import com.umc.product.techblog.domain.TechBlogCommentSort;

public interface LoadTechBlogCommentPort {

    Optional<TechBlogComment> findById(Long commentId);

    Optional<TechBlogComment> findByIdAndContentId(Long commentId, Long contentId);

    List<TechBlogComment> listTopLevel(Long contentId, TechBlogCommentSort sort, Long cursor, int size);

    List<TechBlogComment> listRepliesByParentIds(List<Long> parentCommentIds);

    boolean existsVisibleReply(Long parentCommentId);

    Map<Long, Integer> countLikesByCommentIds(List<Long> commentIds);

    Set<Long> findLikedCommentIds(List<Long> commentIds, Long memberId);
}
