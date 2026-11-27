package com.umc.product.notice.application.service.query;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.notice.application.port.in.query.GetNoticeContentUseCase;
import com.umc.product.notice.application.port.in.query.GetNoticeUseCase;
import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.application.port.in.query.dto.NoticeAuthorInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeImageInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeLinkInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusResult;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusSummary;
import com.umc.product.notice.application.port.in.query.dto.NoticeSummary;
import com.umc.product.notice.application.port.in.query.dto.NoticeViewerInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeVoteInfo;
import com.umc.product.notice.application.port.out.LoadNoticeImagePort;
import com.umc.product.notice.application.port.out.LoadNoticeLinkPort;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeTargetPort;
import com.umc.product.notice.application.port.out.LoadNoticeVotePort;
import com.umc.product.notice.application.service.query.NoticeReadAudienceService.Reader;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeClassification;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.NoticeTargetInfo;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.notice.domain.enums.NoticeTargetPattern;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeQueryService implements GetNoticeUseCase {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final LoadNoticePort loadNoticePort;
    private final LoadNoticeTargetPort loadNoticeTargetPort;
    private final LoadNoticeImagePort loadNoticeImagePort;
    private final LoadNoticeLinkPort loadNoticeLinkPort;
    private final LoadNoticeVotePort loadNoticeVotePort;

    private final GetMemberUseCase getMemberUseCase;
    private final GetNoticeContentUseCase getNoticeContentUseCase;
    private final NoticeAuthorQueryService noticeAuthorQueryService;
    private final NoticeReadAudienceService noticeReadAudienceService;

    @Override
    public Page<NoticeSummary> getAllNoticeSummaries(NoticeViewerInfo viewerInfo, NoticeClassification classification,
                                                     Pageable pageable) {
        validateClassification(classification, viewerInfo);
        NoticeClassification enriched = enrichClassification(viewerInfo, classification);
        Page<Notice> notices = loadNoticePort.findNoticesByClassification(enriched, viewerInfo, pageable);
        return toNoticeSummaryPage(notices);
    }

    @Override
    public Page<NoticeSummary> searchNoticesByKeyword(String keyword, NoticeViewerInfo viewerInfo,
                                                      NoticeClassification classification,
                                                      Pageable pageable) {
        validateClassification(classification, viewerInfo);
        NoticeClassification enriched = enrichClassification(viewerInfo, classification);
        Page<Notice> notices = loadNoticePort.findNoticesByKeyword(keyword, enriched, viewerInfo, pageable);
        return toNoticeSummaryPage(notices);
    }

    @Override
    public NoticeInfo getNoticeDetail(Long noticeId, Long memberId) {
        // 권한 검증은 컨트롤러에서 @CheckAccess 사용
        // Notice, NoticeContent 조회
        Notice notice = findById(noticeId);
        List<NoticeImageInfo> imageInfos = getNoticeContentUseCase.findImageByNoticeId(noticeId);
        List<NoticeLinkInfo> linkInfos = getNoticeContentUseCase.findLinkByNoticeId(noticeId);
        NoticeVoteInfo voteInfo = getNoticeContentUseCase.findVoteByNoticeId(noticeId, memberId);

        // NoticeTargetInfo 조회
        NoticeTarget target = loadNoticeTargetPort.findByNoticeId(noticeId).orElse(null);
        NoticeTargetInfo targetInfo = target != null ? NoticeTargetInfo.from(target) : null;

        return new NoticeInfo(
            notice.getId(),
            notice.getTitle(),
            notice.getContent(),
            notice.getAuthorMemberId(),
            notice.isMustRead(),
            notice.isShouldSendNotification(),
            voteInfo,
            imageInfos,
            linkInfos,
            targetInfo,
            notice.getViewCount(),
            notice.getCreatedAt(),
            notice.getUpdatedAt(),
            noticeAuthorQueryService.getAll(getMemberUseCase.findAllByIds(Set.of(notice.getAuthorMemberId())))
                .get(notice.getAuthorMemberId())
        );
    }

    @Override
    public NoticeReadStatusResult getReadStatus(GetNoticeStatusQuery query) {
        List<Reader> page = noticeReadAudienceService.listReaders(query.noticeId()).stream()
            .filter(reader -> reader.matches(query))
            .filter(reader -> query.cursorId() == null || reader.cursor() > query.cursorId())
            .sorted(Comparator.comparingLong(Reader::cursor))
            .limit(DEFAULT_PAGE_SIZE + 1)
            .toList();
        boolean hasNext = page.size() > DEFAULT_PAGE_SIZE;
        List<Reader> readers = hasNext ? page.subList(0, DEFAULT_PAGE_SIZE) : page;
        Map<Long, MemberInfo> members = getMemberUseCase.findAllByIds(readers.stream()
            .map(reader -> reader.challenger().memberId()).collect(Collectors.toSet()));

        List<NoticeReadStatusInfo> content = readers.stream()
            .filter(reader -> members.containsKey(reader.challenger().memberId()))
            .map(reader -> {
                MemberInfo member = members.get(reader.challenger().memberId());
                return new NoticeReadStatusInfo(reader.challenger().challengerId(), member.name(),
                    member.profileImageLink(), reader.challenger().part(), member.schoolId(), member.schoolName(),
                    reader.chapter() == null ? null : reader.chapter().id(),
                    reader.chapter() == null ? null : reader.chapter().name());
            }).toList();
        Long nextCursor = readers.isEmpty() ? null : readers.getLast().cursor();
        return new NoticeReadStatusResult(content, nextCursor, hasNext);
    }

    @Override
    public NoticeReadStatusSummary getReadStatistics(Long noticeId) {
        List<Reader> readers = noticeReadAudienceService.listReaders(noticeId);
        int total = readers.size();
        int read = (int) readers.stream().filter(reader -> reader.readId() != null).count();
        return new NoticeReadStatusSummary(total, read, total - read, total == 0 ? 0f : 100f * read / total);
    }

    /**
     * 조회 범위(지부/학교) 권한 검증.
     * <p>
     * 총괄/중앙운영진(CENTRAL_MEMBER)은 타 학교/지부 공지까지 조회할 수 있습니다. 지부장은 타 지부의 챌린저 공지도 조회할 수 있으나,
     * 학교 전용 챌린저 공지는 담당 지부 소속 학교로 제한합니다. 그 외 역할은 본인 소속 지부/학교 범위만 조회할 수 있습니다.
     * 목록 조회와 상세 조회(@CheckAccess)의 접근 범위를 일치시킵니다.
     */
    private void validateViewerScope(NoticeClassification classification, NoticeViewerInfo viewerInfo) {
        if (viewerInfo.viewerRole() == NoticeTab.CENTRAL_MEMBER) {
            return;
        }
        if (classification.chapterId() != null
            && !classification.chapterId().equals(viewerInfo.chapterId())
            && !(classification.isChallengerQuery() && viewerInfo.isChapterPresident())) {
            throw new NoticeDomainException(NoticeErrorCode.NO_READ_PERMISSION,
                "본인 소속 지부의 공지만 조회할 수 있습니다.");
        }
        if (classification.schoolId() != null
            && !classification.schoolId().equals(viewerInfo.schoolId())
            && !(classification.isChallengerQuery() && viewerInfo.isChapterPresident()
                && viewerInfo.chapterSchoolIds().contains(classification.schoolId()))) {
            throw new NoticeDomainException(NoticeErrorCode.NO_READ_PERMISSION,
                "본인 소속 학교의 공지만 조회할 수 있습니다.");
        }
    }

    private void validateClassification(NoticeClassification classification, NoticeViewerInfo viewerInfo) {
        validateViewerScope(classification, viewerInfo);
        if (classification.isChallengerQuery()) {
            // 챌린저 공지: 필드 조합 유효성 검증 (지부+학교 동시 지정 등 불가 조합 차단)
            NoticeTargetPattern.from(classification.toTargetInfo());
        } else {
            if (viewerInfo.isChapterPresident() && classification.schoolId() != null) {
                throw new NoticeDomainException(NoticeErrorCode.NO_READ_PERMISSION,
                    "지부장은 학교를 지정하지 않은 중앙 운영진 공지만 조회할 수 있습니다.");
            }
            // 운영진 공지: 조회자 역할이 요청 역할 이상인지 검증
            NoticeTab viewerRole = viewerInfo.viewerRole();
            if (viewerRole == null) {
                throw new NoticeDomainException(NoticeErrorCode.NO_READ_PERMISSION,
                    "운영진 공지를 조회할 권한이 없습니다.");
            }
            if (!classification.noticeTab().includes(viewerRole)) {
                throw new NoticeDomainException(NoticeErrorCode.NO_READ_PERMISSION,
                    "조회자의 역할이 요청한 공지 대상 역할보다 낮습니다.");
            }
        }
    }

    /**
     * 챌린저 공지 한정으로 파트 필터 시 지부/학교 ID를 보완합니다.
     * 지부장은 타 지부도 조회할 수 있으므로 권한 검증을 통과한 필터를 그대로 사용합니다.
     * <p>
     * - chapterId/schoolId가 이미 명시된 경우: 그대로 사용 (총괄단 등이 특정 범위 지정 가능) - 챌린저 공지에서 part만 있고 chapterId/schoolId 없는 경우:
     * viewerInfo로 보완 - 운영진 공지는 schoolId를 자동 보완하지 않습니다. schoolId 유무로 중앙공지/교내공지를 명확히 구분하므로, 반드시 클라이언트가 명시해야 합니다.
     */
    private NoticeClassification enrichClassification(NoticeViewerInfo viewerInfo,
                                                      NoticeClassification classification) {
        if (classification.isChallengerQuery() && viewerInfo.isChapterPresident()) {
            return classification;
        }
        if (classification.chapterId() == null
            && classification.schoolId() == null
            && classification.part() == null) {
            return classification;
        }

        boolean hasPart = classification.part() != null;
        boolean isChallengerQuery = classification.isChallengerQuery();

        Long filteredChapterId = classification.chapterId() != null
            ? classification.chapterId()
            : (hasPart && isChallengerQuery ? viewerInfo.chapterId() : null);
        Long filteredSchoolId = classification.schoolId() != null
            ? classification.schoolId()
            : (hasPart && isChallengerQuery ? viewerInfo.schoolId() : null);

        return new NoticeClassification(classification.gisuId(), filteredChapterId, filteredSchoolId,
            classification.part(), classification.noticeTab(), classification.upmsNoticeType());
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

        Map<Long, MemberInfo> memberMap = getMemberUseCase.findAllByIds(authorMemberIds);

        Set<Long> noticeIdsWithImages = new HashSet<>(loadNoticeImagePort.listNoticeIdsWithImages(noticeIds));
        Set<Long> noticeIdsWithLinks = new HashSet<>(loadNoticeLinkPort.listNoticeIdsWithLinks(noticeIds));
        Set<Long> noticeIdsWithVotes = new HashSet<>(loadNoticeVotePort.listNoticeIdsWithVotes(noticeIds));

        return new NoticeQueryData(targetMap, memberMap, noticeIdsWithImages, noticeIdsWithLinks, noticeIdsWithVotes,
            noticeAuthorQueryService.getAll(memberMap));
    }

    // Notice를 NoticeSummary로 매핑
    private NoticeSummary toNoticeSummary(Notice notice, NoticeQueryData data) {
        MemberInfo memberInfo = data.memberMap.get(notice.getAuthorMemberId());

        NoticeTarget target = data.targetMap.get(notice.getId());

        NoticeTargetInfo targetInfo = target != null ? NoticeTargetInfo.from(target) : null;

        long viewCount = notice.getViewCount();

        return new NoticeSummary(
            notice.getId(), notice.getTitle(), notice.getContent(),
            notice.isShouldSendNotification(), notice.isMustRead(), viewCount, notice.getCreatedAt(),
            targetInfo, notice.getAuthorMemberId(),
            memberInfo != null ? memberInfo.nickname() : null,
            memberInfo != null ? memberInfo.name() : null,
            data.noticeIdsWithImages().contains(notice.getId()),
            data.noticeIdsWithLinks().contains(notice.getId()),
            data.noticeIdsWithVotes().contains(notice.getId()),
            data.authorByMemberId().get(notice.getAuthorMemberId())
        );
    }

    private Notice findById(Long noticeId) {
        return loadNoticePort.findNoticeById(noticeId).orElseThrow(
            () -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }

    private record NoticeQueryData(
        Map<Long, NoticeTarget> targetMap,
        Map<Long, MemberInfo> memberMap,
        Set<Long> noticeIdsWithImages,
        Set<Long> noticeIdsWithLinks,
        Set<Long> noticeIdsWithVotes,
        Map<Long, NoticeAuthorInfo> authorByMemberId
    ) {
    }
}
