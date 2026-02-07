package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.application.port.out.LoadRecruitmentPartPort;
import com.umc.product.recruitment.application.port.out.SaveRecruitmentPartPort;
import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.recruitment.domain.enums.RecruitmentPartStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecruitmentPartPersistenceAdapter implements SaveRecruitmentPartPort, LoadRecruitmentPartPort {

    private final RecruitmentPartRepository recruitmentPartRepository;

    // ================ SaveRecruitmentPartPort ================
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

    // ================ LoadRecruitmentPartPort ================
    @Override
    public List<RecruitmentPart> findByRecruitmentId(Long recruitmentId) {
        return recruitmentPartRepository.findByRecruitmentId(recruitmentId);
    }

    @Override
    public List<RecruitmentPart> findByRecruitmentIdAndStatus(Long recruitmentId, RecruitmentPartStatus status) {
        return recruitmentPartRepository.findByRecruitmentIdAndStatus(recruitmentId, status);
    }
}
