package com.umc.product.project.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

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

    private static final Duration MIN_RESULT_PUBLICATION_INTERVAL = Duration.ofMinutes(5);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 매칭 차수 이름

    private String description; // 매칭 차수 설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchingType type; // 매칭의 종류

    // 1~3차 지원 매칭과 랜덤 매칭을 구분합니다.
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

    // 자동 선발·배정 실행 시각: 1~3차는 PM 결정 마감 후, 랜덤 매칭은 같은 유형의 3차 결과 공개 완료 후 실행한다.
    // 아직 실행하지 않았으면 null입니다.
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
     * - 일반 차수는 startsAt < endsAt < decisionDeadline, 랜덤 매칭은 startsAt < endsAt = decisionDeadline 이어야 합니다.
     * <p>
     * - 결과 발표 시각은 선발 마감 시각으로부터 최소 5분 이후여야 합니다.
     * <p>
     * - 동일한 지부 내에서는 차수 간 startsAt ~ decisionDeadline 기간이 겹쳐서는 안됩니다.
     *
     * @param name             매칭 차수 이름 (e.g. 기획-디자인 1차 매칭)
     * @param description      차수 설명 (nullable)
     * @param type             매칭 유형 (기획-디자인/기획-개발자)
     * @param phase            차수 단계 (1~3차 지원 매칭, 랜덤 매칭)
     * @param chapterId        지부 ID (어떤 지부의 매칭 차수인지를 나타냅니다.)
     * @param startsAt         매칭 시작 시간
     * @param endsAt           매칭 종료 시간
     * @param decisionDeadline 매칭 기간 중 접수된 지원서의 합/불 결정 데드라인. <p> 경과되었을 때 UMC 규칙에 의한 최소 선발 인원을 뽑지 않은 경우 해당 규모만큼 자동으로
     *                         선발됩니다. <p> 최종 선발되지 않은 지원서는 불합격 처리됩니다. 발표 시각이 있는 차수는 합불을 발표 시 반영합니다.
     *                         <p> 랜덤 매칭은 endsAt과 같은 시각을 입력하며, startsAt~endsAt은 운영진 인원 조정 기간입니다.
     * @param resultPublishAt  결과 발표 시각 (기존 차수의 미설정 값은 null)
     * @return 생성된 매칭 차수
     */
    public static ProjectMatchingRound create(
        String name, String description,
        MatchingType type, MatchingPhase phase, Long chapterId,
        Instant startsAt, Instant endsAt, Instant decisionDeadline, Instant resultPublishAt
    ) {
        validateSchedule(type, phase, startsAt, endsAt, decisionDeadline, resultPublishAt);

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
     * 1~3차는 시작 < 종료 < 결정 마감 순서이며, 발표는 결정 마감으로부터 최소 5분 이후여야 합니다.
     * 발표 시각이 없는 기존 차수는 발표 시각 검사를 생략합니다.
     */
    public static void validateDates(
        Instant startsAt, Instant endsAt, Instant decisionDeadline, Instant resultPublishAt
    ) {
        if (startsAt == null || endsAt == null || decisionDeadline == null
            || !startsAt.isBefore(endsAt)
            || !endsAt.isBefore(decisionDeadline)
            || (resultPublishAt != null
            && resultPublishAt.isBefore(decisionDeadline.plus(MIN_RESULT_PUBLICATION_INTERVAL)))) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_INVALID_PERIOD);
        }
    }

    /**
     * 랜덤 매칭은 시작 < 종료 = 결정 마감이며, 발표는 결정 마감으로부터 최소 5분 이후여야 합니다. 1~3차는 일반 일정 규칙을 적용합니다.
     */
    public static void validateSchedule(
        MatchingType type, MatchingPhase phase,
        Instant startsAt, Instant endsAt, Instant decisionDeadline, Instant resultPublishAt
    ) {
        if (phase != MatchingPhase.RANDOM_MATCHING) {
            validateDates(startsAt, endsAt, decisionDeadline, resultPublishAt);
            return;
        }
        if (type == null || startsAt == null || endsAt == null
            || decisionDeadline == null || resultPublishAt == null
            || !startsAt.isBefore(endsAt) || !endsAt.equals(decisionDeadline)
            || resultPublishAt.isBefore(decisionDeadline.plus(MIN_RESULT_PUBLICATION_INTERVAL))) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_INVALID_PERIOD);
        }
    }

    public boolean isRandomMatching() {
        return phase == MatchingPhase.RANDOM_MATCHING;
    }

    /**
     * 매칭 차수 기본 정보와 일정을 변경합니다.
     */
    public void update(
        String name, String description, MatchingType type, MatchingPhase phase,
        Instant startsAt, Instant endsAt, Instant decisionDeadline, Instant resultPublishAt
    ) {
        validateSchedule(type, phase, startsAt, endsAt, decisionDeadline, resultPublishAt);
        if (isAutoDecisionExecuted() && (this.type != type || this.phase != phase
            || !this.startsAt.equals(startsAt) || !this.endsAt.equals(endsAt)
            || !this.decisionDeadline.equals(decisionDeadline)
            || !Objects.equals(this.resultPublishAt, resultPublishAt))) {
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
        validateSchedule(type, phase, startsAt, endsAt, decisionDeadline, resultPublishAt);

        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.decisionDeadline = decisionDeadline;
    }

    /**
     * 매칭 차수의 자동 선발 또는 랜덤 매칭 완료를 기록합니다.
     *
     * @param executedByMemberId 실행한 운영진 ID. 스케줄러 호출 시 null
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
     * 1~3차에서 startsAt <= now <= endsAt 인 경우 지원할 수 있습니다. 랜덤 매칭은 지원서를 받지 않습니다.
     *
     * @param now 기준 시각
     * @return 제공된 시점을 기준으로 매칭 차수가 활성화되었는지 여부
     */
    public boolean isOpenAt(Instant now) {
        return !isRandomMatching() && !startsAt.isAfter(now) && !endsAt.isBefore(now);
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
     * 발표 시각이 되었고 선발이 끝난 차수 중, 아직 공개하지 않은 차수인지 확인합니다.
     */
    public boolean canPublishAt(Instant now) {
        return resultPublishAt != null && !now.isBefore(resultPublishAt)
            && isAutoDecisionExecuted() && resultPublishedAt == null;
    }

    /**
     * 지원서 합불과 팀원 상태를 반영한 뒤 공개 완료 시각을 기록합니다.
     */
    public void publishResult(Instant now) {
        if (!canPublishAt(now)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_PUBLISHABLE);
        }
        resultPublishedAt = now;
    }

    /**
     * 발표 작업이 끝났는지 확인합니다. 예정 시각이 지났더라도 작업이 끝나지 않았으면 비공개입니다.
     * 발표 시각이 없는 기존 차수는 PM 결정 마감이 지나면 공개합니다.
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
