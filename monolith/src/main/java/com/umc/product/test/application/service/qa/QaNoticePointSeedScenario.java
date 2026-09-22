package com.umc.product.test.application.service.qa;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.GrantChallengerPointCommand;
import com.umc.product.challenger.domain.enums.PointType;
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

import lombok.RequiredArgsConstructor;

@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Transactional(propagation = Propagation.MANDATORY)
@RequiredArgsConstructor
public class QaNoticePointSeedScenario {

    private static final List<PartNotice> CURRENT_PARTS = List.of(
        new PartNotice(ChallengerPart.PLAN, "cau_g11_plan", "기획"),
        new PartNotice(ChallengerPart.DESIGN, "cau_g11_design", "디자인"),
        new PartNotice(ChallengerPart.WEB_PRODUCT_ENGINEER, "cau_g11_web", "웹"),
        new PartNotice(ChallengerPart.MOBILE_PRODUCT_ENGINEER, "cau_g11_mobile", "모바일")
    );
    private static final List<String> LEGACY_LEARNERS = List.of(
        "cau_plan", "cau_design", "cau_web", "cau_android", "cau_ios", "cau_node", "cau_springboot"
    );

    private final ManageNoticeUseCase manageNoticeUseCase;
    private final ManageNoticeReadUseCase manageNoticeReadUseCase;
    private final ManageNoticeContentUseCase manageNoticeContentUseCase;
    private final ManageNoticeVoteResponseUseCase manageNoticeVoteResponseUseCase;
    private final GetNoticeContentUseCase getNoticeContentUseCase;
    private final ManageChallengerUseCase manageChallengerUseCase;

    public void seed(QaSeedContext context) {
        Long gisuId = context.gisuId(11);
        Long schoolId = context.schoolId("중앙대학교");
        seedNotice(context, "11기 전체 운영 안내", "central_g11_president",
            new NoticeTargetInfo(gisuId, null, null, List.of(), NoticeTab.CHALLENGER),
            "cau_g11_web", false);
        seedNotice(context, "11기 수달 지부 모임", "sudal_g11_chapterpresident",
            new NoticeTargetInfo(gisuId, context.chapterId(11, "수달"), null, List.of(), NoticeTab.CHALLENGER),
            "cau_g11_web", false);
        Long schoolNoticeId = seedNotice(context, "11기 중앙대 스터디 시간 투표",
            "cau_g11_schoolpresident",
            new NoticeTargetInfo(gisuId, null, schoolId, List.of(), NoticeTab.CHALLENGER),
            "cau_g11_web", true);
        seedNotice(context, "11기 중앙대 운영진 안내", "cau_g11_schoolpresident",
            new NoticeTargetInfo(gisuId, null, schoolId, List.of(), NoticeTab.SCHOOL_PART_LEADER),
            "cau_g11_web_leader", false);
        for (PartNotice part : CURRENT_PARTS) {
            seedNotice(context, "11기 " + part.label() + " 학습 안내",
                "central_g11_education",
                new NoticeTargetInfo(gisuId, null, null, List.of(part.part()), NoticeTab.CHALLENGER),
                part.learnerAlias(), false);
            seedPoints(context, part.learnerAlias(), 11);
        }
        seedVote(context, schoolNoticeId);
        seedPoints(context, "cau_g11_web_infra", 11);
        seedPoints(context, "cau_g11_mobile_infra", 11);

        seedNotice(context, "10기 Xenon 활동 안내", "xenon_chapterpresident",
            new NoticeTargetInfo(context.gisuId(10), context.chapterId(10, "Xenon"), null,
                List.of(), NoticeTab.CHALLENGER), "cau_node", false);
        seedNotice(context, "10기 중앙대 활동 안내", "cau_schoolpresident",
            new NoticeTargetInfo(context.gisuId(10), null, schoolId, List.of(), NoticeTab.CHALLENGER),
            "cau_springboot", false);
        LEGACY_LEARNERS.forEach(alias -> seedPoints(context, alias, 10));
    }

    private Long seedNotice(QaSeedContext context, String title, String authorAlias,
                            NoticeTargetInfo target, String readerAlias, boolean mustRead) {
        Long noticeId = manageNoticeUseCase.createNotice(
            new CreateNoticeCommand(context.memberId(authorAlias), "[QA] " + title,
                "개발 환경 QA용 공지입니다. 대상 범위, 읽음 여부와 권한을 확인합니다.",
                false, mustRead, target));
        manageNoticeReadUseCase.recordRead(noticeId, context.memberId(readerAlias));
        return noticeId;
    }

    private void seedVote(QaSeedContext context, Long noticeId) {
        Instant now = Instant.now();
        manageNoticeContentUseCase.addVote(AddNoticeVoteCommand.builder()
            .createdMemberId(context.memberId("cau_g11_schoolpresident"))
            .title("스터디 가능한 시간을 선택해주세요")
            .isAnonymous(false).allowMultipleChoice(false)
            .startsAt(now.minus(5, ChronoUnit.MINUTES)).endsAtExclusive(now.plus(30, ChronoUnit.DAYS))
            .options(List.of("평일 저녁", "주말 오후")).build(), noticeId);
        seedVoteResponse(noticeId, context.memberId("cau_g11_web"), 0);
        Long planMemberId = context.memberId("cau_g11_plan");
        manageNoticeReadUseCase.recordRead(noticeId, planMemberId);
        seedVoteResponse(noticeId, planMemberId, 1);
    }

    private void seedVoteResponse(Long noticeId, Long memberId, int optionIndex) {
        NoticeVoteInfo vote = getNoticeContentUseCase.findVoteByNoticeId(noticeId, memberId);
        manageNoticeVoteResponseUseCase.submit(SubmitNoticeVoteResponseCommand.builder()
            .noticeId(noticeId).respondentMemberId(memberId)
            .selectedOptionIds(List.of(vote.options().get(optionIndex).optionId())).build());
    }

    private void seedPoints(QaSeedContext context, String alias, int generation) {
        Long challengerId = context.challengerId(alias, generation);
        for (PointType pointType : List.of(PointType.UMC_EVENT_REVIEW, PointType.STUDY_LATE)) {
            manageChallengerUseCase.grantChallengerPoint(GrantChallengerPointCommand.builder()
                .challengerId(challengerId).pointType(pointType)
                .description("[QA] " + generation + "기 상벌점 조회 확인").build());
        }
    }

    private record PartNotice(ChallengerPart part, String learnerAlias, String label) {
    }
}
