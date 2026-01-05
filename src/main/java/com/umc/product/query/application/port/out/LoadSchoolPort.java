package com.umc.product.query.application.port.out;

import com.umc.product.command.organization.domain.Chapter;
import com.umc.product.command.organization.domain.School;
import java.util.List;
import java.util.Optional;

public interface LoadSchoolPort {

    Optional<School> findById(Long id);

    List<School> findAll();

    List<School> findByChapter(Chapter chapter);
}
