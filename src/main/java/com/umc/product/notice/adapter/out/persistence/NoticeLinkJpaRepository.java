package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.domain.NoticeLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeLinkJpaRepository extends JpaRepository<NoticeLink, Long> {
    List<NoticeLink> findByNoticeId(Long noticeId);

    boolean existsByNoticeId(Long noticeId);

    int countByNotice_Id(Long noticeId);

    @Modifying
    @Query("DELETE FROM NoticeLink nl WHERE nl.notice.id = :noticeId")
    void deleteAllByNoticeId(@Param("noticeId") Long noticeId);
}
