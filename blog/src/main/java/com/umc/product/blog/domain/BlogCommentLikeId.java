package com.umc.product.blog.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BlogCommentLikeId implements Serializable {

    private Long commentId;
    private Long memberId;
}
