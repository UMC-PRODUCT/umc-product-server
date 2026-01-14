package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.Application;
import java.util.List;

public interface LoadApplicationListPort {
    List<Application> findByRecruitmentId(Long recruitmentId);
}
