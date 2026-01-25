package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.application.port.out.LoadApplicationPartPreferencePort;
import com.umc.product.recruitment.domain.ApplicationPartPreference;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationPartPreferencePersistenceAdapter implements LoadApplicationPartPreferencePort {

    private final ApplicationPartPreferenceJpaRepository applicationPartPreferenceJpaRepository;

    @Override
    public List<ApplicationPartPreference> findAllByApplicationIdOrderByPriorityAsc(Long applicationId) {
        if (applicationId == null) {
            return List.of();
        }
        return applicationPartPreferenceJpaRepository.findAllByApplicationIdOrderByPriorityAsc(applicationId);
    }

}
