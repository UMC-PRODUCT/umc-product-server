package com.umc.product.community.application.service;

import com.umc.product.community.application.port.in.post.CommentInfo;
import com.umc.product.community.application.port.in.post.query.GetCommentListUseCase;
import com.umc.product.community.application.port.out.LoadCommentPort;
import com.umc.product.community.application.port.out.LoadPostPort;
import com.umc.product.community.domain.Comment;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService implements GetCommentListUseCase {

    private final LoadPostPort loadPostPort;
    private final LoadCommentPort loadCommentPort;

    @Override
    public List<CommentInfo> getComments(Long postId) {
        loadPostPort.findById(postId)
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        Page<Comment> comments = loadCommentPort.findByPostId(postId, Pageable.unpaged());

        // TODO: challengerName은 Challenger/Member 도메인에서 조회 필요
        return comments.stream()
                .map(comment -> CommentInfo.from(comment, null))
                .toList();
    }
}
