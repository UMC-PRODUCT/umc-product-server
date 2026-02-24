package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.adapter.out.persistence.entity.PostJpaEntity;
import com.umc.product.community.domain.enums.Category;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<PostJpaEntity, Long> {

    List<PostJpaEntity> findByCategory(Category category);

    @Query("SELECT p.id as postId, p.authorChallengerId as authorId FROM PostJpaEntity p WHERE p.id IN :postIds")
    List<PostAuthorProjection> findAuthorIdsByPostIds(@Param("postIds") List<Long> postIds);

    default Map<Long, Long> findAuthorIdsMapByPostIds(List<Long> postIds) {
        return findAuthorIdsByPostIds(postIds).stream()
            .collect(Collectors.toMap(
                PostAuthorProjection::getPostId,
                PostAuthorProjection::getAuthorId
            ));
    }

    interface PostAuthorProjection {
        Long getPostId();

        Long getAuthorId();
    }
}
