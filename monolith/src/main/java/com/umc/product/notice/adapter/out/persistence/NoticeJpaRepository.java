package com.umc.product.notice.adapter.out.persistence;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.notice.domain.Notice;

public interface NoticeJpaRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByAuthorMemberId(Long memberId);

    @Modifying
    @Query("UPDATE Notice n SET n.viewCount = n.viewCount + 1 WHERE n.id = :noticeId")
    void incrementViewCount(@Param("noticeId") Long noticeId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Notice n SET n.updatedAt = :updatedAt WHERE n.id = :noticeId")
    void updateUpdatedAt(@Param("noticeId") Long noticeId, @Param("updatedAt") Instant updatedAt);
}
