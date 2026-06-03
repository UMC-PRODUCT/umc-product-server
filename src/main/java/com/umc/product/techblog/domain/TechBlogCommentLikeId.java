package com.umc.product.techblog.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TechBlogCommentLikeId implements Serializable {

    private Long commentId;
    private Long memberId;
}
