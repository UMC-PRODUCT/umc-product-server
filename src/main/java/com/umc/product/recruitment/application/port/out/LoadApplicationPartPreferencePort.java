package com.umc.product.recruitment.application.port.out;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.domain.ApplicationPartPreference;
import java.util.List;

public interface LoadApplicationPartPreferencePort {
    List<ApplicationPartPreference> findAllByApplicationIdOrderByPriorityAsc(Long applicationId);

    boolean existsPreferredOpenPart(Long applicationId, ChallengerPart part);
}
