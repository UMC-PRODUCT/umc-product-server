package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.domain.Notice;
import java.awt.print.Pageable;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeJpaRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByAuthorChallengerId(Long challengerId);

    List<Notice> findAll(Pageable pageable);


}
