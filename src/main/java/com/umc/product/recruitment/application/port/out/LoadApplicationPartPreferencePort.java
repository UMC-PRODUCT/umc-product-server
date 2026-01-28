package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.ApplicationPartPreference;
import java.util.List;

public interface LoadApplicationPartPreferencePort {
    List<ApplicationPartPreference> findAllByApplicationIdOrderByPriorityAsc(Long applicationId);
}
