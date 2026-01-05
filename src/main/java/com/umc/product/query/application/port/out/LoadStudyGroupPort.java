package com.umc.product.query.application.port.out;

import com.umc.product.command.organization.domain.StudyGroup;
import java.util.Optional;

public interface LoadStudyGroupPort {

    Optional<StudyGroup> findById(Long id);

    Optional<StudyGroup> findByName(String name);
}
