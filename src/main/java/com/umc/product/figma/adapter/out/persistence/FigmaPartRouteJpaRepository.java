package com.umc.product.figma.adapter.out.persistence;

import com.umc.product.figma.domain.FigmaPartRoute;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FigmaPartRouteJpaRepository extends JpaRepository<FigmaPartRoute, Long> {

    List<FigmaPartRoute> findAllByFileKey(String fileKey);

    Optional<FigmaPartRoute> findFirstByFileKeyAndFallbackTrue(String fileKey);

    boolean existsByFileKeyAndPageName(String fileKey, String pageName);
}
