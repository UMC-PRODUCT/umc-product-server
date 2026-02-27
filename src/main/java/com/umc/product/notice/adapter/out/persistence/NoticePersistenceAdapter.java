package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeReadPort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeReadPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeRead;
import com.umc.product.notice.dto.NoticeClassification;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoticePersistenceAdapter implements
    LoadNoticePort,
    SaveNoticePort,
    LoadNoticeReadPort,
    SaveNoticeReadPort {

    private final NoticeJpaRepository noticeJpaRepository;
    private final NoticeReadJpaRepository noticeReadJpaRepository;
    private final NoticeQueryRepository noticeQueryRepository;

    @Override
    public Optional<Notice> findNoticeById(Long id) {
        return noticeJpaRepository.findById(id);
    }

    @Override
    public Page<Notice> findNoticesByClassification(NoticeClassification classification, Pageable pageable) {
        return noticeQueryRepository.findByClassification(classification, pageable);
    }

    @Override
    public Page<Notice> findNoticesByKeyword(String keyword, NoticeClassification noticeClassification, Pageable pageable) {
        return noticeQueryRepository.findByKeyword(keyword, noticeClassification, pageable);
    }

    @Override
    public Page<Notice> findAllNotices(Pageable pageable) {
        return noticeJpaRepository.findAll(pageable);
    }


    @Override
    public Notice save(Notice notice) {
        return noticeJpaRepository.save(notice);
    }

    @Override
    public void delete(Notice notice) {
        noticeJpaRepository.delete(notice);
    }

    @Override
    public List<NoticeRead> findNoticeReadByNoticeId(Long noticeId) {
        return noticeReadJpaRepository.findAllByNoticeId(noticeId);
    }

    @Override
    public List<Long> findUnreadChallengerIdByNoticeId(Long noticeId) {
        return noticeQueryRepository.findUnreadChallengerIdByNoticeId(noticeId);
    }

    @Override
    public boolean existsRead(Long noticeId, Long challengerId) {
        return noticeReadJpaRepository.existsByNoticeIdAndChallengerId(noticeId, challengerId);
    }

    @Override
    public long countReadsByNoticeId(Long noticeId) {
        return noticeReadJpaRepository.countByNoticeId(noticeId);
    }

    @Override
    public Map<Long, Long> countReadsByNoticeIds(List<Long> noticeIds) {
        return noticeQueryRepository.countReadsByNoticeIds(noticeIds);
    }

    @Override
    public NoticeRead saveRead(NoticeRead noticeRead) {
        return noticeReadJpaRepository.save(noticeRead);
    }

    @Override
    public void deleteRead(NoticeRead noticeRead) {
        noticeReadJpaRepository.delete(noticeRead);
    }
}
