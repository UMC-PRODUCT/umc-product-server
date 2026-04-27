package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.domain.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectJpaRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByProductOwnerMemberIdAndGisuId(Long productOwnerMemberId, Long gisuId);

    boolean existsByProductOwnerMemberIdAndGisuId(Long productOwnerMemberId, Long gisuId);
}
