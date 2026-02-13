package com.umc.product.community.application.port.out;

import com.umc.product.community.domain.Comment;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadCommentPort {
    Optional<Comment> findById(Long commentId);

    Page<Comment> findByPostId(Long postId, Pageable pageable);

    int countByPostId(Long postId);

    /**
     * 여러 게시글의 댓글 수를 일괄 조회
     * @param postIds 게시글 ID 목록
     * @return postId -> 댓글 수 매핑
     */
    Map<Long, Integer> countByPostIds(List<Long> postIds);
}
