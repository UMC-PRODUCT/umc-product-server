package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.application.port.in.query.RecruitmentListStatus;
import com.umc.product.recruitment.domain.Recruitment;
import java.util.List;

public interface LoadRecruitmentListPort {
    List<Recruitment> findByStatus(Long memberId, RecruitmentListStatus status);
}
