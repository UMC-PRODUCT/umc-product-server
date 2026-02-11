package com.umc.product.community.application.port.out;

import com.umc.product.community.application.port.in.post.query.PostSearchQuery;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.enums.Category;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadPostPort {
    Page<Post> findAllByQuery(PostSearchQuery query, Pageable pageable);

    Optional<Post> findById(Long postId);

    /**
     * Post와 작성자 ID를 함께 조회 (중복 조회 방지)
     */
    Optional<PostWithAuthor> findByIdWithAuthor(Long postId);

    /**
     * Post와 작성자 ID를 함께 조회 (viewerChallengerId 포함, 좋아요 여부 확인용)
     */
    Optional<PostWithAuthor> findByIdWithAuthor(Long postId, Long viewerChallengerId);

    List<Post> findByCategory(Category category);

    Page<PostSearchData> searchByKeyword(String keyword, Pageable pageable);

    Long findAuthorIdByPostId(Long postId);  // 게시글 작성자 ID 조회 (단건)

    Map<Long, Long> findAuthorIdsByPostIds(List<Long> postIds);  // 게시글 작성자 ID 일괄 조회 (postId -> authorChallengerId)
}
