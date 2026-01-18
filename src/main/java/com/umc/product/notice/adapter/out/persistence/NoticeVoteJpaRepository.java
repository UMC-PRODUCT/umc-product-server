package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.domain.NoticeVote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeVoteJpaRepository extends JpaRepository<NoticeVote, Long> {

    Optional<List<NoticeVote>> findByNoticeId(Long noticeId);
    boolean existsByNoticeId(Long noticeId);
}
