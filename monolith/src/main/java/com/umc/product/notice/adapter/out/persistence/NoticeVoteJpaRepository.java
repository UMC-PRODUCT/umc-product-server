package com.umc.product.notice.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.notice.domain.NoticeVote;

import jakarta.persistence.LockModeType;

public interface NoticeVoteJpaRepository extends JpaRepository<NoticeVote, Long> {

    Optional<NoticeVote> findByNoticeId(Long noticeId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT nv FROM NoticeVote nv WHERE nv.notice.id = :noticeId")
    Optional<NoticeVote> findByNoticeIdForShare(@Param("noticeId") Long noticeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT nv FROM NoticeVote nv WHERE nv.notice.id = :noticeId")
    Optional<NoticeVote> findByNoticeIdForUpdate(@Param("noticeId") Long noticeId);

    boolean existsByNoticeId(Long noticeId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM NoticeVote nv WHERE nv.notice.id = :noticeId")
    void deleteAllByNoticeId(@Param("noticeId") Long noticeId);
}
