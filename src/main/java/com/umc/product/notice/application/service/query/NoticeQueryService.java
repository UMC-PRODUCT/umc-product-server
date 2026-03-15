package com.umc.product.notice.application.service.query;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
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
import java.util.HashSet;
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
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetNoticeContentUseCase getNoticeContentUseCase;

    @Override
    public Page<NoticeSummary> getAllNoticeSummaries(Long memberId, NoticeClassification info, Pageable pageable) {
        Set<ChallengerPart> memberParts = resolveMemberParts(memberId, info.gisuId());
        NoticeClassification enriched = enrichClassificationWithPart(memberId, info);
        Page<Notice> notices = loadNoticePort.findNoticesByClassification(enriched, memberParts, pageable);
        return toNoticeSummaryPage(notices);
    }

    @Override
    public Page<NoticeSummary> searchNoticesByKeyword(Long memberId, String keyword,
                                                      NoticeClassification classification,
                                                      Pageable pageable) {
        Set<ChallengerPart> memberParts = resolveMemberParts(memberId, classification.gisuId());
        NoticeClassification enriched = enrichClassificationWithPart(memberId, classification);
        Page<Notice> notices = loadNoticePort.findNoticesByKeyword(keyword, enriched, memberParts, pageable);
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

        return new NoticeInfo(
            notice.getId(),
            notice.getTitle(),
            notice.getContent(),
            notice.getAuthorMemberId(),
            voteInfo,
            imageInfos,
            linkInfos,
            targetInfo,
            notice.getViewCount(),
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
        List<Long> targetIds = findTargetChallengerIds(noticeId);

        int totalCount = targetIds.size();
        if (totalCount == 0) {
            return new NoticeReadStatusSummary(0, 0, 0, 0f);
        }

        int readCount = (int) loadNoticeReadPort.countReadsByChallengerIdIn(noticeId, targetIds);
        int unreadCount = totalCount - readCount;
        float readRate = (float) readCount / totalCount * 100;

        return new NoticeReadStatusSummary(totalCount, readCount, unreadCount, readRate);
    }

    // ====== PRIVATE =====

    /**
     * 공지 대상 챌린저 조회를 위한 공통 컨텍스트를 생성합니다.
     */
    private TargetChallengerContext findTargetChallengerContext(Long noticeId) {
        NoticeTarget target = loadNoticeTargetPort.findByNoticeId(noticeId)
            .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
        NoticeTargetInfo targetInfo = NoticeTargetInfo.from(target);

        List<ChallengerInfo> challengers;

        // 전체 기수 공지 여부
        if (targetInfo.targetGisuId() != null) {
            challengers = getChallengerUseCase.getByGisuIdWithoutPoints(targetInfo.targetGisuId());
        } else {
            // 모든 기수 대상 공지: DB 쿼리에서 멤버당 최신 기수 챌린저 1건만 조회 (읽음 현황 조회 시 혼선 방지)
            challengers = getChallengerUseCase.getLatestPerMemberWithoutPoints();
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
            .forEach((gisuId, schoolMap) ->
                schoolMap.forEach((schoolId, info) ->
                    chapterCache.put(new ChapterKey(gisuId, schoolId), info)
                )
            );

        // 조건에 맞는 챌린저 필터링
        List<ChallengerInfo> targetChallengers = challengers.stream()
            .filter(challenger -> isTargetChallenger(targetInfo, challenger, memberMap.get(challenger.memberId()),
                chapterCache))
            .toList();

        return new TargetChallengerContext(targetChallengers, memberMap, chapterCache);
    }

    /**
     * 공지 대상 챌린저 ID 목록을 반환합니다. (경량 조회)
     * <p>
     * 통계 집계처럼 ID만 필요한 경우 사용합니다.
     */
    private List<Long> findTargetChallengerIds(Long noticeId) {
        NoticeTarget target = loadNoticeTargetPort.findByNoticeId(noticeId)
            .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
        NoticeTargetInfo targetInfo = NoticeTargetInfo.from(target);

        List<ChallengerInfo> challengers;
        if (targetInfo.targetGisuId() != null) {
            challengers = getChallengerUseCase.getByGisuIdWithoutPoints(targetInfo.targetGisuId());
        } else {
            challengers = getChallengerUseCase.getLatestPerMemberWithoutPoints();
        }

        if (challengers.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> schoolIdMap = getMemberUseCase.getSchoolIds(
            challengers.stream().map(ChallengerInfo::memberId).collect(Collectors.toSet())
        );

        Set<Long> gisuIds = challengers.stream()
            .map(ChallengerInfo::gisuId)
            .collect(Collectors.toSet());

        Set<Long> schoolIds = schoolIdMap.values().stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Map<ChapterKey, ChapterInfo> chapterCache = new HashMap<>();
        getChapterUseCase.getChapterMapByGisuIdsAndSchoolIds(gisuIds, schoolIds)
            .forEach((gisuId, schoolMap) ->
                schoolMap.forEach((schoolId, info) ->
                    chapterCache.put(new ChapterKey(gisuId, schoolId), info)
                )
            );

        return challengers.stream()
            .filter(challenger -> isTargetChallenger(targetInfo, challenger,
                schoolIdMap.get(challenger.memberId()), chapterCache))
            .map(ChallengerInfo::challengerId)
            .toList();
    }


    /**
     * 지부/학교/파트 필터 요청 시, 호출자의 실제 소속 정보로 chapterId/schoolId를 덮어씁니다. 모든 필터가 없으면 그대로 반환합니다.
     * <p>
     * 클라이언트가 넘긴 chapterId/schoolId는 "이 필터 종류로 조회"라는 의도로만 사용하고, 실제 ID 값은 인증된 사용자 프로필에서 직접 조회합니다.
     */
    private NoticeClassification enrichClassificationWithPart(Long memberId, NoticeClassification classification) {
        if (classification.chapterId() == null
            && classification.schoolId() == null
            && classification.part() == null) {
            return classification;
        }

        Long gisuId = classification.gisuId();

        MemberInfo memberInfo = getMemberUseCase.getProfiles(Set.of(memberId)).get(memberId);
        Long schoolId = memberInfo != null ? memberInfo.schoolId() : null;

        Long chapterId = null;
        if (schoolId != null) {
            try {
                ChapterInfo chapterInfo = getChapterUseCase.byGisuAndSchool(gisuId, schoolId);
                chapterId = chapterInfo.id();
            } catch (Exception e) {
                log.debug("지부 정보 조회 실패 - gisuId={}, schoolId={}: {}", gisuId, schoolId, e.getMessage());
            }
        }

        // 클라이언트가 요청한 필터 종류는 유지하되, 실제 값은 사용자 프로필에서만 가져옴
        // 파트 필터일 때는 chapterId/schoolId가 없어도 실제 유저 값으로 채움
        boolean hasPart = classification.part() != null;
        Long filteredChapterId = (classification.chapterId() != null || hasPart) ? chapterId : null;
        Long filteredSchoolId = (classification.schoolId() != null || hasPart) ? schoolId : null;

        return new NoticeClassification(gisuId, filteredChapterId, filteredSchoolId, classification.part());
    }

    /**
     * 해당 기수에서 멤버가 볼 수 있는 파트 목록을 조회합니다. - Challenger.part: 챌린저 본인의 소속 파트 - ChallengerRole.responsiblePart: 파트장 역할에서 담당하는
     * 파트 챌린저 정보가 없으면 빈 Set을 반환하여 파트 조건 없는 공지만 노출합니다.
     */
    private Set<ChallengerPart> resolveMemberParts(Long memberId, Long gisuId) {
        if (memberId == null || gisuId == null) {
            return Set.of();
        }

        ChallengerInfo challenger = getChallengerUseCase.getByMemberIdAndGisuId(memberId, gisuId);

        Set<ChallengerPart> parts = new HashSet<>();
        parts.add(challenger.part());
        parts.addAll(getChallengerRoleUseCase.getResponsiblePartsByMemberAndGisu(memberId, gisuId));
        return parts;
    }

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

        Map<Long, MemberInfo> memberMap = getMemberUseCase.getProfiles(authorMemberIds);

        return new NoticeQueryData(targetMap, memberMap);
    }

    // Notice를 NoticeSummary로 매핑
    private NoticeSummary toNoticeSummary(Notice notice, NoticeQueryData data) {
        MemberInfo memberInfo = data.memberMap.get(notice.getAuthorMemberId());

        NoticeTarget target = data.targetMap.get(notice.getId());

        NoticeTargetInfo targetInfo = target != null ? NoticeTargetInfo.from(target) : null;

        long viewCount = notice.getViewCount();

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

    private boolean isTargetChallenger(
        NoticeTargetInfo targetInfo,
        ChallengerInfo challenger,
        Long schoolId,
        Map<ChapterKey, ChapterInfo> chapterCache
    ) {
        if (schoolId == null) {
            return false;
        }

        ChapterInfo chapterInfo = getCachedChapterInfo(challenger.gisuId(), schoolId, chapterCache);
        Long chapterId = chapterInfo != null ? chapterInfo.id() : null;

        return targetInfo.isTarget(
            challenger.gisuId(),
            chapterId,
            schoolId,
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
