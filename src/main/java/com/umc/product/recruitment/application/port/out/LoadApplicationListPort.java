package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.adapter.out.dto.ApplicationListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.DocumentSelectionListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.EvaluationListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.FinalSelectionListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.MyDocumentEvaluationProjection;
import com.umc.product.recruitment.application.port.in.query.dto.DocumentSelectionApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.FinalSelectionApplicationListInfo;
import com.umc.product.recruitment.domain.Application;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadApplicationListPort {
    List<Application> findByRecruitmentId(Long recruitmentId);

    /**
     * 서류 평가 대상 지원서 목록 조회 (페이지네이션, 검색, 파트 필터링 지원)
     */
    Page<ApplicationListItemProjection> searchApplications(
        Long recruitmentId,
        String keyword,
        String part,
        Long evaluatorId,
        Pageable pageable
    );

    /**
     * 해당 모집의 전체 지원자 수
     */
    long countTotalApplications(Long recruitmentId);

    /**
     * 현재 평가자가 서류 평가 완료한 지원서 수
     */
    long countEvaluatedApplications(Long recruitmentId, Long evaluatorId);

    /**
     * 특정 지원서에 대한 서류 평가 목록 조회
     */
    List<EvaluationListItemProjection> findDocumentEvaluationsByApplicationId(Long applicationId);

    /**
     * 특정 지원서에 대한 서류 평가 평균 점수 조회
     */
    BigDecimal calculateAvgDocScoreByApplicationId(Long applicationId);

    /**
     * 특정 Application이 특정 Recruitment에 속하는지 확인 (application → formResponse → form → recruitment 경로)
     */
    boolean isApplicationBelongsToRecruitment(Long applicationId, Long recruitmentId);

    /**
     * 특정 지원서에 대해 해당 평가자가 작성한 서류 평가 조회 (DRAFT, SUBMITTED 모두 포함)
     */
    Optional<MyDocumentEvaluationProjection> findMyDocumentEvaluation(Long applicationId, Long evaluatorMemberId);

    DocumentSelectionApplicationListInfo.Summary getDocumentSelectionSummary(Long recruitmentId, String part);

    Page<DocumentSelectionListItemProjection> searchDocumentSelections(
        Long recruitmentId, String part, String sort, Pageable pageable
    );

    Map<Long, BigDecimal> calculateAvgDocScoreByApplicationIds(Set<Long> applicationIds);

    FinalSelectionApplicationListInfo.Summary getFinalSelectionSummary(Long recruitmentId, String part);

    Page<FinalSelectionListItemProjection> searchFinalSelections(
        Long recruitmentId, String part, String sort, Pageable pageable
    );

    Map<Long, BigDecimal> calculateAvgInterviewScoreByApplicationIds(Set<Long> applicationIds);

}
