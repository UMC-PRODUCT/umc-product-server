package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.adapter.out.dto.ApplicationListItemProjection;
import com.umc.product.recruitment.domain.Application;
import java.util.List;
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
}
