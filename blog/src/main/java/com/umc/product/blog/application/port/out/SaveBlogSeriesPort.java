package com.umc.product.blog.application.port.out;

import com.umc.product.blog.domain.BlogSeries;

public interface SaveBlogSeriesPort {

    BlogSeries save(BlogSeries series);
}
