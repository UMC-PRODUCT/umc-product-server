package com.umc.product.community.application.port.out;

import com.umc.product.community.domain.Comment;

public interface SaveCommentPort {
    Comment save(Comment comment);

    void delete(Comment comment);
}
