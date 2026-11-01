package com.umc.product.blog.application.port.out;

import java.util.List;

import com.umc.product.blog.application.port.out.dto.BlogSeoPathRow;

public interface LoadBlogSeoPort {

    List<BlogSeoPathRow> listPublicSeoPaths();
}
