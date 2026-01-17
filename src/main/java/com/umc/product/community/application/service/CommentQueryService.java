package com.umc.product.community.application.service;

import com.umc.product.community.application.port.in.post.CommentInfo;
import com.umc.product.community.application.port.in.post.Query.GetCommentListUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService implements GetCommentListUseCase {

    @Override
    public Page<CommentInfo> getComments(Long postId, Pageable pageable) {
        return Page.empty();
    }
}
