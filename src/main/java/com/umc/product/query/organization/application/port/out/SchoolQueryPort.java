package com.umc.product.query.organization.application.port.out;

import com.umc.product.command.organization.domain.Chapter;
import com.umc.product.command.organization.domain.School;
import java.util.List;
import java.util.Optional;

public interface SchoolQueryPort {

    Optional<School> findById(Long id);

    List<School> findAll();

    List<School> findByChapter(Chapter chapter);
}
