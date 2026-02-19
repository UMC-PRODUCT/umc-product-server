package com.umc.product.community.application.port.in.query;

import com.umc.product.community.application.port.in.query.dto.PostInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 스크랩 해둔 글 조회 UseCase
 */
public interface GetScrappedPostsUseCase {
    /**
     * 챌린저가 스크랩한 게시글 목록 조회
     *
     * @param challengerId 챌린저 ID
     * @param pageable     페이지네이션
     * @return 게시글 목록
     */
    Page<PostInfo> getScrappedPosts(Long challengerId, Pageable pageable);
}
