package com.umc.product.community.application.port.in.post.query;

import com.umc.product.community.application.port.in.PostInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 내가 쓴 글 조회 UseCase
 */
public interface GetMyPostsUseCase {
    /**
     * 챌린저가 작성한 게시글 목록 조회
     *
     * @param challengerId 챌린저 ID
     * @param pageable     페이지네이션
     * @return 게시글 목록
     */
    Page<PostInfo> getMyPosts(Long challengerId, Pageable pageable);
}
