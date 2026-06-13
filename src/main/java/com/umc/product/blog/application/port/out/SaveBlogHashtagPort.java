package com.umc.product.blog.application.port.out;

import java.util.List;

import com.umc.product.blog.domain.BlogContentHashtag;
import com.umc.product.blog.domain.BlogHashtag;

public interface SaveBlogHashtagPort {

    BlogHashtag save(BlogHashtag hashtag);

    void deleteContentHashtags(Long contentId);

    List<BlogContentHashtag> saveContentHashtags(List<BlogContentHashtag> contentHashtags);
}
