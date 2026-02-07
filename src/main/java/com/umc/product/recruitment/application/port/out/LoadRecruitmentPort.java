package com.umc.product.recruitment.application.port.out;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentPublishedInfo;
import com.umc.product.recruitment.application.port.in.query.RecruitmentListStatus;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentListInfo;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LoadRecruitmentPort {
    Optional<Recruitment> findById(Long recruitmentId);

    RecruitmentDraftInfo findDraftInfoById(Long recruitmentId);

    RecruitmentApplicationFormInfo findApplicationFormInfoById(Long recruitmentId);

    boolean existsOtherOngoingPublishedRecruitment(
        Long schoolId,
        Long excludeRecruitmentId,
        Instant now
    );

    List<ChallengerPart> findPartsByRecruitmentId(Long recruitmentId);

    List<RecruitmentSchedule> findSchedulesByRecruitmentId(Long recruitmentId);

    List<RecruitmentListInfo.RecruitmentSummary> findRecruitmentSummaries(
        Long schoolId,
        RecruitmentListStatus status
    );

    List<RecruitmentListInfo.RecruitmentSummary> findDraftRecruitmentSummaries(
        Long schoolId
    );

    Optional<Long> findActiveRecruitmentId(Long schoolId, Long gisuId, Instant now);

    Optional<Recruitment> findByFormId(Long formId);

    RecruitmentApplicationFormInfo findApplicationFormInfoForApplicantById(
        Long recruitmentId,
        RecruitmentApplicationFormInfo.PreferredPartInfo preferredPartInfo
    );

    List<Long> findActiveRecruitmentIds(Long schoolId, Long gisuId, Instant now);

    RecruitmentPublishedInfo.ScheduleInfo findPublishedScheduleInfoByRecruitmentId(Long recruitmentId);

    Map<RecruitmentScheduleType, RecruitmentSchedule> findScheduleMapByRecruitmentId(Long recruitmentId);
}
