package com.umc.product.notice.adapter.out.persistence;

import com.umc.product.notice.application.port.out.LoadNoticeImagePort;
import com.umc.product.notice.application.port.out.LoadNoticeLinkPort;
import com.umc.product.notice.application.port.out.LoadNoticeVotePort;
import com.umc.product.notice.application.port.out.SaveNoticeImagePort;
import com.umc.product.notice.application.port.out.SaveNoticeLinkPort;
import com.umc.product.notice.application.port.out.SaveNoticeVotePort;
import com.umc.product.notice.domain.NoticeImage;
import com.umc.product.notice.domain.NoticeLink;
import com.umc.product.notice.domain.NoticeVote;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoticeContentPersistenceAdapter implements
        SaveNoticeImagePort,
        SaveNoticeVotePort,
        SaveNoticeLinkPort,
        LoadNoticeImagePort,
        LoadNoticeVotePort,
        LoadNoticeLinkPort {

    private final NoticeVoteJpaRepository voteJpaRepository;
    private final NoticeLinkJpaRepository linkJpaRepository;
    private final NoticeImageJpaRepository imageJpaRepository;


    @Override
    public Optional<NoticeImage> findImageById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<List<NoticeImage>> findImagesByNoticeId(Long noticeId) {
        return Optional.empty();
    }

    @Override
    public boolean existsImageByNoticeId(Long noticeId) {
        return false;
    }

    @Override
    public Optional<NoticeLink> findLinkById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<List<NoticeLink>> findLinksByNoticeId(Long noticeId) {
        return Optional.empty();
    }

    @Override
    public boolean existsLinkByNoticeId(Long noticeId) {
        return false;
    }

    @Override
    public Optional<NoticeVote> findVoteById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<List<NoticeVote>> findVotesByNoticeId(Long noticeId) {
        return Optional.empty();
    }

    @Override
    public boolean existsVoteByNoticeId(Long noticeId) {
        return false;
    }

    @Override
    public NoticeImage saveImage(NoticeImage noticeImage) {
        return null;
    }

    @Override
    public List<NoticeImage> saveAllImages(List<NoticeImage> noticeImages) {
        return null;
    }

    @Override
    public void deleteImage(NoticeImage noticeImage) {

    }

    @Override
    public NoticeLink saveLink(NoticeLink noticeLink) {
        return null;
    }

    @Override
    public List<NoticeLink> saveAllLinks(List<NoticeLink> noticeLinks) {
        return null;
    }

    @Override
    public void deleteLink(NoticeLink noticeLink) {

    }

    @Override
    public NoticeVote saveVote(NoticeVote noticeVote) {
        return null;
    }

    @Override
    public List<NoticeVote> saveAllVotes(List<NoticeVote> noticeVotes) {
        return null;
    }

    @Override
    public void deleteVote(NoticeVote noticeVote) {

    }
}
