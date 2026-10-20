package com.umc.product.project.domain;

import java.time.Instant;

import com.umc.product.common.BaseEntity;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지부별로 상이한 프로젝트 매칭 차수입니다.
 * <p>
 * 기획-디자인 매칭 과 기획-개발자 매칭 크게 두 종류로 구성되어 있습니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "project_matching_round",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_project_matching_round_type_phase_chapter",
            // 동일한 매칭 종류와 차수를 가진건 지부에 하나만 있어야 해요!
            columnNames = {"type", "phase", "chapter_id"}
        )
    }
)
public class ProjectMatchingRound extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 매칭 차수 이름

    private String description; // 매칭 차수 설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchingType type; // 매칭의 종류

    /*
    몇 차 매칭인지
    고민사항 : 처음에는 Long 값으로 phaseNo로 넣었었는데, 매칭 관련해서는 확장성이 사실 상 필요 없는 점을 고려하였음.
    또한 랜덤매칭의 경우, UI 상에 표시해줄 필요성은 있다고 생각하는데 .. DB에서 THIRD가 지난 상태를 랜덤매칭으로 지정할지가 고민 ..

    -> 랜덤매칭에 최초에 기간이 필요했던 이유는 사람이 일을 해야하니까인데.. 지금은 그냥 랜덤매칭 딸깎 하면 되는거라 enum에 넣을 필요는 없을 듯
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchingPhase phase; // 매칭 차수

    @Column(nullable = false)
    private Long chapterId;

    @Column(nullable = false)
    private Instant startsAt;
    @Column(nullable = false)
    private Instant endsAt;
    @Column(nullable = false)
    private Instant decisionDeadline; // 선발 마감 기한

    private Instant resultPublishAt; // 결과 발표 예정 시각 (기존 차수는 null)
    private Instant resultPublishedAt; // 결과 공개 완료 시각 (공개 전에는 null)

    // 선발 마감 기한이 지나서 스케쥴러 등에 의해서 자동으로 선발이 실행된 시간
    // null인 경우 아닙니다.
    private Instant autoDecisionExecutedAt;
    // 자동 선발이 실행된 경우, 누가 실행했는지 (운영진 ID)
    // 스케쥴러가 진행한 경우 null로 유지함
    private Long autoDecisionExecutedMemberId;

    @Builder(access = AccessLevel.PRIVATE)
    private ProjectMatchingRound(
        String name, String description, MatchingType type,
        MatchingPhase phase, Long chapterId,
        Instant startsAt, Instant endsAt, Instant decisionDeadline, Instant resultPublishAt
    ) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.phase = phase;
        this.chapterId = chapterId;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.decisionDeadline = decisionDeadline;
        this.resultPublishAt = resultPublishAt;
    }

    /**
     * 프로젝트 매칭 차수를 생성하는 팩토리 메서드입니다.
     * <p>
     * 검증 내용:
     * <p>
     * - 동일한 유형과 차수를 가진 매칭 차수는 생성할 수 없습니다.
     * <p>
     * - startsAt < endsAt < decisionDeadline 이어야 합니다.
     * <p>
     * - 결과 발표 시각은 선발 마감 시각보다 늦어야 합니다.
     * <p>
     * - 동일한 지부 내에서는 차수 간 startsAt ~ decisionDeadline 기간이 겹쳐서는 안됩니다.
     *
     * @param name             매칭 차수 이름 (e.g. 기획-디자인 1차 매칭)
     * @param description      차수 설명 (nullable)
     * @param type             매칭 유형 (기획-디자인/기획-개발자)
     * @param phase            차수 단계 (1,2,3차 중에서 선택, 랜덤 매칭은 별도로 제공하지 않으며 운영진이 직접 API를 통해서 실행하면 됩니다.)
     * @param chapterId        지부 ID (어떤 지부의 매칭 차수인지를 나타냅니다.)
     * @param startsAt         매칭 시작 시간
     * @param endsAt           매칭 종료 시간
     * @param decisionDeadline 매칭 기간 중 접수된 지원서의 합/불 결정 데드라인. <p> 경과되었을 때 UMC 규칙에 의한 최소 선발 인원을 뽑지 않은 경우 해당 규모만큼 자동으로
     *                         선발됩니다. <p> 최소 선발 인원을 제외한 나머지는 모두 불합격 처리됩니다.
     * @param resultPublishAt  결과 발표 시각 (기존 차수의 미설정 값은 null)
     * @return 생성된 매칭 차수
     */
    public static ProjectMatchingRound create(
        String name, String description,
        MatchingType type, MatchingPhase phase, Long chapterId,
        Instant startsAt, Instant endsAt, Instant decisionDeadline, Instant resultPublishAt
    ) {
        validateDates(startsAt, endsAt, decisionDeadline, resultPublishAt);

        return ProjectMatchingRound.builder()
            .name(name)
            .description(description)
            .type(type)
            .phase(phase)
            .chapterId(chapterId)
            .startsAt(startsAt)
            .endsAt(endsAt)
            .decisionDeadline(decisionDeadline)
            .resultPublishAt(resultPublishAt)
            .build();
    }

    /**
     * 새로운 매칭 차수와 기존 매칭 차수의 startsAt ~ decisionDeadline 기간이 겹치는지 검증하는 로직
     */
    public static void validateNoOverlap(
        ProjectMatchingRound newRound,
        ProjectMatchingRound existingRound
    ) {
        if (existingRound == null || newRound == null) {
            return;
        }
        boolean sameChapter = newRound.chapterId.equals(existingRound.chapterId);
        boolean overlapped = !newRound.decisionDeadline.isBefore(existingRound.startsAt)
            && !existingRound.decisionDeadline.isBefore(newRound.startsAt);

        if (sameChapter && overlapped) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_PERIOD_OVERLAPPED);
        }
    }

    /**
     * 매칭 차수의 날짜가 유효한지 검증합니다.
     * <p>
     * 시작 < 종료 < 결정 마감 순서여야 하며, 결과 발표 시각은 결정 마감보다 늦어야 합니다.
     * <p>
     * 발표 시각이 없는 기존 차수는 null을 허용하여 이름 등 다른 정보를 수정할 수 있도록 합니다.
     */
    public static void validateDates(
        Instant startsAt, Instant endsAt, Instant decisionDeadline, Instant resultPublishAt
    ) {
        if (startsAt == null || endsAt == null || decisionDeadline == null
            || !startsAt.isBefore(endsAt)
            || !endsAt.isBefore(decisionDeadline)
            || (resultPublishAt != null && !resultPublishAt.isAfter(decisionDeadline))) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_INVALID_PERIOD);
        }
    }

    /**
     * 매칭 차수 기본 정보와 일정을 변경합니다.
     */
    public void update(
        String name, String description, MatchingType type, MatchingPhase phase,
        Instant startsAt, Instant endsAt, Instant decisionDeadline, Instant resultPublishAt
    ) {
        validateDates(startsAt, endsAt, decisionDeadline, resultPublishAt);
        if (isAutoDecisionExecuted() && (this.type != type || this.phase != phase
            || !this.startsAt.equals(startsAt) || !this.endsAt.equals(endsAt)
            || !this.decisionDeadline.equals(decisionDeadline)
            || !java.util.Objects.equals(this.resultPublishAt, resultPublishAt))) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_LOCKED);
        }

        this.name = name;
        this.description = description;
        this.type = type;
        this.phase = phase;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.decisionDeadline = decisionDeadline;
        this.resultPublishAt = resultPublishAt;
    }

    /**
     * 매칭 차수의 일정을 변경하는 메서드입니다. 매칭 차수의 일정은 매칭이 시작하기 전까지만 변경할 수 있습니다.
     * <p>
     * 각 매개변수에 대한 설명은 {@link #create}를 참조해주세요.
     */
    public void reschedule(Instant startsAt, Instant endsAt, Instant decisionDeadline) {
        validateDates(startsAt, endsAt, decisionDeadline, resultPublishAt);

        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.decisionDeadline = decisionDeadline;
    }

    /**
     * 매칭 차수에 대해 랜덤 매칭을 실행하는 경우 사용됩니다. 실행한 지부장 Member ID를 받습니다.
     *
     * @param executedByMemberId 실행한 지부장 ID
     */
    public void executeAutoDecision(Long executedByMemberId) {
        this.autoDecisionExecutedAt = Instant.now();
        this.autoDecisionExecutedMemberId = executedByMemberId;
    }

    /*
     * 도메인 메소드에 포함되어야 할지, Service에 포함되어야 할지 애매한 내용:
     *
     * 잔여 TO가 존재하는 프로젝트 ID와 아직 지원하지 않은 챌린저 ID 목록을 받아서 각 프로젝트의 잔여 TO 및 파트에 맞게 배정하는 로직이 필요합니다.
     */

    /**
     * 자동 선발(마감 처리)이 이미 실행되었는지 여부. 실행 전이면 PM 판정이나 자동 선발로 지원서 결과가 아직 바뀔 수 있습니다.
     */
    public boolean isAutoDecisionExecuted() {
        return autoDecisionExecutedAt != null;
    }

    /**
     * 특정 시점 기준으로 매칭 차수가 활성화 되어있는지 여부를 판단합니다.
     * <p>
     * startsAt <= now <= endsAt 인 경우에 활성화된 것으로 간주합니다.
     *
     * @param now 기준 시각
     * @return 제공된 시점을 기준으로 매칭 차수가 활성화되었는지 여부
     */
    public boolean isOpenAt(Instant now) {
        return !startsAt.isAfter(now) && !endsAt.isBefore(now);
    }

    /**
     * 스케쥴러에서 판단용으로 사용할 메소드.
     * <p>
     * 주어진 매개변수를 기준으로 선발 마감 기한이 경과하였는지 여부를 판단합니다.
     * <p>
     * decisionDeadline < now 인 경우에 true를 반환합니다.
     *
     * @param now 기준 시각
     * @return 선발 마감 기한 경과 여부
     */
    public boolean isDecisionDeadlinePassed(Instant now) {
        return decisionDeadline.isBefore(now);
    }

    /**
     * 결과 발표 시각에 도달한 차수를 공개할 수 있는지 판단합니다.
     * <p>
     * 자동 선발이 완료됐으며 아직 공개되지 않은 차수만 대상입니다.
     *
     * @param now 기준 시각
     * @return 결과 공개 가능 여부
     */
    public boolean canPublishAt(Instant now) {
        return resultPublishAt != null && !now.isBefore(resultPublishAt)
            && isAutoDecisionExecuted() && resultPublishedAt == null;
    }

    /**
     * 매칭 결과의 공개 완료 시각을 기록합니다.
     * <p>
     * 팀원 활성화와 동일한 트랜잭션에서 호출합니다.
     *
     * @param now 공개 완료 시각
     */
    public void publishResult(Instant now) {
        if (!canPublishAt(now)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_PUBLISHABLE);
        }
        resultPublishedAt = now;
    }

    /**
     * 특정 시점에 지원자에게 매칭 결과를 공개할 수 있는지 판단합니다.
     * <p>
     * 발표 작업에서 팀원 활성화와 함께 {@code resultPublishedAt} 을 기록한 뒤 공개합니다.
     * {@code resultPublishAt} 이 null인 기존 차수는 {@code decisionDeadline < now} 이면 공개합니다.
     *
     * @param now 기준 시각
     * @return 매칭 결과 공개 가능 여부
     */
    public boolean isResultPublishedAt(Instant now) {
        if (resultPublishAt == null) {
            return isDecisionDeadlinePassed(now);
        }
        return resultPublishedAt != null && !resultPublishedAt.isAfter(now);
    }

    /**
     * PM 의 합/불 토글이 허용되는 시점인지 검증합니다.
     * <p>
     * {@code endsAt < now <= decisionDeadline} 범위에서만 mutable. 지원(모집)이 끝난 뒤부터 결정 마감 전까지만 합/불을 변경할 수 있습니다.
     * <ul>
     *   <li>아직 지원 기간 중({@code now <= endsAt}): {@link ProjectErrorCode#PROJECT_MATCHING_ROUND_NOT_ENDED}</li>
     *   <li>결정 마감 경과({@code now > decisionDeadline}): {@link ProjectErrorCode#PROJECT_MATCHING_ROUND_LOCKED}</li>
     * </ul>
     *
     * @param now 기준 시각
     */
    public void validateIsMutableAt(Instant now) {
        if (!now.isAfter(endsAt)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_ENDED);
        }
        if (now.isAfter(decisionDeadline)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_LOCKED);
        }
    }

    /**
     * PM/운영진의 지원서 조회가 허용되는 시점인지 검증합니다.
     * <p>
     * 지원(모집)이 끝난 뒤({@code endsAt < now})부터 조회할 수 있습니다. 아직 지원 기간 중인 경우
     * {@link ProjectErrorCode#PROJECT_MATCHING_ROUND_APPLICANTS_NOT_VIEWABLE} 도메인 예외를 던집니다.
     *
     * @param now 기준 시각
     */
    public void validateIsViewableAt(Instant now) {
        if (!now.isAfter(endsAt)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_APPLICANTS_NOT_VIEWABLE);
        }
    }
}
