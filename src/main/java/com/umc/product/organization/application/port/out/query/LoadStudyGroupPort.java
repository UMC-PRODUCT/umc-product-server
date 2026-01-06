package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.StudyGroup;
import java.util.Optional;

public interface LoadStudyGroupPort {

    Optional<StudyGroup> findById(Long id);

    Optional<StudyGroup> findByName(String name);
}
