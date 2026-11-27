package com.umc.product.notice.adapter.out.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.notice.domain.NoticeRead;

public interface NoticeReadJpaRepository extends JpaRepository<NoticeRead, Long> {
    List<NoticeRead> findAllByNoticeId(Long noticeId);

    @Modifying
    @Query(value = """
        INSERT INTO notice_read (notice_id, challenger_id, created_at, updated_at)
        VALUES (:noticeId, :challengerId, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (notice_id, challenger_id) DO NOTHING
        """, nativeQuery = true)
    void insertIfAbsent(@Param("noticeId") Long noticeId, @Param("challengerId") Long challengerId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM NoticeRead nr WHERE nr.notice.id = :noticeId")
    void deleteAllByNoticeId(@Param("noticeId") Long noticeId);

    boolean existsByNoticeIdAndChallengerId(Long noticeId, Long challengerId);

    int countByNoticeId(Long noticeId);

    long countByNoticeIdAndChallengerIdIn(Long noticeId, Collection<Long> challengerIds);
}
