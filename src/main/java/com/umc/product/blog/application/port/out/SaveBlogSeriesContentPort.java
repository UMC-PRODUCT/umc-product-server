package com.umc.product.blog.application.port.out;

import java.util.List;

import com.umc.product.blog.domain.BlogSeriesContent;

public interface SaveBlogSeriesContentPort {

    void deleteBySeriesId(Long seriesId);

    List<BlogSeriesContent> saveAll(List<BlogSeriesContent> seriesContents);
}
