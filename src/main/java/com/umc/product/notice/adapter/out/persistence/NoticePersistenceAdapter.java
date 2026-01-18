package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeRead;
import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoticePersistenceAdapter implements LoadNoticePort, SaveNoticePort {

    private final NoticeJpaRepository noticeJpaRepository;
    private final NoticeReadJpaRepository noticeReadJpaRepository;

    @Override
    public Optional<Notice> findNoticeById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Notice> loadAllNotices(Pageable pageable) {
        return null;
    }

    @Override
    public List<NoticeRead> findNoticeReadsByNoticeId(Long noticeId) {
        return null;
    }

    @Override
    public boolean existsNoticeRead(Long noticeId, Long challengerId) {
        return false;
    }

    @Override
    public long countNoticeReadsByNoticeId(Long noticeId) {
        return 0;
    }

    @Override
    public Notice save(Notice notice) {
        return null;
    }

    @Override
    public void delete(Notice notice) {

    }
}
