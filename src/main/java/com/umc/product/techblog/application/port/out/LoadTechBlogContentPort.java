package com.umc.product.techblog.application.port.out;

import com.umc.product.techblog.domain.TechBlogContent;
import com.umc.product.techblog.domain.TechBlogContentType;
import java.util.Optional;

public interface LoadTechBlogContentPort {

    Optional<TechBlogContent> findByTypeAndSlug(TechBlogContentType type, String slug);

    int countLikesByContentId(Long contentId);

    boolean existsLikeByContentIdAndMemberId(Long contentId, Long memberId);
}
