package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.application.port.in.PartOption;
import java.time.LocalDate;

public interface LoadInterviewAssignmentPort {

    long countByRecruitmentId(Long recruitmentId);

    long countByRecruitmentIdAndFirstPreferredPart(Long recruitmentId, PartOption part);

    long countByRecruitmentIdAndDateAndFirstPreferredPart(Long recruitmentId, LocalDate date, PartOption part);

}
