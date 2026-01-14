package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.domain.Enum.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardsRepository extends JpaRepository<BoardsJpaEntity, Long> {
    List<BoardsJpaEntity> findByCategory(Category category);

    List<BoardsJpaEntity> findByRegion(String region);
}
