package com.umc.product.query.organization.application.port.out;

import com.umc.product.command.organization.domain.StudyGroup;
import java.util.Optional;

public interface StudyGroupQueryPort {

    Optional<StudyGroup> findById(Long id);

    Optional<StudyGroup> findByName(String name);
}
