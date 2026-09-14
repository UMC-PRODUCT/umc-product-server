package com.umc.product.community.application.port.out.post;

import com.umc.product.community.application.port.in.query.dto.PostSearchQuery;
import com.umc.product.community.application.port.out.dto.PostWithAuthor;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.enums.Category;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 커뮤니티 게시글을 불러오는 포트
 * <p>
 * Post, PostWithAuthor, PostSearchData 총 3가지 return typ
 */
public interface LoadPostPort {

    Page<Post> findAllByQuery(PostSearchQuery query, Pageable pageable);

    Optional<Post> findById(Long postId);

    List<Post> findByCategory(Category category);

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

    // ======= PostWithAuthor 관련 메서드 =======

    /**
     * Post와 작성자 ID를 함께 조회 (중복 조회 방지)
     */
    Optional<PostWithAuthor> findByIdWithAuthor(Long postId);

    /**
     * Post와 작성자 ID를 함께 조회 (viewerChallengerId 포함, 좋아요 여부 확인용)
     */
    Optional<PostWithAuthor> findByIdWithAuthor(Long postId, Long viewerChallengerId);

    // ======= PostSearchData 관련 메서드 =======
    Page<Post> searchByKeyword(String keyword, Pageable pageable);

    // ======= 기타 메서드 =======
    Long findAuthorIdByPostId(Long postId);  // 게시글 작성자 ID 조회 (단건)

    Map<Long, Long> findAuthorIdsByPostIds(List<Long> postIds);  // 게시글 작성자 ID 일괄 조회 (postId -> authorChallengerId)


}
