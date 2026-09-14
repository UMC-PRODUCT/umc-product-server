package com.umc.product.community.application.port.in.query;

import com.umc.product.community.application.port.in.query.dto.PostInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 내가 쓴 글 조회 UseCase
 */
public interface GetMyPostsUseCase {
    /**
     * 멤버가 작성한 게시글 목록 조회
     *
     * @param memberId 멤버 ID
     * @param pageable 페이지네이션
     * @return 게시글 목록
     */
    Page<PostInfo> getMyPosts(Long memberId, Pageable pageable);
}
