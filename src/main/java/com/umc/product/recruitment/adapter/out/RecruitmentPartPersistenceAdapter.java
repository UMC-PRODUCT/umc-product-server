package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.application.port.out.SaveRecruitmentPartPort;
import com.umc.product.recruitment.domain.RecruitmentPart;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecruitmentPartPersistenceAdapter implements SaveRecruitmentPartPort {

    private final RecruitmentPartRepository recruitmentPartRepository;

    @Override
    public List<RecruitmentPart> saveAll(List<RecruitmentPart> parts) {
        if (parts == null || parts.isEmpty()) {
            return List.of();
        }
        return recruitmentPartRepository.saveAll(parts);
    }

    @Override
    public void deleteAllByRecruitmentId(Long recruitmentId) {
        recruitmentPartRepository.deleteAllByRecruitmentId(recruitmentId);
    }
}
