package com.umc.product.project.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지부별로 상이한 프로젝트 매칭 차수입니다.
 * <p>
 * 기획-디자인 매칭 과 기획-개발자 매칭 크게 두 종류로 구성되어 있습니다.
 */
@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "project_matching_round")
public class ProjectMatchingRound extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 매칭 차수 이름

    private String description; // 매칭 차수 설명

    @Enumerated(EnumType.STRING)
    private MatchingType type; // 매칭 차수의 종류

    // 몇 차 매칭인지
    // 고민사항 : 처음에는 Long 값으로 phaseNo로 넣었었는데, 매칭 관련해서는 확장성이 사실 상 필요 없는 점을 고려하였음.
    // 또한 랜덤매칭의 경우, UI 상에 표시해줄 필요성은 있다고 생각하는데 .. DB에서 THIRD가 지난 상태를 랜덤매칭으로 지정할지가 고민 ..
    @Enumerated(EnumType.STRING)
    private MatchingPhase phase;

    private Long chapterId;

    private Instant startsAt;
    private Instant endsAt;
    private Instant decisionDeadline; // 선발 마감 기한

    // 선발 마감 기한이 지나서 스케쥴러 등에 의해서 자동으로 선발이 실행된 시간
    // null인 경우 아닙니다.
    private Instant autoDecisionExecutedAt;
    // 자동 선발이 실행된 경우, 누가 실행했는지 (운영진 ID)
    // 스케쥴러가 진행한 경우 null로 유지함
    private Long autoDecisionExecutedBy;
}
