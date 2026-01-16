package com.umc.product.community.application.service;

import com.umc.product.community.application.port.out.LoadCommentPort;
import com.umc.product.community.application.port.out.SaveCommentPort;
import com.umc.product.community.domain.Comment;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService implements LoadCommentPort, SaveCommentPort {

    private final LoadCommentPort loadCommentPort;
    private final SaveCommentPort saveCommentPort;

    @Override
    public Optional<Comment> findById(Long commentId) {
        return Optional.empty();
    }

    public Page<Comment> findByPostId(Long postId, Pageable pageable) {
        return Page.empty();
    }

    public int countByPostId(Long postId) {
        return 0;
    }

    public Comment save(Comment comment) {
        return null;
    }

    public void delete(Comment comment) {

    }
}
