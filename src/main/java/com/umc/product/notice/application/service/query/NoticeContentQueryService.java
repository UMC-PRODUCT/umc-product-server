package com.umc.product.notice.application.service.query;


import com.umc.product.notice.application.port.in.query.GetNoticeContentUseCase;
import com.umc.product.notice.application.port.in.query.dto.NoticeImageInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeLinkInfo;
import com.umc.product.notice.application.port.out.LoadNoticeImagePort;
import com.umc.product.notice.application.port.out.LoadNoticeLinkPort;
import com.umc.product.notice.application.port.out.LoadNoticeVotePort;
import com.umc.product.notice.domain.NoticeImage;
import com.umc.product.notice.domain.NoticeLink;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.survey.application.port.in.query.GetVoteDetailUseCase;
import com.umc.product.survey.application.port.in.query.dto.GetVoteDetailsQuery;
import com.umc.product.survey.application.port.in.query.dto.VoteInfo;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeContentQueryService implements GetNoticeContentUseCase {

    private final LoadNoticeVotePort loadNoticeVotePort;
    private final LoadNoticeImagePort loadNoticeImagePort;
    private final LoadNoticeLinkPort loadNoticeLinkPort;
    private final GetVoteDetailUseCase getVoteDetailUseCase;
    private final GetFileUseCase getFileUseCase;

    @Override
    public List<NoticeLinkInfo> findLinkByNoticeId(Long noticeId) {

        List<NoticeLink> links = loadNoticeLinkPort.findLinksByNoticeId(noticeId);
        return links.stream()
            .sorted(Comparator.comparing(NoticeLink::getDisplayOrder))
            .map(link -> new NoticeLinkInfo(
                link.getId(),
                link.getLink(),
                link.getDisplayOrder()
            ))
            .toList();
    }

    @Override
    public VoteInfo findVoteByNoticeId(Long noticeId, Long memberId) {
        return loadNoticeVotePort.findVoteByNoticeId(noticeId)
            .map(vote -> getVoteDetailUseCase.get(new GetVoteDetailsQuery(vote.getVoteId(), memberId)))
            .orElse(null);
    }

    @Override
    public List<NoticeImageInfo> findImageByNoticeId(Long noticeId) {
        List<NoticeImage> images = loadNoticeImagePort.findImagesByNoticeId(noticeId);

        List<String> imageIds = images.stream()
            .map(NoticeImage::getImageId)
            .toList();

        Map<String, String> fileLinks = getFileUseCase.getFileLinks(imageIds);

        return images.stream()
            .sorted(Comparator.comparing(NoticeImage::getDisplayOrder))
            .map(image -> new NoticeImageInfo(
                image.getId(),
                fileLinks.get(image.getImageId()),
                image.getDisplayOrder()
            ))
            .toList();
    }
}
