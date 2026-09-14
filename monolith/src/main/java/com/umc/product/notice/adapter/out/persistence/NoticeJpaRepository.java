package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.domain.Notice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeJpaRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByAuthorMemberId(Long memberId);

    @Modifying
    @Query("UPDATE Notice n SET n.viewCount = n.viewCount + 1 WHERE n.id = :noticeId")
    void incrementViewCount(@Param("noticeId") Long noticeId);
}
