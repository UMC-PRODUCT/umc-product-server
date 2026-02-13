package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.domain.NoticeVote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeVoteJpaRepository extends JpaRepository<NoticeVote, Long> {

    List<NoticeVote> findByNoticeId(Long noticeId);

    boolean existsByNoticeId(Long noticeId);

    @Modifying
    @Query("DELETE FROM NoticeVote nv WHERE nv.notice.id = :noticeId")
    void deleteAllByNoticeId(@Param("noticeId") Long noticeId);
}
