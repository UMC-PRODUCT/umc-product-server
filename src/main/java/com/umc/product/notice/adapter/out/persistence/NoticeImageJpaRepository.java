package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.domain.NoticeImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeImageJpaRepository extends JpaRepository<NoticeImage, Long> {

    Optional<List<NoticeImage>> findByNoticeId(Long noticeId);
    boolean existsByNoticeId(Long noticeId);


}
