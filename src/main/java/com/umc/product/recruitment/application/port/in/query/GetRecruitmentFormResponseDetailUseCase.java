package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentFormResponseDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormResponseDetailInfo;

public interface GetRecruitmentFormResponseDetailUseCase {
    /**
     * 모집 지원 폼 응답 단건 조회
     * <p>
     * - DRAFT / SUBMITTED 모두 조회 가능 - memberId 기준 권한 검증 - recruitmentId ↔ formResponseId 연관성 검증
     */
    RecruitmentFormResponseDetailInfo get(
            GetRecruitmentFormResponseDetailQuery query
    );
}
