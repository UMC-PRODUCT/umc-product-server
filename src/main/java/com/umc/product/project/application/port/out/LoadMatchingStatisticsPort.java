package com.umc.product.project.application.port.out;

import com.umc.product.project.application.port.in.query.dto.RoundMemberInfo;
import java.util.List;

public interface LoadMatchingStatisticsPort {

    /**
     * 운영진: gisuId + chapterId 범위 내 (projectId, roundId, memberId) 목록.
     * ACTIVE ProjectMember 중 application != null(지원서 경로) 기준.
     * application 이 null 인 랜덤 매칭 멤버는 차수 정보가 없으므로 제외.
     * 서비스에서 인메모리로 차수별·프로젝트×차수별·학교별 집계를 모두 파생한다.
     */
    List<RoundMemberInfo> listMembersByRound(Long gisuId, Long chapterId);
}
