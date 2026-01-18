package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.domain.NoticeRead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeReadJpaRepository extends JpaRepository<NoticeRead, Long> {

}
