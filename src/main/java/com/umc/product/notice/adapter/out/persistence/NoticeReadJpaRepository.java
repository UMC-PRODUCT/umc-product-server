package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.domain.NoticeRead;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeReadJpaRepository extends JpaRepository<NoticeRead, Long> {
    List<NoticeRead> findAllByNoticeId(Long noticeId);

    boolean existsByNoticeIdAndChallengerId(Long noticeId, Long challengerId);

    int countByNoticeId(Long noticeId);
}
