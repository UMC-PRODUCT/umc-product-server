package com.umc.product.community.application.service;

import com.umc.product.community.application.port.in.post.CommentInfo;
import com.umc.product.community.application.port.in.post.query.GetCommentListUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService implements GetCommentListUseCase {

    @Override
    public List<CommentInfo> getComments(Long postId) {
        return List.of();
    }
}
