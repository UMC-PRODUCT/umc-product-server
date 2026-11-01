package com.umc.product.blog.application.port.out;

import com.umc.product.blog.domain.BlogContent;

public interface SaveBlogContentPort {

    BlogContent save(BlogContent content);
}
