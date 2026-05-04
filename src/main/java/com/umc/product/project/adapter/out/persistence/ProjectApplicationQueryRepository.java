package com.umc.product.project.adapter.out.persistence;

import static com.umc.product.project.domain.QProjectApplication.projectApplication;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProjectApplicationQueryRepository {

    private final JPAQueryFactory queryFactory;

    /** 동일 차수에 특정 상태의 지원서가 존재하는지 확인. */
    public boolean existsByRoundAndApplicantAndStatus(
        Long roundId, Long applicantMemberId, ProjectApplicationStatus status
    ) {
        return queryFactory
            .selectOne()
            .from(projectApplication)
            .where(
                projectApplication.appliedMatchingRound.id.eq(roundId),
                projectApplication.applicantMemberId.eq(applicantMemberId),
                projectApplication.status.eq(status)
            )
            .fetchFirst() != null;
    }

    /**
     * (projectId, applicantMemberId, status) 조합으로 단건 조회.
     * applicationForm.project.id 를 통해 projectId 까지 타고 들어간다.
     */
    public Optional<ProjectApplication> findByProjectIdAndApplicantMemberIdAndStatus(
        Long projectId, Long applicantMemberId, ProjectApplicationStatus status
    ) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(projectApplication)
                .where(
                    projectApplication.applicationForm.project.id.eq(projectId),
                    projectApplication.applicantMemberId.eq(applicantMemberId),
                    projectApplication.status.eq(status)
                )
                .fetchOne()
        );
    }
}
