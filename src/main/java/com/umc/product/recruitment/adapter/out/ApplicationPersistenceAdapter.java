package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.application.port.out.LoadApplicationPort;
import com.umc.product.recruitment.domain.Application;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationPersistenceAdapter implements LoadApplicationPort {

    private final ApplicationRepository applicationRepository;

    @Override
    public Optional<Application> findByRecruitmentIdAndApplicantId(Long recruitmentId, Long memberId) {
        return applicationRepository.findByRecruitmentIdAndApplicantMemberId(recruitmentId, memberId);
    }

    @Override
    public Optional<Application> findById(Long applicationId) {
        return applicationRepository.findById(applicationId);
    }

    @Override
    public boolean existsByRecruitmentId(Long recruitmentId) {
        return applicationRepository.existsByRecruitmentId(recruitmentId);
    }

}
