package com.umc.product.query.organization.application.port.out;

import com.umc.product.command.organization.domain.Chapter;
import com.umc.product.command.organization.domain.Gisu;
import java.util.List;
import java.util.Optional;

public interface ChapterPortQueryPort {

    Optional<Chapter> findById(Long id);

    List<Chapter> findAll();

    List<Chapter> findAllByGisu(Gisu gisu);
}
