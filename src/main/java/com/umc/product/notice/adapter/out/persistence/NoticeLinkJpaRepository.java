package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.domain.NoticeLink;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeLinkJpaRepository extends JpaRepository<NoticeLink, Long> {
    Optional<List<NoticeLink>> findByNoticeId(Long noticeId);
    boolean existsByNoticeId(Long noticeId);
}
