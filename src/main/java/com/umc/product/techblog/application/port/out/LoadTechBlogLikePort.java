package com.umc.product.techblog.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LoadTechBlogLikePort {

    int countContentLikes(Long contentId);

    boolean existsContentLike(Long contentId, Long memberId);

    int countCommentLikes(Long commentId);

    boolean existsCommentLike(Long commentId, Long memberId);

    Map<Long, Integer> countCommentLikesByCommentIds(List<Long> commentIds);

    Set<Long> findLikedCommentIds(List<Long> commentIds, Long memberId);
}
