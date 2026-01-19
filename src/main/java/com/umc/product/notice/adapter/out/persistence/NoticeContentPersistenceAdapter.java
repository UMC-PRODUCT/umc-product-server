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
        return imageJpaRepository.findById(id);
    }

    @Override
    public Optional<List<NoticeImage>> findImagesByNoticeId(Long noticeId) {
        return imageJpaRepository.findByNoticeId(noticeId);
    }

    @Override
    public boolean existsImageByNoticeId(Long noticeId) {
        return imageJpaRepository.existsByNoticeId(noticeId);
    }

    @Override
    public Optional<NoticeLink> findLinkById(Long id) {
        return linkJpaRepository.findById(id);
    }

    @Override
    public Optional<List<NoticeLink>> findLinksByNoticeId(Long noticeId) {
        return linkJpaRepository.findByNoticeId(noticeId);
    }

    @Override
    public boolean existsLinkByNoticeId(Long noticeId) {
        return linkJpaRepository.existsByNoticeId(noticeId);
    }

    @Override
    public Optional<NoticeVote> findVoteById(Long id) {
        return voteJpaRepository.findById(id);
    }

    @Override
    public Optional<List<NoticeVote>> findVotesByNoticeId(Long noticeId) {
        return voteJpaRepository.findByNoticeId(noticeId);
    }

    @Override
    public boolean existsVoteByNoticeId(Long noticeId) {
        return voteJpaRepository.existsByNoticeId(noticeId);
    }

    @Override
    public NoticeImage saveImage(NoticeImage noticeImage) {
        return imageJpaRepository.save(noticeImage);
    }

    @Override
    public List<NoticeImage> saveAllImages(List<NoticeImage> noticeImages) {
        return imageJpaRepository.saveAll(noticeImages);
    }

    @Override
    public void deleteImage(NoticeImage noticeImage) {
        imageJpaRepository.delete(noticeImage);
    }

    @Override
    public NoticeLink saveLink(NoticeLink noticeLink) {
        return linkJpaRepository.save(noticeLink);
    }

    @Override
    public List<NoticeLink> saveAllLinks(List<NoticeLink> noticeLinks) {
        return linkJpaRepository.saveAll(noticeLinks);
    }

    @Override
    public void deleteLink(NoticeLink noticeLink) {
        linkJpaRepository.delete(noticeLink);
    }

    @Override
    public NoticeVote saveVote(NoticeVote noticeVote) {
        return voteJpaRepository.save(noticeVote);
    }

    @Override
    public List<NoticeVote> saveAllVotes(List<NoticeVote> noticeVotes) {
        return voteJpaRepository.saveAll(noticeVotes);
    }

    @Override
    public void deleteVote(NoticeVote noticeVote) {
        voteJpaRepository.delete(noticeVote);
    }
}
