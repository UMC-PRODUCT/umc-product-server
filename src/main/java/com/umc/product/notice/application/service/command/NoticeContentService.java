package com.umc.product.notice.application.service.command;

import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeImagesCommand;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeLinksCommand;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeVotesCommand;
import com.umc.product.notice.application.port.in.command.dto.RemoveNoticeImageCommand;
import com.umc.product.notice.application.port.in.command.dto.RemoveNoticeLinkCommand;
import com.umc.product.notice.application.port.in.command.dto.RemoveNoticeVoteCommand;
import com.umc.product.notice.application.port.out.LoadNoticeImagePort;
import com.umc.product.notice.application.port.out.LoadNoticeLinkPort;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeVotePort;
import com.umc.product.notice.application.port.out.SaveNoticeImagePort;
import com.umc.product.notice.application.port.out.SaveNoticeLinkPort;
import com.umc.product.notice.application.port.out.SaveNoticeVotePort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeImage;
import com.umc.product.notice.domain.NoticeLink;
import com.umc.product.notice.domain.NoticeVote;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeContentService implements ManageNoticeContentUseCase {
    private final LoadNoticeVotePort loadNoticeVotePort;
    private final LoadNoticeLinkPort loadNoticeLinkPort;
    private final LoadNoticeImagePort loadNoticeImagePort;
    private final SaveNoticeVotePort saveNoticeVotePort;
    private final SaveNoticeImagePort saveNoticeImagePort;
    private final SaveNoticeLinkPort saveNoticeLinkPort;

    private final LoadNoticePort loadNoticePort;

    @Override
    public List<Long> addVotes(AddNoticeVotesCommand command, Long noticeId) {
        Notice notice = findNoticeById(noticeId);

        if (command.voteIds() == null || command.voteIds().isEmpty()) {
            throw new NoticeDomainException(NoticeErrorCode.VOTE_IDS_REQUIRED);
        }

        AtomicInteger order = new AtomicInteger(0);
        List<NoticeVote> votes = command.voteIds().stream()
                .map(voteId -> NoticeVote.create(voteId, notice, order.getAndIncrement()))
                .toList()
        ;

        List<NoticeVote> savedVotes = saveNoticeVotePort.saveAllVotes(votes);

        return savedVotes.stream()
                .map(NoticeVote::getId)
                .toList();
    }

    @Override
    public List<Long> addImages(AddNoticeImagesCommand command, Long noticeId) {
        Notice notice = findNoticeById(noticeId);

        if (command.imageIds() == null || command.imageIds().isEmpty()) {
            throw new NoticeDomainException(NoticeErrorCode.IMAGE_URLS_REQUIRED);
        }

        AtomicInteger order = new AtomicInteger(0);
        List<NoticeImage> images = command.imageIds().stream()
                .map(imgId -> NoticeImage.create(imgId, notice, order.getAndIncrement()))
                .toList()
        ;

        List<NoticeImage> savedImages = saveNoticeImagePort.saveAllImages(images);
        return savedImages.stream()
                .map(NoticeImage::getId)
                .toList();
    }

    @Override
    public List<Long> addLinks(AddNoticeLinksCommand command, Long noticeId) {
        Notice notice = findNoticeById(noticeId);

        if (command.links() == null || command.links().isEmpty()) {
            throw new NoticeDomainException(NoticeErrorCode.LINK_URLS_REQUIRED);
        }

        AtomicInteger order = new AtomicInteger(0);
        List<NoticeLink> links = command.links().stream()
                .map(link -> NoticeLink.create(link, notice, order.getAndIncrement()))
                .toList()
        ;

        List<NoticeLink> savedLinks = saveNoticeLinkPort.saveAllLinks(links);
        return savedLinks.stream()
                .map(NoticeLink::getId)
                .toList();
    }

    @Override
    public void removeVote(RemoveNoticeVoteCommand command) {
        NoticeVote noticeVote = loadNoticeVotePort.findVoteById(command.noticeVoteId())
                .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_VOTE_NOT_FOUND));

        saveNoticeVotePort.deleteVote(noticeVote);
    }

    @Override
    public void removeImage(RemoveNoticeImageCommand command) {
        NoticeImage noticeImage = loadNoticeImagePort.findImageById(command.noticeImageId())
                .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_IMAGE_NOT_FOUND));

        saveNoticeImagePort.deleteImage(noticeImage);
    }

    @Override
    public void removeLink(RemoveNoticeLinkCommand command) {
        NoticeLink noticeLink = loadNoticeLinkPort.findLinkById(command.noticeLinkId())
                .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_LINK_NOT_FOUND));

        saveNoticeLinkPort.deleteLink(noticeLink);
    }

    private Notice findNoticeById(Long noticeId) {
        return loadNoticePort.findNoticeById(noticeId)
                .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }
}
