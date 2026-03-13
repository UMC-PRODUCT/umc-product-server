package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.domain.NoticeRead;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeReadJpaRepository extends JpaRepository<NoticeRead, Long> {
    List<NoticeRead> findAllByNoticeId(Long noticeId);

    @Modifying
    @Query("DELETE FROM NoticeRead nr WHERE nr.notice.id = :noticeId")
    void deleteAllByNoticeId(@Param("noticeId") Long noticeId);

    boolean existsByNoticeIdAndChallengerId(Long noticeId, Long challengerId);

    int countByNoticeId(Long noticeId);

    long countByNoticeIdAndChallengerIdIn(Long noticeId, Collection<Long> challengerIds);
}
