package com.umc.product.techblog.application.port.out;

import com.umc.product.techblog.domain.TechBlogContent;

public interface SaveTechBlogContentPort {

    TechBlogContent save(TechBlogContent content);
}
