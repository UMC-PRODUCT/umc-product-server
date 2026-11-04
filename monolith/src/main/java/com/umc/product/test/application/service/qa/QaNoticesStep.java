package com.umc.product.test.application.service.qa;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import com.umc.product.notice.application.port.in.command.ManageNoticeReadUseCase;
import com.umc.product.notice.application.port.in.command.ManageNoticeUseCase;
import com.umc.product.notice.application.port.in.command.ManageNoticeVoteResponseUseCase;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeVoteCommand;
import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.SubmitNoticeVoteResponseCommand;
import com.umc.product.notice.application.port.in.query.GetNoticeContentUseCase;
import com.umc.product.notice.application.port.in.query.dto.NoticeVoteInfo;
import com.umc.product.notice.domain.NoticeTargetInfo;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.test.application.port.in.command.dto.QaSeedStep;
import com.umc.product.test.application.port.out.QaSeedMarkerPort;
import com.umc.product.test.domain.roster.QaPersonas;
import com.umc.product.test.domain.roster.QaPersonas.Persona;

import lombok.RequiredArgsConstructor;

/**
 * 공지, 읽음, 투표. 제목이 같은 공지가 있으면 그 공지는 건너뛴다.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class QaNoticesStep implements QaSeedStepRunner {

    private static final String CAU = "중앙대학교";
    private static final String VOTE_NOTICE_TITLE = "11기 중앙대 스터디 시간 투표";
    private static final List<PartNotice> CURRENT_PARTS = List.of(
        new PartNotice(ChallengerPart.PLAN, QaPersonas.CAU_PLAN, "기획"),
        new PartNotice(ChallengerPart.DESIGN, QaPersonas.CAU_DESIGN, "디자인"),
        new PartNotice(ChallengerPart.WEB_PRODUCT_ENGINEER, QaPersonas.CAU_WEB, "웹"),
        new PartNotice(ChallengerPart.MOBILE_PRODUCT_ENGINEER, QaPersonas.CAU_MOBILE, "모바일")
    );

    private final ManageNoticeUseCase manageNoticeUseCase;
    private final ManageNoticeReadUseCase manageNoticeReadUseCase;
    private final ManageNoticeContentUseCase manageNoticeContentUseCase;
    private final ManageNoticeVoteResponseUseCase manageNoticeVoteResponseUseCase;
    private final GetNoticeContentUseCase getNoticeContentUseCase;
    private final QaSeedMarkerPort qaSeedMarkerPort;

    @Override
    public QaSeedStep step() {
        return QaSeedStep.NOTICES;
    }

    @Override
    @Transactional
    public StepTally run(QaDirectory directory) {
        List<NoticeSpec> notices = notices(directory);
        Set<String> existing = qaSeedMarkerPort.findExistingNoticeTitles(
            notices.stream().map(NoticeSpec::title).toList());
        StepTally tally = new StepTally();
        for (NoticeSpec notice : notices) {
            if (existing.contains(notice.title())) {
                tally.skipped();
                continue;
            }
            Long noticeId = seedNotice(directory, notice);
            if (notice.label().equals(VOTE_NOTICE_TITLE)) {
                seedVote(directory, noticeId);
            }
            tally.created();
        }
        return tally;
    }

    private List<NoticeSpec> notices(QaDirectory directory) {
        Long gisuId = directory.gisuId(11);
        Long schoolId = directory.schoolId(CAU);
        List<NoticeSpec> notices = new ArrayList<>(List.of(
            new NoticeSpec("11기 전체 운영 안내", QaPersonas.CENTRAL_PRESIDENT,
                new NoticeTargetInfo(gisuId, null, null, List.of(), NoticeTab.CHALLENGER), QaPersonas.CAU_WEB, false),
            new NoticeSpec("11기 수달 지부 모임", QaPersonas.CAU_SCHOOL_PRESIDENT,
                new NoticeTargetInfo(gisuId, directory.chapterId(11, "수달"), null, List.of(), NoticeTab.CHALLENGER),
                QaPersonas.CAU_WEB, false),
            new NoticeSpec(VOTE_NOTICE_TITLE, QaPersonas.CAU_SCHOOL_PRESIDENT,
                new NoticeTargetInfo(gisuId, null, schoolId, List.of(), NoticeTab.CHALLENGER), QaPersonas.CAU_WEB,
                true),
            new NoticeSpec("11기 중앙대 운영진 안내", QaPersonas.CAU_SCHOOL_PRESIDENT,
                new NoticeTargetInfo(gisuId, null, schoolId, List.of(), NoticeTab.SCHOOL_PART_LEADER),
                QaPersonas.CAU_WEB_LEADER, false)));
        for (PartNotice part : CURRENT_PARTS) {
            notices.add(new NoticeSpec("11기 " + part.label() + " 학습 안내", QaPersonas.CENTRAL_EDUCATION,
                new NoticeTargetInfo(gisuId, null, null, List.of(part.part()), NoticeTab.CHALLENGER),
                part.learner(), false));
        }
        // 공지 작성 권한은 활성 기수 직책으로만 판단해 10기 운영진은 쓸 수 없고, superadmin만 기수와 무관하게 통과한다.
        notices.add(new NoticeSpec("10기 Xenon 활동 안내", QaPersonas.SUPERADMIN,
            new NoticeTargetInfo(directory.gisuId(10), directory.chapterId(10, "Xenon"), null, List.of(),
                NoticeTab.CHALLENGER), QaPersonas.G10_CAU_NODE, false));
        notices.add(new NoticeSpec("10기 중앙대 활동 안내", QaPersonas.SUPERADMIN,
            new NoticeTargetInfo(directory.gisuId(10), null, schoolId, List.of(), NoticeTab.CHALLENGER),
            QaPersonas.G10_CAU_SPRINGBOOT, false));
        return notices;
    }

    private Long seedNotice(QaDirectory directory, NoticeSpec notice) {
        Long noticeId = manageNoticeUseCase.createNotice(
            new CreateNoticeCommand(directory.memberId(notice.author()), notice.title(),
                "개발 환경 QA용 공지입니다. 대상 범위, 읽음 여부와 권한을 확인합니다.",
                false, notice.mustRead(), notice.target()));
        manageNoticeReadUseCase.recordRead(noticeId, directory.memberId(notice.reader()));
        return noticeId;
    }

    private void seedVote(QaDirectory directory, Long noticeId) {
        Instant now = Instant.now();
        manageNoticeContentUseCase.addVote(AddNoticeVoteCommand.builder()
            .createdMemberId(directory.memberId(QaPersonas.CAU_SCHOOL_PRESIDENT))
            .title("스터디 가능한 시간을 선택해주세요")
            .isAnonymous(false).allowMultipleChoice(false)
            .startsAt(now.minus(5, ChronoUnit.MINUTES)).endsAtExclusive(now.plus(30, ChronoUnit.DAYS))
            .options(List.of("평일 저녁", "주말 오후")).build(), noticeId);
        seedVoteResponse(noticeId, directory.memberId(QaPersonas.CAU_WEB), 0);
        Long planMemberId = directory.memberId(QaPersonas.CAU_PLAN);
        manageNoticeReadUseCase.recordRead(noticeId, planMemberId);
        seedVoteResponse(noticeId, planMemberId, 1);
    }

    private void seedVoteResponse(Long noticeId, Long memberId, int optionIndex) {
        NoticeVoteInfo vote = getNoticeContentUseCase.findVoteByNoticeId(noticeId, memberId);
        manageNoticeVoteResponseUseCase.submit(SubmitNoticeVoteResponseCommand.builder()
            .noticeId(noticeId).respondentMemberId(memberId)
            .selectedOptionIds(List.of(vote.options().get(optionIndex).optionId())).build());
    }

    private record NoticeSpec(String label, Persona author, NoticeTargetInfo target, Persona reader,
                              boolean mustRead) {
        String title() {
            return "[QA] " + label;
        }
    }

    private record PartNotice(ChallengerPart part, Persona learner, String label) {
    }
}
