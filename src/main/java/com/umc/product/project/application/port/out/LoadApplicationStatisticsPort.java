package com.umc.product.project.application.port.out;

import com.umc.product.project.application.port.in.query.dto.RoundMemberInfo;
import java.util.List;

public interface LoadApplicationStatisticsPort {

    /**
     * 운영진: gisuId + chapterId 범위 내 (projectId, roundId, applicantMemberId) 목록.
     * DRAFT/CANCELLED 제외(SUBMITTED 이상) 기준.
     * 서비스에서 인메모리로 차수별·프로젝트×차수별·학교별 집계를 모두 파생한다.
     */
    List<RoundMemberInfo> listApplicantsByRound(Long gisuId, Long chapterId);

    /**
     * PM챌린저: 단일 프로젝트의 (projectId, roundId, applicantMemberId) 목록.
     */
    List<RoundMemberInfo> listApplicantsByRoundForProject(Long projectId);
}
