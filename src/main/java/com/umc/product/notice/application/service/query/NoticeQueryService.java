package com.umc.product.notice.application.service.query;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
import com.umc.product.notice.application.port.in.query.GetNoticeContentUseCase;
import com.umc.product.notice.application.port.in.query.GetNoticeUseCase;
import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.application.port.in.query.dto.NoticeImageInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeLinkInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusResult;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusSummary;
import com.umc.product.notice.application.port.in.query.dto.NoticeSummary;
import com.umc.product.notice.application.port.in.query.dto.NoticeVoteInfo;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeReadPort;
import com.umc.product.notice.application.port.out.LoadNoticeTargetPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.dto.NoticeClassification;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.notice.dto.NoticeTargetInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeQueryService implements GetNoticeUseCase {

    private final LoadNoticePort loadNoticePort;
    private final LoadNoticeReadPort loadNoticeReadPort;
    private final LoadNoticeTargetPort loadNoticeTargetPort;

    private final GetMemberUseCase getMemberUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetNoticeContentUseCase getNoticeContentUseCase;

    @Override
    public Page<NoticeSummary> getAllNoticeSummaries(NoticeClassification info, Pageable pageable) {
        Page<Notice> notices = loadNoticePort.findNoticesByClassification(info, pageable);
        return toNoticeSummaryPage(notices);
    }

    @Override
    public Page<NoticeSummary> searchNoticesByKeyword(String keyword, NoticeClassification classification, Pageable pageable) {
        Page<Notice> notices = loadNoticePort.findNoticesByKeyword(keyword, classification, pageable);
        return toNoticeSummaryPage(notices);
    }

    @Override
    public NoticeInfo getNoticeDetail(Long noticeId) {
        // 권한 검증은 컨트롤러에서 @CheckAccess 사용
        // Notice, NoticeContent 조회
        Notice notice = findById(noticeId);
        List<NoticeImageInfo> imageInfos = getNoticeContentUseCase.findImageByNoticeId(noticeId);
        List<NoticeLinkInfo> linkInfos = getNoticeContentUseCase.findLinkByNoticeId(noticeId);
        List<NoticeVoteInfo> voteInfos = getNoticeContentUseCase.findVoteByNoticeId(noticeId);

        // NoticeTargetInfo 조회
        NoticeTarget target = loadNoticeTargetPort.findByNoticeId(noticeId).orElse(null);
        NoticeTargetInfo targetInfo = target != null ? NoticeTargetInfo.from(target) : null;

        int viewCount = (int) loadNoticeReadPort.countReadsByNoticeId(noticeId);

        return new NoticeInfo(
            notice.getId(),
            notice.getTitle(),
            notice.getContent(),
            notice.getAuthorChallengerId(),
            voteInfos,
            imageInfos,
            linkInfos,
            targetInfo,
            viewCount,
            notice.getCreatedAt()
        );
    }

    @Override
    public NoticeReadStatusResult getReadStatus(GetNoticeStatusQuery command) {
        Notice notice = findById(command.noticeId());

        // TODO: 권한 작업하면서 진행 예정
        throw new NoticeDomainException(NoticeErrorCode.NOT_IMPLEMENTED_YET);
    }


    @Override
    public NoticeReadStatusSummary getReadStatistics(Long noticeId) {
        Notice notice = findById(noticeId);

        //TODO: 권한 작업하면서 진행 예정
        throw new NoticeDomainException(NoticeErrorCode.NOT_IMPLEMENTED_YET);
    }


    // ====== PRIVTE =====

    private Page<NoticeSummary> toNoticeSummaryPage(Page<Notice> notices) {
        NoticeQueryData queryData = fetchBatchData(notices);
        return notices.map(notice -> toNoticeSummary(notice, queryData));
    }

    // N+1문제 방지를 위해 Batch로 데이터를 정리
    private NoticeQueryData fetchBatchData(Page<Notice> notices) {
        List<Long> noticeIds = notices.getContent().stream().map(Notice::getId).toList();

        Set<Long> challengerIds = notices.getContent().stream()
            .map(Notice::getAuthorChallengerId).collect(Collectors.toSet());

        Map<Long, NoticeTarget> targetMap = loadNoticeTargetPort.findByNoticeIdIn(noticeIds).stream()
            .collect(Collectors.toMap(NoticeTarget::getNoticeId, Function.identity()));

        Map<Long, Long> viewCountMap = loadNoticeReadPort.countReadsByNoticeIds(noticeIds);

        Map<Long, ChallengerInfo> challengerMap = getChallengerUseCase.getChallengerPublicInfoByIds(challengerIds);

        Set<Long> memberIds = challengerMap.values().stream()
            .map(ChallengerInfo::memberId).collect(Collectors.toSet());

        Map<Long, MemberProfileInfo> memberMap = getMemberUseCase.getProfiles(memberIds);

        return new NoticeQueryData(targetMap, viewCountMap, challengerMap, memberMap);
    }

    // Notice를 NoticeSummary로 매핑
    private NoticeSummary toNoticeSummary(Notice notice, NoticeQueryData data) {
        ChallengerInfo challengerInfo = data.challengerMap.get(notice.getAuthorChallengerId());

        MemberProfileInfo memberInfo = challengerInfo != null
            ? data.memberMap.get(challengerInfo.memberId()) : null;

        NoticeTarget target = data.targetMap.get(notice.getId());

        NoticeTargetInfo targetInfo = target != null ? NoticeTargetInfo.from(target) : null;

        int viewCount = data.viewCountMap.getOrDefault(notice.getId(), 0L).intValue();

        return new NoticeSummary(
            notice.getId(), notice.getTitle(), notice.getContent(),
            notice.isShouldSendNotification(), viewCount, notice.getCreatedAt(),
            targetInfo, notice.getAuthorChallengerId(),
            memberInfo != null ? memberInfo.nickname() : null,
            memberInfo != null ? memberInfo.name() : null
        );
    }

    private record NoticeQueryData(
        Map<Long, NoticeTarget> targetMap,
        Map<Long, Long> viewCountMap,
        Map<Long, ChallengerInfo> challengerMap,
        Map<Long, MemberProfileInfo> memberMap
    ) {}


    private Notice findById(Long noticeId) {
        return loadNoticePort.findNoticeById(noticeId).orElseThrow(
            () -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }

}
