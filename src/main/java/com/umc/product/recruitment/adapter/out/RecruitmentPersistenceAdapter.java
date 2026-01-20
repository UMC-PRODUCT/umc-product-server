package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.application.port.out.SaveRecruitmentPort;
import com.umc.product.recruitment.domain.Recruitment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecruitmentPersistenceAdapter implements SaveRecruitmentPort {
    private final RecruitmentRepository recruitmentRepository;

    @Override
    public Recruitment save(Recruitment recruitment) {
        return recruitmentRepository.save(recruitment);
    }

    @Override
    public void deleteById(Long recruitmentId) {
        recruitmentRepository.deleteById(recruitmentId);
    }

}
