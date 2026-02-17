package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.recruitment.domain.enums.RecruitmentPartStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecruitmentPartRepository extends JpaRepository<RecruitmentPart, Long> {
    List<RecruitmentPart> findByRecruitmentId(Long recruitmentId);

    void deleteAllByRecruitmentId(Long recruitmentId);

    List<RecruitmentPart> findByRecruitmentIdAndStatus(Long recruitmentId, RecruitmentPartStatus status);

    List<RecruitmentPart> findAllByRecruitmentIdAndStatus(Long recruitmentId, RecruitmentPartStatus status);

    @Query("SELECT rp FROM RecruitmentPart rp " +
        "JOIN Recruitment r ON rp.recruitmentId = r.id " +
        "WHERE r.rootRecruitmentId = :rootId " +
        "AND rp.status = :status")
    List<RecruitmentPart> findAllByRootIdAndStatus(
        @Param("rootId") Long rootId,
        @Param("status") RecruitmentPartStatus status
    );
}
