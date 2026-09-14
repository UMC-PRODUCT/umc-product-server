package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.domain.NoticeTarget;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * NoticePermission JPA Repository
 */
public interface NoticeTargetJpaRepository extends JpaRepository<NoticeTarget, Long> {

    /**
     * 공지사항 ID로 권한 정보 조회
     */
    Optional<NoticeTarget> findByNoticeId(Long noticeId);

    /**
     * 여러 공지사항의 권한 정보 조회
     */
    List<NoticeTarget> findByNoticeIdIn(List<Long> noticeIds);

    /**
     * 특정 기수를 대상으로 하는 공지사항 권한 조회
     */
    List<NoticeTarget> findByTargetGisuId(Long gisuId);

    /**
     * 특정 지부를 대상으로 하는 공지사항 권한 조회
     */
    List<NoticeTarget> findByTargetChapterId(Long chapterId);

    /**
     * 특정 학교를 대상으로 하는 공지사항 권한 조회
     */
    List<NoticeTarget> findByTargetSchoolId(Long schoolId);

    /**
     * 공지사항 ID 존재 여부 확인
     */
    boolean existsByNoticeId(Long noticeId);

    /**
     * 공지사항 ID로 권한 정보 삭제
     */
    void deleteByNoticeId(Long noticeId);
}
