package com.umc.product.test.application.service;

import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerCommand;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.command.RegisterEmailMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.test.application.port.in.command.SeedChallengersUseCase;
import com.umc.product.test.application.port.in.command.dto.SeedChallengersCommand;
import com.umc.product.test.application.port.in.command.dto.SeedChallengersResult;
import com.umc.product.test.application.port.in.command.dto.SeedChallengersResult.PerCellSummary;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 챌린저 분포 시딩 서비스. ADR-017 참조.
 * <p>
 * (Chapter, School, Part) 셀별로 더미 회원과 챌린저를 함께 생성한다. 셀 단위로 try-catch 를 두어
 * 한 셀의 실패가 다른 셀 시딩을 막지 않는다. Hexagonal 원칙을 따라 다른 도메인의 UseCase 만 호출한다.
 */
@Slf4j
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ChallengerSeedService implements SeedChallengersUseCase {

    private static final Set<ChallengerPart> DEFAULT_PARTS = Arrays.stream(ChallengerPart.values())
        .filter(p -> p != ChallengerPart.ADMIN)
        .collect(Collectors.toUnmodifiableSet());

    private final DummyMemberFactory dummyMemberFactory;
    private final GetMemberUseCase getMemberUseCase;
    private final RegisterEmailMemberUseCase registerEmailMemberUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChapterUseCase getChapterUseCase;
    private final ManageChallengerUseCase manageChallengerUseCase;

    @Override
    public SeedChallengersResult seed(SeedChallengersCommand command) {
        Long gisuId = resolveGisuId(command.gisuId());
        List<ChallengerPart> parts = resolveParts(command.parts());
        List<ChapterWithSchoolsInfo> chapters = resolveChapters(gisuId, command.chapterIds());

        long startedAt = System.currentTimeMillis();
        log.info(
            "challenger seed start: gisuId={}, chapters={}, parts={}, countPerCell={}",
            gisuId, chapters.size(), parts.size(), command.countPerPartPerSchool()
        );

        AtomicLong sequence = new AtomicLong(getMemberUseCase.countAll() + 1);
        List<TermConsents> consents = dummyMemberFactory.snapshotMandatoryConsents();
        List<PerCellSummary> summaries = new ArrayList<>();
        int totalCreated = 0;
        int totalFailed = 0;

        for (ChapterWithSchoolsInfo chapter : chapters) {
            for (ChapterWithSchoolsInfo.SchoolInfo school : chapter.schools()) {
                for (ChallengerPart part : parts) {
                    PerCellSummary summary = seedCell(
                        chapter.chapterId(),
                        school.schoolId(),
                        part,
                        gisuId,
                        command.countPerPartPerSchool(),
                        sequence,
                        consents
                    );
                    summaries.add(summary);
                    totalCreated += summary.created();
                    totalFailed += summary.totalFailed();
                }
            }
        }

        long elapsedMs = System.currentTimeMillis() - startedAt;
        log.info(
            "challenger seed completed in {}ms: gisuId={}, totalCreated={}, totalFailed={}",
            elapsedMs, gisuId, totalCreated, totalFailed
        );

        return new SeedChallengersResult(gisuId, totalCreated, totalFailed, summaries);
    }

    private Long resolveGisuId(Long gisuId) {
        if (gisuId != null) {
            return gisuId;
        }
        return getGisuUseCase.getActiveGisuId();
    }

    private List<ChallengerPart> resolveParts(Collection<ChallengerPart> parts) {
        if (parts == null || parts.isEmpty()) {
            return List.copyOf(DEFAULT_PARTS);
        }
        return List.copyOf(parts);
    }

    private List<ChapterWithSchoolsInfo> resolveChapters(Long gisuId, List<Long> chapterIds) {
        List<ChapterWithSchoolsInfo> all = getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId);
        if (chapterIds == null || chapterIds.isEmpty()) {
            return all;
        }
        Set<Long> filter = Set.copyOf(chapterIds);
        return all.stream().filter(c -> filter.contains(c.chapterId())).toList();
    }

    /**
     * 한 (Chapter, School, Part) 셀을 시딩한다. 셀 단위 try-catch 로 한 셀의 실패가 다른 셀로
     * 전파되지 않게 한다. 멤버 생성은 per-call 트랜잭션, 챌린저 생성은 bulk 단일 트랜잭션이다.
     * 실패 단계(멤버 / 챌린저)를 분리해서 보고하므로 호출자는 응답만 보고도 실패 지점을 추적할 수 있다.
     */
    private PerCellSummary seedCell(
        Long chapterId,
        Long schoolId,
        ChallengerPart part,
        Long gisuId,
        int countPerCell,
        AtomicLong sequence,
        List<TermConsents> consents
    ) {
        if (countPerCell <= 0) {
            return new PerCellSummary(chapterId, schoolId, part, 0, 0, 0);
        }
        List<com.umc.product.member.application.port.in.command.dto.EmailRegisterMemberCommand> memberCommands =
            new ArrayList<>(countPerCell);
        for (int i = 0; i < countPerCell; i++) {
            long seq = sequence.getAndIncrement();
            memberCommands.add(dummyMemberFactory.nextEmailCommandWithSchool(seq, schoolId, consents));
        }

        List<Long> createdMemberIds;
        int memberFailed;
        try {
            createdMemberIds = registerEmailMemberUseCase.batchRegister(memberCommands);
            memberFailed = countPerCell - createdMemberIds.size();
        } catch (Exception e) {
            log.error(
                "challenger seed member batchRegister failed (chapterId={}, schoolId={}, part={}, count={}): {}",
                chapterId, schoolId, part, countPerCell, e.toString()
            );
            return new PerCellSummary(chapterId, schoolId, part, 0, countPerCell, 0);
        }

        if (createdMemberIds.isEmpty()) {
            return new PerCellSummary(chapterId, schoolId, part, 0, memberFailed, 0);
        }

        List<CreateChallengerCommand> commands = createdMemberIds.stream()
            .map(memberId -> CreateChallengerCommand.builder()
                .memberId(memberId)
                .part(part)
                .gisuId(gisuId)
                .build())
            .toList();
        try {
            List<Long> challengerIds = manageChallengerUseCase.createChallengerBulk(commands);
            return new PerCellSummary(chapterId, schoolId, part, challengerIds.size(), memberFailed, 0);
        } catch (Exception e) {
            log.error(
                "challenger seed challenger bulk failed (chapterId={}, schoolId={}, part={}, members={}): {}",
                chapterId, schoolId, part, createdMemberIds.size(), e.toString()
            );
            return new PerCellSummary(chapterId, schoolId, part, 0, memberFailed, createdMemberIds.size());
        }
    }
}
