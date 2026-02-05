package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.ApplicationPartPreference;
import java.util.List;
import java.util.Set;

public interface LoadApplicationPartPreferencePort {
    List<ApplicationPartPreference> findAllByApplicationIdOrderByPriorityAsc(Long applicationId);

    /**
     * 여러 Application의 파트 선호도를 한 번에 조회
     */
    List<ApplicationPartPreference> findAllByApplicationIdsOrderByPriorityAsc(Set<Long> applicationIds);
}
