package com.umc.product.recruitment.application.port.out;

public interface LoadInterviewSlotPort {
    boolean existsByRecruitmentId(Long recruitmentId);
}
