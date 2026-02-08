package com.umc.product.recruitment.adapter.out;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.out.LoadInterviewAssignmentPort;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewAssignmentPersistenceAdapter implements LoadInterviewAssignmentPort {

    private final InterviewAssignmentQueryRepository interviewAssignmentQueryRepository;

    @Override
    public long countByRecruitmentId(Long recruitmentId) {
        return interviewAssignmentQueryRepository.countByRecruitmentId(recruitmentId);
    }

    @Override
    public long countByRecruitmentIdAndFirstPreferredPart(Long recruitmentId, PartOption part) {
        ChallengerPart challengerPart = toChallengerPart(part);
        return interviewAssignmentQueryRepository.countByRecruitmentIdAndFirstPreferredPart(
            recruitmentId, challengerPart
        );
    }

    @Override
    public long countByRecruitmentIdAndDateAndFirstPreferredPart(Long recruitmentId, LocalDate date, PartOption part) {
        ChallengerPart challengerPart = toChallengerPart(part);
        return interviewAssignmentQueryRepository.countByRecruitmentIdAndDateAndFirstPreferredPart(
            recruitmentId, date, challengerPart
        );
    }

    private ChallengerPart toChallengerPart(PartOption part) {
        if (part == null || part == PartOption.ALL) {
            throw new IllegalArgumentException("ALL/null is not allowed here");
        }
        return ChallengerPart.valueOf(part.name());
    }
}
