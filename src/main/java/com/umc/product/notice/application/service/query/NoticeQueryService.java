package com.umc.product.notice.application.service.query;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.notice.application.port.in.query.GetNoticeContentUseCase;
import com.umc.product.notice.application.port.in.query.GetNoticeUseCase;
import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.application.port.in.query.dto.NoticeImageInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeLinkInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusResult;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusSummary;
import com.umc.product.notice.application.port.in.query.dto.NoticeSummary;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeReadPort;
import com.umc.product.notice.application.port.out.LoadNoticeTargetPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeRead;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.enums.NoticeReadStatusFilterType;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.notice.dto.NoticeClassification;
import com.umc.product.notice.dto.NoticeTargetInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.survey.application.port.in.query.dto.VoteInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final LoadNoticePort loadNoticePort;
    private final LoadNoticeReadPort loadNoticeReadPort;
    private final LoadNoticeTargetPort loadNoticeTargetPort;

    private final GetChapterUseCase getChapterUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetNoticeContentUseCase getNoticeContentUseCase;

    @Override
    public Page<NoticeSummary> getAllNoticeSummaries(NoticeClassification info, Pageable pageable) {
        Page<Notice> notices = loadNoticePort.findNoticesByClassification(info, pageable);
        return toNoticeSummaryPage(notices);
    }

    @Override
    public Page<NoticeSummary> searchNoticesByKeyword(String keyword, NoticeClassification classification,
                                                      Pageable pageable) {
        Page<Notice> notices = loadNoticePort.findNoticesByKeyword(keyword, classification, pageable);
        return toNoticeSummaryPage(notices);
    }

    @Override
    public NoticeInfo getNoticeDetail(Long noticeId, Long memberId) {
        // 권한 검증은 컨트롤러에서 @CheckAccess 사용
        // Notice, NoticeContent 조회
        Notice notice = findById(noticeId);
        List<NoticeImageInfo> imageInfos = getNoticeContentUseCase.findImageByNoticeId(noticeId);
        List<NoticeLinkInfo> linkInfos = getNoticeContentUseCase.findLinkByNoticeId(noticeId);
        VoteInfo voteInfo = getNoticeContentUseCase.findVoteByNoticeId(noticeId, memberId);

        // NoticeTargetInfo 조회
        NoticeTarget target = loadNoticeTargetPort.findByNoticeId(noticeId).orElse(null);
        NoticeTargetInfo targetInfo = target != null ? NoticeTargetInfo.from(target) : null;

        int viewCount = (int) loadNoticeReadPort.countReadsByNoticeId(noticeId);

        return new NoticeInfo(
            notice.getId(),
            notice.getTitle(),
            notice.getContent(),
            notice.getAuthorMemberId(),
            voteInfo,
            imageInfos,
            linkInfos,
            targetInfo,
            viewCount,
            notice.getCreatedAt()
        );
    }

    @Override
    public NoticeReadStatusResult getReadStatus(GetNoticeStatusQuery command) {
        TargetChallengerContext context = findTargetChallengerContext(command.noticeId());
        if (context.targetChallengers().isEmpty()) {
            return new NoticeReadStatusResult(List.of(), null, false);
        }

        // 조직 필터 적용
        List<ChallengerInfo> filteredByOrg = context.targetChallengers().stream()
            .filter(
                challenger -> isOrganizationMatch(command, challenger, context.memberMap().get(challenger.memberId()),
                    context.chapterCache()))
            .toList();

        if (filteredByOrg.isEmpty()) {
            return new NoticeReadStatusResult(List.of(), null, false);
        }

        List<NoticeRead> reads = loadNoticeReadPort.findNoticeReadByNoticeId(command.noticeId());
        Map<Long, NoticeRead> readMap = reads.stream()
            .collect(Collectors.toMap(NoticeRead::getChallengerId, Function.identity()));

        // 읽음/안읽음 상태에 따라 필터링
        List<ChallengerInfo> filteredByStatus = switch (command.status()) {
            case READ -> filteredByOrg.stream()
                .filter(challenger -> readMap.containsKey(challenger.challengerId()))
                .toList();
            case UNREAD -> filteredByOrg.stream()
                .filter(challenger -> !readMap.containsKey(challenger.challengerId()))
                .toList();
        };

        // 커서 기반 페이지네이션 적용
        int startIdx = findCursorIndex(filteredByStatus, readMap, command);

        List<ChallengerInfo> pagedChallengers = filteredByStatus.stream()
            .skip(startIdx)
            .limit(DEFAULT_PAGE_SIZE + 1)  // 다음 페이지 존재 여부 확인용
            .toList();

        boolean hasNext = pagedChallengers.size() > DEFAULT_PAGE_SIZE;
        List<ChallengerInfo> resultChallengers = hasNext
            ? pagedChallengers.subList(0, DEFAULT_PAGE_SIZE)
            : pagedChallengers;

        List<NoticeReadStatusInfo> content = resultChallengers.stream()
            .map(challenger -> toReadStatusInfo(challenger, context.memberMap().get(challenger.memberId()),
                context.chapterCache()))
            .toList();

        Long nextCursorId = content.isEmpty() ? null : switch (command.status()) {
            case READ -> readMap.get(content.get(content.size() - 1).challengerId()).getId();
            case UNREAD -> content.get(content.size() - 1).challengerId();
        };

        return new NoticeReadStatusResult(content, nextCursorId, hasNext);
    }

    @Override
    public NoticeReadStatusSummary getReadStatistics(Long noticeId) {
        TargetChallengerContext context = findTargetChallengerContext(noticeId);

        int totalCount = context.targetChallengers().size();
        if (totalCount == 0) {
            return new NoticeReadStatusSummary(0, 0, 0, 0f);
        }

        Set<Long> readChallengerIds = loadNoticeReadPort.findNoticeReadByNoticeId(noticeId).stream()
            .map(NoticeRead::getChallengerId)
            .collect(Collectors.toSet());

        int readCount = (int) context.targetChallengers().stream()
            .filter(challenger -> readChallengerIds.contains(challenger.challengerId()))
            .count();

        int unreadCount = totalCount - readCount;
        float readRate = (float) readCount / totalCount * 100;

        return new NoticeReadStatusSummary(totalCount, readCount, unreadCount, readRate);
    }

    /**
     * 공지 대상 챌린저 조회를 위한 공통 컨텍스트를 생성합니다.
     */
    private TargetChallengerContext findTargetChallengerContext(Long noticeId) {
        NoticeTarget target = loadNoticeTargetPort.findByNoticeId(noticeId)
            .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
        NoticeTargetInfo targetInfo = NoticeTargetInfo.from(target);

        List<ChallengerInfo> challengers;
        if (targetInfo.targetGisuId() != null) {
            challengers = getChallengerUseCase.getByGisuId(targetInfo.targetGisuId());
        } else {
            // 모든 기수 대상 공지: DB 쿼리에서 멤버당 최신 기수 챌린저 1건만 조회 (읽음 현황 조회 시 혼선 방지)
            challengers = getChallengerUseCase.getLatestPerMember();
        }

        if (challengers.isEmpty()) {
            return new TargetChallengerContext(List.of(), Map.of(), new HashMap<>());
        }

        Map<Long, MemberInfo> memberMap = getMemberUseCase.getProfiles(
            challengers.stream().map(ChallengerInfo::memberId).collect(Collectors.toSet())
        );

        // 필터링 전에 필요한 챕터 정보를 1번 쿼리로 선점해 캐시에 채워둠
        Set<Long> gisuIds = challengers.stream()
            .map(ChallengerInfo::gisuId)
            .collect(Collectors.toSet());

        Set<Long> schoolIds = challengers.stream()
            .map(c -> memberMap.get(c.memberId()))
            .filter(Objects::nonNull)
            .map(MemberInfo::schoolId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Map<ChapterKey, ChapterInfo> chapterCache = new HashMap<>();
        getChapterUseCase.getChapterMapByGisuIdsAndSchoolIds(gisuIds, schoolIds)
            .forEach((gId, schoolMap) ->
                schoolMap.forEach((sId, info) ->
                    chapterCache.put(new ChapterKey(gId, sId), info)
                )
            );

        List<ChallengerInfo> targetChallengers = challengers.stream()
            .filter(challenger -> isTargetChallenger(targetInfo, challenger, memberMap.get(challenger.memberId()),
                chapterCache))
            .toList();

        return new TargetChallengerContext(targetChallengers, memberMap, chapterCache);
    }

    // ====== PRIVTE =====

    private Page<NoticeSummary> toNoticeSummaryPage(Page<Notice> notices) {
        NoticeQueryData queryData = fetchBatchData(notices);
        return notices.map(notice -> toNoticeSummary(notice, queryData));
    }

    // N+1문제 방지를 위해 Batch로 데이터를 정리
    private NoticeQueryData fetchBatchData(Page<Notice> notices) {
        List<Long> noticeIds = notices.getContent().stream().map(Notice::getId).toList();

        Set<Long> authorMemberIds = notices.getContent().stream()
            .map(Notice::getAuthorMemberId).collect(Collectors.toSet());

        Map<Long, NoticeTarget> targetMap = loadNoticeTargetPort.findByNoticeIdIn(noticeIds).stream()
            .collect(Collectors.toMap(NoticeTarget::getNoticeId, Function.identity()));

        Map<Long, Long> viewCountMap = loadNoticeReadPort.countReadsByNoticeIds(noticeIds);

        Map<Long, MemberInfo> memberMap = getMemberUseCase.getProfiles(authorMemberIds);

        return new NoticeQueryData(targetMap, viewCountMap, memberMap);
    }

    // Notice를 NoticeSummary로 매핑
    private NoticeSummary toNoticeSummary(Notice notice, NoticeQueryData data) {
        MemberInfo memberInfo = data.memberMap.get(notice.getAuthorMemberId());

        NoticeTarget target = data.targetMap.get(notice.getId());

        NoticeTargetInfo targetInfo = target != null ? NoticeTargetInfo.from(target) : null;

        int viewCount = data.viewCountMap.getOrDefault(notice.getId(), 0L).intValue();

        return new NoticeSummary(
            notice.getId(), notice.getTitle(), notice.getContent(),
            notice.isShouldSendNotification(), viewCount, notice.getCreatedAt(),
            targetInfo, notice.getAuthorMemberId(),
            memberInfo != null ? memberInfo.nickname() : null,
            memberInfo != null ? memberInfo.name() : null
        );
    }

    private Notice findById(Long noticeId) {
        return loadNoticePort.findNoticeById(noticeId).orElseThrow(
            () -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }

    // ChapterInfo 캐싱용 키
    private record ChapterKey(Long gisuId, Long schoolId) {
    }

    private boolean isTargetChallenger(
        NoticeTargetInfo targetInfo,
        ChallengerInfo challenger,
        MemberInfo memberInfo,
        Map<ChapterKey, ChapterInfo> chapterCache
    ) {
        if (memberInfo == null) {
            return false;
        }

        ChapterInfo chapterInfo = getCachedChapterInfo(challenger.gisuId(), memberInfo.schoolId(), chapterCache);
        Long chapterId = chapterInfo != null ? chapterInfo.id() : null;

        return targetInfo.isTarget(
            challenger.gisuId(),
            chapterId,
            memberInfo.schoolId(),
            challenger.part()
        );
    }

    private boolean isOrganizationMatch(
        GetNoticeStatusQuery command,
        ChallengerInfo challenger,
        MemberInfo memberInfo,
        Map<ChapterKey, ChapterInfo> chapterCache
    ) {
        if (command.filterType() == null || command.filterType() == NoticeReadStatusFilterType.ALL) {
            return true;
        }

        if (memberInfo == null || command.organizationId() == null || command.organizationId().isEmpty()) {
            return false;
        }

        ChapterInfo chapterInfo = getCachedChapterInfo(challenger.gisuId(), memberInfo.schoolId(), chapterCache);
        Long chapterId = chapterInfo != null ? chapterInfo.id() : null;

        return switch (command.filterType()) {
            case SCHOOL -> command.organizationId().contains(memberInfo.schoolId());
            case CHAPTER -> chapterId != null && command.organizationId().contains(chapterId);
            case ALL -> true;
        };
    }

    private ChapterInfo getCachedChapterInfo(Long gisuId, Long schoolId, Map<ChapterKey, ChapterInfo> cache) {
        if (gisuId == null || schoolId == null) {
            return null;
        }
        return cache.computeIfAbsent(
            new ChapterKey(gisuId, schoolId),
            key -> getChapterUseCase.byGisuAndSchool(key.gisuId(), key.schoolId())
        );
    }

    private NoticeReadStatusInfo toReadStatusInfo(
        ChallengerInfo challenger,
        MemberInfo memberInfo,
        Map<ChapterKey, ChapterInfo> chapterCache
    ) {
        ChapterInfo chapterInfo = memberInfo != null
            ? getCachedChapterInfo(challenger.gisuId(), memberInfo.schoolId(), chapterCache)
            : null;

        Long chapterId = chapterInfo != null ? chapterInfo.id() : null;
        String chapterName = chapterInfo != null ? chapterInfo.name() : null;

        return new NoticeReadStatusInfo(
            challenger.challengerId(),
            memberInfo != null ? memberInfo.name() : null,
            memberInfo != null ? memberInfo.profileImageLink() : null,
            challenger.part(),
            memberInfo != null ? memberInfo.schoolId() : null,
            memberInfo != null ? memberInfo.schoolName() : null,
            chapterId,
            chapterName
        );
    }

    /**
     * 커서 위치 이후의 시작 인덱스를 찾습니다.
     */
    private int findCursorIndex(
        List<ChallengerInfo> challengers,
        Map<Long, NoticeRead> readMap,
        GetNoticeStatusQuery command
    ) {
        if (command.cursorId() == null) {
            return 0;
        }

        for (int i = 0; i < challengers.size(); i++) {
            ChallengerInfo challenger = challengers.get(i);
            Long currentCursorId = switch (command.status()) {
                case READ -> readMap.get(challenger.challengerId()).getId();
                case UNREAD -> challenger.challengerId();
            };

            if (currentCursorId.equals(command.cursorId())) {
                return i + 1;  // 커서 다음부터 시작
            }
        }

        return 0;  // 커서를 찾지 못하면 처음부터
    }

    // =============== PRIVATE records ===============

    private record NoticeQueryData(
        Map<Long, NoticeTarget> targetMap,
        Map<Long, Long> viewCountMap,
        Map<Long, MemberInfo> memberMap
    ) {
    }

    private record TargetChallengerContext(
        List<ChallengerInfo> targetChallengers,
        Map<Long, MemberInfo> memberMap,
        Map<ChapterKey, ChapterInfo> chapterCache
    ) {
    }

}
