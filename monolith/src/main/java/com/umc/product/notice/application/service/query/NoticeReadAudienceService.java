package com.umc.product.notice.application.service.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.query.ListChallengerRoleUseCase;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SubjectAttributes.GisuChallengerInfo;
import com.umc.product.authorization.domain.SystemRoleType;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.ListMemberSystemRoleUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberBasicInfo;
import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.application.port.out.LoadNoticeReadPort;
import com.umc.product.notice.application.port.out.LoadNoticeTargetPort;
import com.umc.product.notice.application.service.NoticeReadPolicy;
import com.umc.product.notice.domain.NoticeRead;
import com.umc.product.notice.domain.NoticeTargetInfo;
import com.umc.product.notice.domain.enums.NoticeReadStatus;
import com.umc.product.notice.domain.enums.NoticeReadStatusFilterType;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeReadAudienceService {

    private final LoadNoticeTargetPort loadNoticeTargetPort;
    private final LoadNoticeReadPort loadNoticeReadPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final ListChallengerRoleUseCase listChallengerRoleUseCase;
    private final ListMemberSystemRoleUseCase listMemberSystemRoleUseCase;
    private final GetChapterUseCase getChapterUseCase;

    public List<Long> listUnreadRecipientMemberIds(Long noticeId, Set<Long> requestedChallengerIds) {
        return listUnreadRecipientMemberIds(noticeId, requestedChallengerIds::contains);
    }

    public List<Long> listAllUnreadRecipientMemberIds(Long noticeId) {
        return listUnreadRecipientMemberIds(noticeId, id -> true);
    }

    private List<Long> listUnreadRecipientMemberIds(Long noticeId, Predicate<Long> includesChallenger) {
        return listReaders(noticeId).stream()
            .filter(reader -> reader.readId() == null)
            .filter(reader -> includesChallenger.test(reader.challenger().challengerId()))
            .map(reader -> reader.challenger().memberId())
            .toList();
    }

    List<Reader> listReaders(Long noticeId) {
        NoticeTargetInfo target = loadNoticeTargetPort.findByNoticeId(noticeId)
            .map(NoticeTargetInfo::from)
            .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
        Map<Long, List<RoleAttribute>> roles = listChallengerRoleUseCase
            .mapRolesByMemberIncludingCentralCore(target.targetGisuId());
        Set<Long> admins = listMemberSystemRoleUseCase.listSuperAdminMemberIds();
        // 다른 기수의 총괄단이나 시스템 관리자도 기수 공지를 열람할 수 있다.
        Set<Long> staffMemberIds = new HashSet<>(roles.keySet());
        staffMemberIds.addAll(admins);
        Map<Long, List<ChallengerBasicInfo>> byMember = getChallengerUseCase
            .getHistoriesForActiveOrGraduatedMembers(target.targetGisuId(), staffMemberIds);
        List<ChallengerBasicInfo> challengers = byMember.values().stream().flatMap(List::stream).toList();
        Map<Long, MemberBasicInfo> members = getMemberUseCase.findAllBasicByIds(byMember.keySet());
        Map<Long, Map<Long, ChapterInfo>> chapters = loadChapters(target, challengers, members);
        Map<Long, Long> readIds = readIdsByMember(noticeId, challengers);

        List<Reader> readers = new ArrayList<>();
        for (MemberBasicInfo member : members.values()) {
            List<ChallengerBasicInfo> history = byMember.get(member.memberId());
            List<GisuChallengerInfo> gisus = history.stream().map(c -> new GisuChallengerInfo(
                c.gisuId(), chapterId(chapters, c.gisuId(), member.schoolId()), c.part(), c.challengerId())).toList();
            SubjectAttributes subject = new SubjectAttributes(member.memberId(), member.schoolId(), gisus,
                roles.getOrDefault(member.memberId(), List.of()),
                admins.contains(member.memberId()) ? Set.of(SystemRoleType.SUPER_ADMIN) : Set.of());
            if (NoticeReadPolicy.canRead(subject, target,
                gisuId -> chapterId(chapters, gisuId, target.targetSchoolId()))) {
                // 명단에는 대상 기수 이력을 우선 표시하고, 없으면 가장 최근 기수 이력을 표시한다.
                ChallengerBasicInfo display = history.stream().filter(c -> c.gisuId().equals(target.targetGisuId()))
                    .findFirst().orElse(history.getFirst());
                ChapterInfo chapter = chapter(chapters, display.gisuId(), member.schoolId());
                readers.add(new Reader(display, member.schoolId(), chapter, readIds.get(member.memberId())));
            }
        }
        return readers;
    }

    private Map<Long, Map<Long, ChapterInfo>> loadChapters(NoticeTargetInfo target,
                                                           List<ChallengerBasicInfo> challengers,
                                                           Map<Long, MemberBasicInfo> members) {
        Set<Long> gisus = challengers.stream().map(ChallengerBasicInfo::gisuId).collect(Collectors.toSet());
        Set<Long> schools = members.values().stream().map(MemberBasicInfo::schoolId)
            .filter(Objects::nonNull).collect(Collectors.toSet());
        if (target.targetSchoolId() != null) {
            schools.add(target.targetSchoolId());
        }
        return getChapterUseCase.getChapterMapByGisuIdsAndSchoolIds(gisus, schools);
    }

    private static ChapterInfo chapter(Map<Long, Map<Long, ChapterInfo>> chapters, Long gisuId, Long schoolId) {
        return schoolId == null ? null : chapters.getOrDefault(gisuId, Map.of()).get(schoolId);
    }

    private static Long chapterId(Map<Long, Map<Long, ChapterInfo>> chapters, Long gisuId, Long schoolId) {
        ChapterInfo chapter = chapter(chapters, gisuId, schoolId);
        return chapter == null ? null : chapter.id();
    }

    private Map<Long, Long> readIdsByMember(Long noticeId, List<ChallengerBasicInfo> challengers) {
        List<NoticeRead> reads = loadNoticeReadPort.findNoticeReadByNoticeId(noticeId);
        Map<Long, Long> owners = challengers.stream()
            .collect(Collectors.toMap(ChallengerBasicInfo::challengerId, ChallengerBasicInfo::memberId));
        Set<Long> otherIds = reads.stream().map(NoticeRead::getChallengerId)
            .filter(id -> !owners.containsKey(id)).collect(Collectors.toSet());
        for (ChallengerBasicInfo other : getChallengerUseCase.listBasicByIds(otherIds)) {
            owners.put(other.challengerId(), other.memberId());
        }
        Map<Long, Long> firstReadByMember = new HashMap<>();
        for (NoticeRead read : reads) {
            Long memberId = owners.get(read.getChallengerId());
            if (memberId != null) {
                firstReadByMember.merge(memberId, read.getId(), Math::min);
            }
        }
        return firstReadByMember;
    }

    record Reader(ChallengerBasicInfo challenger, Long schoolId, ChapterInfo chapter, Long readId) {

        boolean matches(GetNoticeStatusQuery query) {
            if ((query.status() == NoticeReadStatus.READ) != (readId != null)) {
                return false;
            }
            if (query.filterType() == null || query.filterType() == NoticeReadStatusFilterType.ALL) {
                return true;
            }
            Long organizationId = query.filterType() == NoticeReadStatusFilterType.SCHOOL
                ? schoolId : (chapter == null ? null : chapter.id());
            return organizationId != null && query.organizationId() != null
                && query.organizationId().contains(organizationId);
        }

        long cursor() {
            return readId == null ? challenger.challengerId() : readId;
        }
    }
}
