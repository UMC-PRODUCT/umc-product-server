package com.umc.product.community.application.port.in.query;

import com.umc.product.community.application.port.in.command.post.dto.PostInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 댓글 단 글 조회 UseCase
 */
public interface GetCommentedPostsUseCase {
    /**
     * 챌린저가 댓글을 단 게시글 목록 조회
     *
     * @param challengerId 챌린저 ID
     * @param pageable     페이지네이션
     * @return 게시글 목록
     */
    Page<PostInfo> getCommentedPosts(Long challengerId, Pageable pageable);
}
