package com.umc.product.project.application.port.out;

import com.umc.product.project.application.port.in.query.dto.RoundMemberInfo;
import java.util.List;

public interface LoadMatchingStatisticsPort {

    /**
     * 운영진: gisuId + chapterId 범위 내 (projectId, roundId, memberId) 목록. ACTIVE ProjectMember 중 application != null(지원서
     * 경로) 기준. application 이 null 인 랜덤 매칭 멤버는 차수 정보가 없으므로 제외.
     */
    List<RoundMemberInfo> getMembersByRound(Long gisuId, Long chapterId);

    /**
     * PM챌린저: ownerMemberId 소유 프로젝트(gisuId + chapterId 범위)의 ACTIVE ProjectMember 목록.
     */
    List<RoundMemberInfo> getMembersByRoundForOwner(Long ownerMemberId, Long gisuId, Long chapterId);
}
