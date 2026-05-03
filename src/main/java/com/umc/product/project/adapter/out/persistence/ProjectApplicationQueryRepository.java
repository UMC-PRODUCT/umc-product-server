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
