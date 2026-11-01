package com.umc.product.blog.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BlogSeriesContentId implements Serializable {

    private Long seriesId;
    private Long contentId;
}
