package com.umc.product.test.application.service;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.application.port.in.command.ManageNoticeUseCase;
import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.test.application.port.in.command.SeedNoticeUseCase;
import com.umc.product.test.application.port.in.command.dto.SeedNoticeCommand;
import com.umc.product.test.application.port.in.command.dto.SeedNoticeResult;
import com.umc.product.test.application.port.in.command.dto.SeedNoticeResult.ScopeSummary;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Notice 시딩 서비스. ADR-017 참조.
 * <p>
 * 활성 기수(또는 지정 기수)에 대해 다음 4 가지 scope 로 공지를 분포 시딩한다.
 * <pre>
 *  GLOBAL  — 기수 전체 대상   (SPECIFIC_GISU_ALL_TARGET, 작성자 isCentralCore 필요)
 *  CHAPTER — 지부별           (SPECIFIC_GISU_SPECIFIC_CHAPTER, 작성자 isChapterPresidentInGisu 필요)
 *  SCHOOL  — 학교별           (SPECIFIC_GISU_SPECIFIC_SCHOOL, 작성자 isSchoolCoreInGisu 필요)
 *  PART    — 파트별           (SPECIFIC_GISU_SPECIFIC_PART, 작성자 isCentralMemberInGisu 필요)
 * </pre>
 * 작성자 권한이 부족한 scope 는 도메인이 NO_WRITE_PERMISSION 예외를 던지므로 scope 단위로
 * try-catch 격리되어 다른 scope 시딩은 진행된다. 운영자는 응답의 {@code scopeBreakdown}
 * 으로 어떤 scope 가 권한 부족 등으로 실패했는지 확인할 수 있다.
 * <p>
 * Hexagonal 원칙을 따라 다른 도메인의 UseCase 만 호출한다. 본 서비스의 {@code seed()} 는
 * {@link Propagation#NOT_SUPPORTED} 로 외부 트랜잭션을 차단해 각 createNotice 호출이
 * 독립 트랜잭션으로 커밋되도록 한다 (실패 격리).
 */
@Slf4j
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class NoticeSeedService implements SeedNoticeUseCase {

    private static final String SCOPE_GLOBAL = "GLOBAL";
    private static final String SCOPE_CHAPTER = "CHAPTER";
    private static final String SCOPE_SCHOOL = "SCHOOL";
    private static final String SCOPE_PART = "PART";

    private static final List<ChallengerPart> DEFAULT_PARTS = Arrays.stream(ChallengerPart.values())
        .filter(p -> p != ChallengerPart.ADMIN)
        .toList();

    private final DummyNoticeFactory dummyNoticeFactory;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChapterUseCase getChapterUseCase;
    private final ManageNoticeUseCase manageNoticeUseCase;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public SeedNoticeResult seed(SeedNoticeCommand command) {
        Long gisuId = command.gisuId() != null ? command.gisuId() : getGisuUseCase.getActiveGisuId();
        Long authorMemberId = command.authorMemberId();
        List<ChallengerPart> parts = resolveParts(command.parts());

        long startedAt = System.currentTimeMillis();
        log.info(
            "notice seed start: gisuId={}, authorMemberId={}, global={}, perChapter={}, perSchool={}, perPart={}",
            gisuId, authorMemberId,
            command.globalCount(), command.perChapterCount(), command.perSchoolCount(), command.perPartCount()
        );

        List<ChapterWithSchoolsInfo> chapters = getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId);

        List<Long> createdIds = new ArrayList<>();
        List<ScopeSummary> breakdown = new ArrayList<>();

        breakdown.add(seedGlobal(gisuId, authorMemberId, command.globalCount(), createdIds));
        breakdown.add(seedChapters(gisuId, authorMemberId, chapters, command.perChapterCount(), createdIds));
        breakdown.add(seedSchools(gisuId, authorMemberId, chapters, command.perSchoolCount(), createdIds));
        breakdown.add(seedParts(gisuId, authorMemberId, parts, command.perPartCount(), createdIds));

        int totalCreated = breakdown.stream().mapToInt(ScopeSummary::created).sum();
        int totalFailed = breakdown.stream().mapToInt(ScopeSummary::failed).sum();

        long elapsedMs = System.currentTimeMillis() - startedAt;
        log.info(
            "notice seed completed in {}ms: totalCreated={}, totalFailed={}, breakdown={}",
            elapsedMs, totalCreated, totalFailed, breakdown
        );

        return new SeedNoticeResult(
            gisuId, authorMemberId, createdIds, breakdown,
            totalCreated, totalFailed, false, null
        );
    }

    private List<ChallengerPart> resolveParts(Collection<ChallengerPart> parts) {
        if (parts == null || parts.isEmpty()) {
            return DEFAULT_PARTS;
        }
        return parts.stream().filter(p -> p != ChallengerPart.ADMIN).toList();
    }

    private ScopeSummary seedGlobal(Long gisuId, Long authorMemberId, int target, List<Long> createdIds) {
        if (target <= 0) {
            return new ScopeSummary(SCOPE_GLOBAL, 0, 0, 0);
        }
        int created = 0;
        int failed = 0;
        for (int i = 1; i <= target; i++) {
            final int seq = i;
            Long noticeId = tryCreate(SCOPE_GLOBAL,
                () -> dummyNoticeFactory.nextGlobalNoticeCommand(gisuId, authorMemberId, seq));
            if (noticeId != null) {
                createdIds.add(noticeId);
                created++;
            } else {
                failed++;
            }
        }
        return new ScopeSummary(SCOPE_GLOBAL, target, created, failed);
    }

    private ScopeSummary seedChapters(
        Long gisuId, Long authorMemberId, List<ChapterWithSchoolsInfo> chapters, int perChapter, List<Long> createdIds
    ) {
        if (perChapter <= 0 || chapters.isEmpty()) {
            return new ScopeSummary(SCOPE_CHAPTER, 0, 0, 0);
        }
        int attempted = 0;
        int created = 0;
        int failed = 0;
        for (ChapterWithSchoolsInfo chapter : chapters) {
            for (int i = 1; i <= perChapter; i++) {
                final int seq = i;
                attempted++;
                Long noticeId = tryCreate(SCOPE_CHAPTER,
                    () -> dummyNoticeFactory.nextChapterNoticeCommand(
                        gisuId, authorMemberId, chapter.chapterId(), chapter.chapterName(), seq));
                if (noticeId != null) {
                    createdIds.add(noticeId);
                    created++;
                } else {
                    failed++;
                }
            }
        }
        return new ScopeSummary(SCOPE_CHAPTER, attempted, created, failed);
    }

    private ScopeSummary seedSchools(
        Long gisuId, Long authorMemberId, List<ChapterWithSchoolsInfo> chapters, int perSchool, List<Long> createdIds
    ) {
        if (perSchool <= 0) {
            return new ScopeSummary(SCOPE_SCHOOL, 0, 0, 0);
        }
        int attempted = 0;
        int created = 0;
        int failed = 0;
        for (ChapterWithSchoolsInfo chapter : chapters) {
            for (ChapterWithSchoolsInfo.SchoolInfo school : chapter.schools()) {
                for (int i = 1; i <= perSchool; i++) {
                    final int seq = i;
                    attempted++;
                    Long noticeId = tryCreate(SCOPE_SCHOOL,
                        () -> dummyNoticeFactory.nextSchoolNoticeCommand(
                            gisuId, authorMemberId, school.schoolId(), school.schoolName(), seq));
                    if (noticeId != null) {
                        createdIds.add(noticeId);
                        created++;
                    } else {
                        failed++;
                    }
                }
            }
        }
        return new ScopeSummary(SCOPE_SCHOOL, attempted, created, failed);
    }

    private ScopeSummary seedParts(
        Long gisuId, Long authorMemberId, List<ChallengerPart> parts, int perPart, List<Long> createdIds
    ) {
        if (perPart <= 0 || parts.isEmpty()) {
            return new ScopeSummary(SCOPE_PART, 0, 0, 0);
        }
        int attempted = 0;
        int created = 0;
        int failed = 0;
        for (ChallengerPart part : parts) {
            for (int i = 1; i <= perPart; i++) {
                final int seq = i;
                attempted++;
                Long noticeId = tryCreate(SCOPE_PART,
                    () -> dummyNoticeFactory.nextPartNoticeCommand(gisuId, authorMemberId, part, seq));
                if (noticeId != null) {
                    createdIds.add(noticeId);
                    created++;
                } else {
                    failed++;
                }
            }
        }
        return new ScopeSummary(SCOPE_PART, attempted, created, failed);
    }

    private Long tryCreate(String scope, Supplier<CreateNoticeCommand> commandSupplier) {
        try {
            return manageNoticeUseCase.createNotice(commandSupplier.get());
        } catch (Exception e) {
            log.error("notice seed {} create failed: {}", scope, e.toString());
            return null;
        }
    }
}
