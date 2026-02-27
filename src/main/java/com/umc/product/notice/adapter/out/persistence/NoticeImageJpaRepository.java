package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.domain.NoticeImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeImageJpaRepository extends JpaRepository<NoticeImage, Long> {

    List<NoticeImage> findByNoticeId(Long noticeId);

    boolean existsByNoticeId(Long noticeId);

    int countByNotice_Id(Long noticeId);

    @Modifying
    @Query("DELETE FROM NoticeImage ni WHERE ni.notice.id = :noticeId")
    void deleteAllByNoticeId(@Param("noticeId") Long noticeId);

}
