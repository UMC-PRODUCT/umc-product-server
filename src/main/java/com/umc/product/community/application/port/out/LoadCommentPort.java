package com.umc.product.community.application.port.out;

import com.umc.product.community.domain.Comment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadCommentPort {
    Optional<Comment> findById(Long commentId);

    Page<Comment> findByPostId(Long postId, Pageable pageable);

    int countByPostId(Long postId);
}
