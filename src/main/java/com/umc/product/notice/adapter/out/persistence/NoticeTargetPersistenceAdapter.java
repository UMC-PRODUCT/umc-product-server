package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.application.port.out.LoadNoticeTargetPort;
import com.umc.product.notice.application.port.out.ManageNoticeTargetPort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.domain.NoticeTarget;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * NoticePermission Persistence Adapter
 * <p>
 * Load, Save, Manage Port를 모두 구현합니다.
 */
@Component
@RequiredArgsConstructor
public class NoticeTargetPersistenceAdapter implements
    LoadNoticeTargetPort,
    SaveNoticeTargetPort,
    ManageNoticeTargetPort {

    private final NoticeTargetJpaRepository repository;

    // ========== LoadNoticePermissionPort ==========

    @Override
    public Optional<NoticeTarget> findByNoticeId(Long noticeId) {
        return repository.findByNoticeId(noticeId);
    }

    @Override
    public List<NoticeTarget> findByNoticeIdIn(List<Long> noticeIds) {
        return repository.findByNoticeIdIn(noticeIds);
    }

    @Override
    public List<NoticeTarget> findByTargetGisuId(Long gisuId) {
        return repository.findByTargetGisuId(gisuId);
    }

    @Override
    public List<NoticeTarget> findByTargetChapterId(Long chapterId) {
        return repository.findByTargetChapterId(chapterId);
    }

    @Override
    public List<NoticeTarget> findByTargetSchoolId(Long schoolId) {
        return repository.findByTargetSchoolId(schoolId);
    }

    @Override
    public boolean existsByNoticeId(Long noticeId) {
        return repository.existsByNoticeId(noticeId);
    }

    // ========== SaveNoticePermissionPort ==========

    @Override
    public NoticeTarget save(NoticeTarget noticeTarget) {
        return repository.save(noticeTarget);
    }

    // ========== ManageNoticePermissionPort ==========

    @Override
    public void delete(NoticeTarget noticeTarget) {
        repository.delete(noticeTarget);
    }

    @Override
    public void deleteByNoticeId(Long noticeId) {
        repository.deleteByNoticeId(noticeId);
    }
}
