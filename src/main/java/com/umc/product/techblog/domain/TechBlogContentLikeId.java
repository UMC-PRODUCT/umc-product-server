package com.umc.product.techblog.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TechBlogContentLikeId implements Serializable {

    private Long contentId;
    private Long memberId;
}
