package com.umc.product.project.domain;

import java.time.Instant;
import java.util.Objects;

import com.umc.product.common.BaseEntity;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프로젝트 매칭에서만 적용되는 챌린저의 지부 이동 기록입니다.
 * <p>
 * 챌린저의 지부는 원래 회원 학교와 기수별 지부-학교 매핑으로 결정됩니다. 지부 간 인원 불균형을 맞추기 위해
 * 특정 챌린저를 다른 지부 프로젝트에 매칭해야 할 때 이 기록을 남기며, 활성 기록이 있는 동안 그 챌린저의 매칭 지부는
 * {@link #toChapterId} 가 됩니다. 공지·출석·지부장 관리 범위 등 매칭 밖의 지부 판단에는 영향을 주지 않습니다.
 * <p>
 * 기수·회원당 활성(미해제) 기록은 하나만 존재할 수 있으며, Flyway partial unique index
 * ({@code uk_project_matching_chapter_transfer_active}) 로 보장합니다.
 */
@Entity
@Table(name = "project_matching_chapter_transfer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMatchingChapterTransfer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gisuId;

    @Column(nullable = false)
    private Long memberId;

    // 등록 시점에 회원 학교 기준으로 계산한 원지부 스냅샷
    @Column(nullable = false)
    private Long fromChapterId;

    @Column(nullable = false)
    private Long toChapterId;

    @Column(length = 255)
    private String reason;

    @Column(nullable = false)
    private Long createdByMemberId;

    private Instant releasedAt;

    private Long releasedByMemberId;

    @Column(length = 255)
    private String releaseReason;

    @Builder(access = AccessLevel.PRIVATE)
    private ProjectMatchingChapterTransfer(
        Long gisuId, Long memberId, Long fromChapterId, Long toChapterId, String reason, Long createdByMemberId
    ) {
        this.gisuId = gisuId;
        this.memberId = memberId;
        this.fromChapterId = fromChapterId;
        this.toChapterId = toChapterId;
        this.reason = reason;
        this.createdByMemberId = createdByMemberId;
    }

    /**
     * 매칭 지부 이동을 생성합니다. 원지부와 이동지부가 같으면 {@link ProjectErrorCode#CHAPTER_TRANSFER_SAME_CHAPTER}.
     * <p>
     * 파트, 지원서, 팀원 여부, 권한 등 다른 도메인 정보가 필요한 검증은 Service 가 호출 전에 수행합니다.
     */
    public static ProjectMatchingChapterTransfer create(
        Long gisuId, Long memberId, Long fromChapterId, Long toChapterId, String reason, Long createdByMemberId
    ) {
        if (Objects.equals(fromChapterId, toChapterId)) {
            throw new ProjectDomainException(ProjectErrorCode.CHAPTER_TRANSFER_SAME_CHAPTER);
        }
        return ProjectMatchingChapterTransfer.builder()
            .gisuId(gisuId)
            .memberId(memberId)
            .fromChapterId(fromChapterId)
            .toChapterId(toChapterId)
            .reason(reason)
            .createdByMemberId(createdByMemberId)
            .build();
    }

    /**
     * 이동을 해제합니다. 해제 후에는 회원 학교 기준 지부로 다시 매칭됩니다.
     */
    public void release(Long releasedByMemberId, String releaseReason) {
        if (!isActive()) {
            throw new ProjectDomainException(ProjectErrorCode.CHAPTER_TRANSFER_ALREADY_RELEASED);
        }
        this.releasedAt = Instant.now();
        this.releasedByMemberId = releasedByMemberId;
        this.releaseReason = releaseReason;
    }

    public boolean isActive() {
        return this.releasedAt == null;
    }
}
