package com.umc.product.test.domain.rehearsal;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;

/**
 * @param candidates        이 지부를 매칭 지부로 가진 활동 중 챌린저 중 차수 유형 파트인 사람
 * @param projects          이 지부·기수의 공개(진행 중) 프로젝트
 * @param applications      이 차수 지원서. 취소는 뺀다
 * @param rejectedLastRound 회원 ID → 같은 지부·유형 직전 차수에서 불합격한 프로젝트 ID
 */
public record RoundSnapshot(
    Round round,
    Instant observedAt,
    List<Candidate> candidates,
    List<ProjectSlot> projects,
    List<ApplicationRow> applications,
    Map<Long, Set<Long>> rejectedLastRound
) {
    /** 봇이 쓴 주관식 답변의 머리. 요약은 이것으로 봇 지원과 QA 지원을 나눈다. */
    public static final String BOT_ANSWER_PREFIX = "[봇]";

    public RoundSnapshot {
        candidates = List.copyOf(candidates);
        projects = List.copyOf(projects);
        applications = List.copyOf(applications);
        rejectedLastRound = Map.copyOf(rejectedLastRound);
    }

    public record Round(String name, Long chapterId, String chapterName, MatchingType type, RoundTimes times) {
        public Set<ChallengerPart> parts() {
            return type == MatchingType.PLAN_DESIGN
                ? Set.of(ChallengerPart.DESIGN)
                : Set.of(ChallengerPart.WEB_PRODUCT_ENGINEER, ChallengerPart.MOBILE_PRODUCT_ENGINEER);
        }
    }

    public record Candidate(Long memberId, String email, ChallengerPart part, boolean hasTeam) {
    }

    public record ProjectSlot(Long projectId, String name, Long ownerMemberId, String ownerEmail,
                              Map<ChallengerPart, PartSlot> parts) {
        public ProjectSlot {
            parts = Map.copyOf(parts);
        }
    }

    /**
     * @param existingMembers   이 차수 밖에서 들어온 팀원(활동 중 + 발표 대기)
     * @param selectedThisRound 이 차수가 선발한 팀원(활동 중 + 발표 대기). 자동 선발 뒤에 생긴다
     */
    public record PartSlot(int quota, int existingMembers, int selectedThisRound) {
        public int assigned() {
            return existingMembers + selectedThisRound;
        }

        public int remaining() {
            return Math.max(0, quota - assigned());
        }
    }

    /**
     * @param botAnswered 답변 중 하나가 {@code [봇]}으로 시작한다
     */
    public record ApplicationRow(Long applicationId, Long projectId, Long applicantMemberId, String applicantEmail,
                                 ChallengerPart part, ProjectApplicationStatus status, boolean botAnswered) {
        public boolean submitted() {
            return status != ProjectApplicationStatus.DRAFT;
        }
    }
}
