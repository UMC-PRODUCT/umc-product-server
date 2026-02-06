package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.adapter.out.dto.ApplicationListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.EvaluationListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.MyDocumentEvaluationProjection;
import com.umc.product.recruitment.application.port.out.LoadApplicationListPort;
import com.umc.product.recruitment.application.port.out.LoadApplicationPort;
import com.umc.product.recruitment.application.port.out.SaveApplicationPort;
import com.umc.product.recruitment.domain.Application;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationPersistenceAdapter implements LoadApplicationPort, SaveApplicationPort,
    LoadApplicationListPort {

    private final ApplicationRepository applicationRepository;
    private final ApplicationQueryRepository applicationQueryRepository;

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

    @Override
    public boolean existsByRecruitmentIdAndApplicantMemberId(Long recruitmentId, Long applicantMemberId) {
        return applicationRepository.existsByRecruitmentIdAndApplicantMemberId(recruitmentId, applicantMemberId);
    }

    @Override
    public Application save(Application application) {
        return applicationRepository.save(application);
    }

    @Override
    public Optional<Application> findByRecruitmentIdAndApplicantMemberId(Long recruitmentId, Long applicantMemberId) {
        return applicationRepository.findByRecruitmentIdAndApplicantMemberId(recruitmentId, applicantMemberId);
    }

    @Override
    public List<Application> findAllByApplicantMemberId(Long applicantMemberId) {
        return applicationRepository.findAllByApplicantMemberId(applicantMemberId);
    }

    @Override
    public List<Application> findByRecruitmentId(Long recruitmentId) {
        return applicationRepository.findAllByRecruitmentId(recruitmentId);
    }

    @Override
    public Page<ApplicationListItemProjection> searchApplications(
        Long recruitmentId,
        String keyword,
        String part,
        Long evaluatorId,
        Pageable pageable
    ) {
        return applicationQueryRepository.searchApplications(recruitmentId, keyword, part, evaluatorId, pageable);
    }

    @Override
    public long countTotalApplications(Long recruitmentId) {
        return applicationQueryRepository.countTotalApplications(recruitmentId);
    }

    @Override
    public long countEvaluatedApplications(Long recruitmentId, Long evaluatorId) {
        return applicationQueryRepository.countEvaluatedApplications(recruitmentId, evaluatorId);
    }

    @Override
    public List<EvaluationListItemProjection> findDocumentEvaluationsByApplicationId(Long applicationId) {
        return applicationQueryRepository.findDocumentEvaluationsByApplicationId(applicationId);
    }

    @Override
    public BigDecimal calculateAvgDocScoreByApplicationId(Long applicationId) {
        return applicationQueryRepository.calculateAvgDocScoreByApplicationId(applicationId);
    }

    @Override
    public boolean isApplicationBelongsToRecruitment(Long applicationId, Long recruitmentId) {
        return applicationQueryRepository.isApplicationBelongsToRecruitment(applicationId, recruitmentId);
    }

    @Override
    public Optional<MyDocumentEvaluationProjection> findMyDocumentEvaluation(Long applicationId, Long evaluatorMemberId) {
        return applicationQueryRepository.findMyDocumentEvaluation(applicationId, evaluatorMemberId);
    }
}
