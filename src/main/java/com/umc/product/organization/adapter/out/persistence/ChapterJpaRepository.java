package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.Chapter;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface ChapterJpaRepository extends Repository<Chapter, Long> {

    boolean existsById(Long chapterId);

    Optional<Chapter> findById(Long chapterId);

    void save(Chapter chapter);
}
