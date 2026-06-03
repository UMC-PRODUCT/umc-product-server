package com.umc.product.techblog.application.port.out;

import java.util.Optional;

import com.umc.product.techblog.domain.TechBlogContent;
import com.umc.product.techblog.domain.TechBlogContentType;

public interface LoadTechBlogContentPort {

    Optional<TechBlogContent> findByTypeAndSlug(TechBlogContentType type, String slug);
}
