package com.umc.product.techblog.adapter.out.persistence;

import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogContentJpaEntity;
import com.umc.product.techblog.domain.TechBlogContentType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechBlogContentJpaRepository extends JpaRepository<TechBlogContentJpaEntity, Long> {

    Optional<TechBlogContentJpaEntity> findByContentTypeAndSlug(TechBlogContentType contentType, String slug);
}
