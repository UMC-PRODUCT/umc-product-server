package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.Chapter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface ChapterJpaRepository extends Repository<Chapter, Long> {

    boolean existsById(Long chapterId);

    Optional<Chapter> findById(Long chapterId);

    Chapter save(Chapter chapter);

    List<Chapter> findAll();
}
