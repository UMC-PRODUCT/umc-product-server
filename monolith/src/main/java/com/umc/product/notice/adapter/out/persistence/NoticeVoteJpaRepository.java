package com.umc.product.notice.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.notice.domain.NoticeVote;

public interface NoticeVoteJpaRepository extends JpaRepository<NoticeVote, Long> {

    Optional<NoticeVote> findByNoticeId(Long noticeId);

    boolean existsByNoticeId(Long noticeId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM NoticeVote nv WHERE nv.notice.id = :noticeId")
    void deleteAllByNoticeId(@Param("noticeId") Long noticeId);
}
