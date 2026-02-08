package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.application.port.in.PartOption;
import java.time.LocalDate;
import java.util.Set;

public interface LoadInterviewAssignmentPort {

    long countByRecruitmentId(Long recruitmentId);

    long countByRecruitmentIdAndFirstPreferredPart(Long recruitmentId, PartOption part);

    long countByRecruitmentIdAndDateAndFirstPreferredPart(Long recruitmentId, LocalDate date, PartOption part);

    Set<Long> findAssignedApplicationIdsByRecruitmentId(Long recruitmentId);

    
}
