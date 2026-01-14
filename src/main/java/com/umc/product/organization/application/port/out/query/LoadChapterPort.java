package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import java.util.List;
import java.util.Optional;

public interface LoadChapterPort {

    Optional<Chapter> findById(Long id);

    List<Chapter> findAll();

    List<Chapter> findAllByGisu(Gisu gisu);
}
