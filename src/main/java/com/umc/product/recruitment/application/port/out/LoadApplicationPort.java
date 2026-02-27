package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.adapter.out.dto.ApplicationIdWithFormResponseId;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.domain.Application;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface LoadApplicationPort {
    Optional<Application> findByRecruitmentIdAndApplicantId(Long recruitmentId, Long memberId);

    Optional<Application> findById(Long applicationId);

    boolean existsByRecruitmentId(Long recruitmentId);

    boolean existsByRecruitmentIdAndApplicantMemberId(Long recruitmentId, Long applicantMemberId);

    Optional<Application> findByRecruitmentIdAndApplicantMemberId(Long recruitmentId, Long applicantMemberId);

    List<Application> findAllByApplicantMemberId(Long applicantMemberId);

    Optional<Application> getByRecruitmentIdAndApplicationId(Long recruitmentId, Long applicationId);

    long countByRecruitmentId(Long recruitmentId);

    long countByRecruitmentIdAndFirstPreferredPart(Long recruitmentId, PartOption part);

    List<ApplicationIdWithFormResponseId> findApplicationIdsWithFormResponseIdsByRecruitment(Long recruitmentId);

    List<ApplicationIdWithFormResponseId> findApplicationIdsWithFormResponseIdsByRecruitmentAndFirstPreferredPart(
        Long recruitmentId,
        PartOption part
    );

    Map<Long, Double> findAvgDocumentScoresByApplicationIds(Set<Long> applicationIds);

    List<ApplicationIdWithFormResponseId> findDocPassedApplicationIdsWithFormResponseIdsByRecruitment(
        Long recruitmentId);

    List<ApplicationIdWithFormResponseId> findDocPassedApplicationIdsWithFormResponseIdsByRecruitmentAndFirstPreferredPart(
        Long recruitmentId,
        PartOption partOption
    );

    long countDocPassedByRootId(Long recruitmentId);

    long countDocPassedByRootIdAndFirstPreferredPart(Long recruitmentId, PartOption part);

    List<ApplicationIdWithFormResponseId> findDocPassedApplicationIdsWithFormResponseIdsByRootId(
        Long rootId);

    List<ApplicationIdWithFormResponseId> findDocPassedApplicationIdsWithFormResponseIdsByRootIdAndFirstPreferredPart(
        Long rootId,
        PartOption partOption
    );

    long countByRootRecruitmentId(Long rootId);

    List<ApplicationIdWithFormResponseId> findApplicationIdsWithFormResponseIdsByRootRecruitmentId(Long rootId);

    long countByRootIdAndFirstPreferredPart(Long rootId, PartOption part);

    List<Application> findByRootRecruitmentId(Long rootId);

    List<ApplicationIdWithFormResponseId> findApplicationIdsWithFormResponseIdsByRootRecruitmentIdAndFirstPreferredPart(
        Long rootId, PartOption part
    );
}
