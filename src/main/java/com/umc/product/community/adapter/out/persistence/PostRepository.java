package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.domain.enums.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostJpaEntity, Long> {

    List<PostJpaEntity> findByCategory(Category category);
}
