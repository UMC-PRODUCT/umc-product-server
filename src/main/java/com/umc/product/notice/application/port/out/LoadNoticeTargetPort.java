package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.NoticeTarget;
import java.util.List;
import java.util.Optional;

/**
 * NoticePermission 조회 Port
 */
public interface LoadNoticeTargetPort {

    /**
     * 공지사항 ID로 권한 정보 조회
     *
     * @param noticeId 공지사항 ID
     * @return NoticePermission (없으면 Optional.empty())
     */
    Optional<NoticeTarget> findByNoticeId(Long noticeId);

    /**
     * 여러 공지사항의 권한 정보를 한 번에 조회
     *
     * @param noticeIds 공지사항 ID 목록
     * @return NoticePermission 리스트
     */
    List<NoticeTarget> findByNoticeIdIn(List<Long> noticeIds);

    /**
     * 특정 기수를 대상으로 하는 공지사항 권한 조회
     *
     * @param gisuId 기수 ID
     * @return NoticePermission 리스트
     */
    List<NoticeTarget> findByTargetGisuId(Long gisuId);

    /**
     * 특정 지부를 대상으로 하는 공지사항 권한 조회
     *
     * @param chapterId 지부 ID
     * @return NoticePermission 리스트
     */
    List<NoticeTarget> findByTargetChapterId(Long chapterId);

    /**
     * 특정 학교를 대상으로 하는 공지사항 권한 조회
     *
     * @param schoolId 학교 ID
     * @return NoticePermission 리스트
     */
    List<NoticeTarget> findByTargetSchoolId(Long schoolId);

    /**
     * 공지사항 ID 존재 여부 확인
     *
     * @param noticeId 공지사항 ID
     * @return 존재하면 true
     */
    boolean existsByNoticeId(Long noticeId);
}
