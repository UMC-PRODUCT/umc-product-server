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

    List<Post> findByCategory(Category category);

    Page<PostSearchData> searchByKeyword(String keyword, Pageable pageable);

    Long findAuthorIdByPostId(Long postId);  // 게시글 작성자 ID 조회 (단건)

    Map<Long, Long> findAuthorIdsByPostIds(List<Long> postIds);  // 게시글 작성자 ID 일괄 조회 (postId -> authorChallengerId)

    /**
     * 챌린저가 작성한 게시글 목록 조회
     */
    Page<Post> findByAuthorChallengerId(Long challengerId, Pageable pageable);

    /**
     * 챌린저가 댓글을 단 게시글 목록 조회
     */
    Page<Post> findCommentedPostsByChallengerId(Long challengerId, Pageable pageable);

    /**
     * 챌린저가 스크랩한 게시글 목록 조회
     */
    Page<Post> findScrappedPostsByChallengerId(Long challengerId, Pageable pageable);
}
