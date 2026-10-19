package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.project.application.port.in.command.dto.ReleaseMatchingChapterTransferCommand;
import com.umc.product.project.application.port.in.command.dto.TransferMatchingChapterCommand;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingChapterTransferInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingChapterTransferPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.application.port.out.SaveProjectMatchingChapterTransferPort;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingChapterTransfer;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectMatchingChapterTransferCommandServiceTest {

    private static final Long GISU_ID = 11L;
    private static final Long SEOUL = 3L;
    private static final Long GYEONGGI = 5L;
    private static final Long BUSAN = 7L;
    private static final Long DESIGNER_ID = 1L;
    private static final Long REQUESTER_ID = 99L;
    private static final List<ProjectApplicationStatus> CANCELLABLE = List.of(
        ProjectApplicationStatus.DRAFT, ProjectApplicationStatus.SUBMITTED, ProjectApplicationStatus.REJECTED
    );

    @Mock
    LoadProjectMatchingChapterTransferPort loadTransferPort;
    @Mock
    SaveProjectMatchingChapterTransferPort saveTransferPort;
    @Mock
    LoadProjectApplicationPort loadProjectApplicationPort;
    @Mock
    SaveProjectApplicationPort saveProjectApplicationPort;
    @Mock
    LoadProjectMemberPort loadProjectMemberPort;
    @Mock
    ProjectMatchingChapterResolver matchingChapterResolver;
    @Mock
    GetChallengerUseCase getChallengerUseCase;
    @Mock
    GetChapterUseCase getChapterUseCase;
    @Mock
    CheckChallengerAuthorityUseCase checkChallengerAuthorityUseCase;

    @InjectMocks
    ProjectMatchingChapterTransferCommandService sut;

    @BeforeEach
    void setUp() {
        // 기본: 서울 소속 활성 디자이너, 활성 이동 없음, 팀/합격/진행 중 지원서 없음, 요청자는 서울 지부장
        given(getChapterUseCase.listByGisuId(GISU_ID)).willReturn(List.of(
            new ChapterInfo(SEOUL, "서울"), new ChapterInfo(GYEONGGI, "경기"), new ChapterInfo(BUSAN, "부산")
        ));
        given(matchingChapterResolver.findOriginChapter(DESIGNER_ID, GISU_ID))
            .willReturn(Optional.of(new ChapterInfo(SEOUL, "서울")));
        given(matchingChapterResolver.findActiveTransfer(DESIGNER_ID, GISU_ID)).willReturn(Optional.empty());
        given(getChallengerUseCase.getActiveByMemberIdAndGisuId(DESIGNER_ID, GISU_ID))
            .willReturn(challenger(ChallengerPart.DESIGN));
        given(loadProjectMemberPort.existsByGisuAndMember(GISU_ID, DESIGNER_ID)).willReturn(false);
        given(loadProjectApplicationPort.listByApplicantMemberIdAndGisuIdAndStatusIn(eq(DESIGNER_ID), eq(GISU_ID), any()))
            .willReturn(List.of());
        given(checkChallengerAuthorityUseCase.isChapterPresidentInGisu(REQUESTER_ID, GISU_ID, SEOUL)).willReturn(true);
        given(saveTransferPort.save(any())).willAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    class transfer {

        @Test
        @DisplayName("원지부 지부장이 등록하면 이동이 저장되고 임시저장·제출 지원서가 자동 철회된다")
        void 등록하면_저장되고_진행중_지원서가_철회된다() {
            ProjectApplication draft = application(SEOUL);
            ProjectApplication submitted = application(SEOUL);
            submitted.submit();
            given(loadProjectApplicationPort.listByApplicantMemberIdAndGisuIdAndStatusIn(
                DESIGNER_ID, GISU_ID, CANCELLABLE))
                .willReturn(List.of(draft, submitted));

            List<ProjectMatchingChapterTransferInfo> result = sut.transfer(command(GYEONGGI));

            assertThat(result).singleElement().satisfies(info -> {
                assertThat(info.fromChapterId()).isEqualTo(SEOUL);
                assertThat(info.toChapterId()).isEqualTo(GYEONGGI);
                assertThat(info.cancelledApplicationCount()).isEqualTo(2);
            });
            assertThat(draft.getStatus()).isEqualTo(ProjectApplicationStatus.CANCELLED);
            assertThat(submitted.getStatus()).isEqualTo(ProjectApplicationStatus.CANCELLED);
            assertThat(draft.getStatusChangeReason()).isEqualTo(ProjectMatchingChapterTransferCommandService.AUTO_CANCEL_REASON);
            then(saveProjectApplicationPort).should().saveAll(List.of(draft, submitted));
            then(saveTransferPort).should().save(any(ProjectMatchingChapterTransfer.class));
        }

        @Test
        @DisplayName("자동 선발 전 차수의 불합격 지원서는 철회하고, 마감 처리된 차수의 불합격 지원서는 그대로 둔다")
        void 마감_전_차수의_불합격만_철회한다() {
            ProjectApplication openRoundRejected = rejected(application(SEOUL));
            ProjectApplication finalizedRoundRejected = rejected(application(SEOUL));
            finalizedRoundRejected.getAppliedMatchingRound().executeAutoDecision(REQUESTER_ID);
            given(loadProjectApplicationPort.listByApplicantMemberIdAndGisuIdAndStatusIn(
                DESIGNER_ID, GISU_ID, CANCELLABLE))
                .willReturn(List.of(openRoundRejected, finalizedRoundRejected));

            List<ProjectMatchingChapterTransferInfo> result = sut.transfer(command(GYEONGGI));

            assertThat(result.getFirst().cancelledApplicationCount()).isEqualTo(1);
            assertThat(openRoundRejected.getStatus()).isEqualTo(ProjectApplicationStatus.CANCELLED);
            assertThat(finalizedRoundRejected.getStatus()).isEqualTo(ProjectApplicationStatus.REJECTED);
            then(saveProjectApplicationPort).should().saveAll(List.of(openRoundRejected));
        }

        @Test
        @DisplayName("이동지부 지부장도 등록할 수 있다")
        void 이동지부_지부장도_등록할_수_있다() {
            given(checkChallengerAuthorityUseCase.isChapterPresidentInGisu(REQUESTER_ID, GISU_ID, SEOUL)).willReturn(false);
            given(checkChallengerAuthorityUseCase.isChapterPresidentInGisu(REQUESTER_ID, GISU_ID, GYEONGGI))
                .willReturn(true);

            assertThat(sut.transfer(command(GYEONGGI))).hasSize(1);
        }

        @Test
        @DisplayName("해당 기수 총괄단은 어느 지부든 등록할 수 있다")
        void 총괄단은_등록할_수_있다() {
            given(checkChallengerAuthorityUseCase.isChapterPresidentInGisu(anyLong(), anyLong(), anyLong()))
                .willReturn(false);
            given(checkChallengerAuthorityUseCase.isCentralCoreInGisu(REQUESTER_ID, GISU_ID)).willReturn(true);

            assertThat(sut.transfer(command(GYEONGGI))).hasSize(1);
        }

        @Test
        @DisplayName("원지부·이동지부와 무관한 지부장은 CHAPTER_TRANSFER_ACCESS_DENIED")
        void 관련없는_지부장은_거부() {
            given(checkChallengerAuthorityUseCase.isChapterPresidentInGisu(REQUESTER_ID, GISU_ID, SEOUL)).willReturn(false);
            given(checkChallengerAuthorityUseCase.isChapterPresidentInGisu(REQUESTER_ID, GISU_ID, BUSAN)).willReturn(true);

            assertError(() -> sut.transfer(command(GYEONGGI)), ProjectErrorCode.CHAPTER_TRANSFER_ACCESS_DENIED);
            then(saveTransferPort).should(never()).save(any());
        }

        @Test
        @DisplayName("기획 파트는 CHAPTER_TRANSFER_PART_NOT_ALLOWED")
        void 기획_파트는_거부() {
            given(getChallengerUseCase.getActiveByMemberIdAndGisuId(DESIGNER_ID, GISU_ID))
                .willReturn(challenger(ChallengerPart.PLAN));

            assertError(() -> sut.transfer(command(GYEONGGI)), ProjectErrorCode.CHAPTER_TRANSFER_PART_NOT_ALLOWED);
        }

        @Test
        @DisplayName("해당 기수 지부가 아니면 CHAPTER_TRANSFER_CHAPTER_NOT_IN_GISU")
        void 기수_밖_지부는_거부() {
            assertError(() -> sut.transfer(command(404L)), ProjectErrorCode.CHAPTER_TRANSFER_CHAPTER_NOT_IN_GISU);
        }

        @Test
        @DisplayName("원지부와 같은 지부로는 CHAPTER_TRANSFER_SAME_CHAPTER")
        void 같은_지부는_거부() {
            assertError(() -> sut.transfer(command(SEOUL)), ProjectErrorCode.CHAPTER_TRANSFER_SAME_CHAPTER);
        }

        @Test
        @DisplayName("이미 활성 이동이 있으면 CHAPTER_TRANSFER_ALREADY_ACTIVE")
        void 이미_이동했으면_거부() {
            given(matchingChapterResolver.findActiveTransfer(DESIGNER_ID, GISU_ID)).willReturn(Optional.of(
                ProjectMatchingChapterTransfer.create(GISU_ID, DESIGNER_ID, SEOUL, BUSAN, null, REQUESTER_ID)));

            assertError(() -> sut.transfer(command(GYEONGGI)), ProjectErrorCode.CHAPTER_TRANSFER_ALREADY_ACTIVE);
        }

        @Test
        @DisplayName("이미 팀원이면 CHAPTER_TRANSFER_ALREADY_MATCHED")
        void 팀원이면_거부() {
            given(loadProjectMemberPort.existsByGisuAndMember(GISU_ID, DESIGNER_ID)).willReturn(true);

            assertError(() -> sut.transfer(command(GYEONGGI)), ProjectErrorCode.CHAPTER_TRANSFER_ALREADY_MATCHED);
        }

        @Test
        @DisplayName("합격한 지원서가 있으면 CHAPTER_TRANSFER_ALREADY_MATCHED")
        void 합격_지원서가_있으면_거부() {
            given(loadProjectApplicationPort.listByApplicantMemberIdAndGisuIdAndStatusIn(
                DESIGNER_ID, GISU_ID, List.of(ProjectApplicationStatus.APPROVED)))
                .willReturn(List.of(application(SEOUL)));

            assertError(() -> sut.transfer(command(GYEONGGI)), ProjectErrorCode.CHAPTER_TRANSFER_ALREADY_MATCHED);
            then(saveProjectApplicationPort).should(never()).saveAll(any());
        }

        @Test
        @DisplayName("같은 회원이 요청에 두 번 들어 있으면 CHAPTER_TRANSFER_DUPLICATE_TARGET")
        void 중복_대상은_거부() {
            TransferMatchingChapterCommand command = TransferMatchingChapterCommand.builder()
                .requesterMemberId(REQUESTER_ID)
                .gisuId(GISU_ID)
                .targets(List.of(
                    new TransferMatchingChapterCommand.Target(DESIGNER_ID, GYEONGGI, null),
                    new TransferMatchingChapterCommand.Target(DESIGNER_ID, BUSAN, null)
                ))
                .build();

            assertError(() -> sut.transfer(command), ProjectErrorCode.CHAPTER_TRANSFER_DUPLICATE_TARGET);
            then(saveTransferPort).should(never()).save(any());
        }
    }

    @Nested
    class release {

        @Test
        @DisplayName("해제하면 이동이 비활성이 되고 이동지부에서 낸 진행 중 지원서가 철회된다")
        void 해제하면_비활성이_되고_지원서가_철회된다() {
            ProjectMatchingChapterTransfer transfer = activeTransfer();
            given(loadTransferPort.getById(10L)).willReturn(transfer);
            ProjectApplication draft = application(GYEONGGI);
            given(loadProjectApplicationPort.listByApplicantMemberIdAndGisuIdAndStatusIn(
                DESIGNER_ID, GISU_ID, CANCELLABLE))
                .willReturn(List.of(draft));

            ProjectMatchingChapterTransferInfo result = sut.release(releaseCommand());

            assertThat(transfer.isActive()).isFalse();
            assertThat(result.releasedAt()).isNotNull();
            assertThat(result.cancelledApplicationCount()).isEqualTo(1);
            assertThat(draft.getStatus()).isEqualTo(ProjectApplicationStatus.CANCELLED);
        }

        @Test
        @DisplayName("해제할 때도 자동 선발 전 차수의 불합격 지원서를 철회한다")
        void 해제할_때도_마감_전_불합격을_철회한다() {
            given(loadTransferPort.getById(10L)).willReturn(activeTransfer());
            ProjectApplication openRoundRejected = rejected(application(GYEONGGI));
            given(loadProjectApplicationPort.listByApplicantMemberIdAndGisuIdAndStatusIn(
                DESIGNER_ID, GISU_ID, CANCELLABLE))
                .willReturn(List.of(openRoundRejected));

            ProjectMatchingChapterTransferInfo result = sut.release(releaseCommand());

            assertThat(result.cancelledApplicationCount()).isEqualTo(1);
            assertThat(openRoundRejected.getStatus()).isEqualTo(ProjectApplicationStatus.CANCELLED);
        }

        @Test
        @DisplayName("이미 해제된 이동이면 CHAPTER_TRANSFER_ALREADY_RELEASED")
        void 이미_해제됐으면_거부() {
            ProjectMatchingChapterTransfer transfer = activeTransfer();
            transfer.release(REQUESTER_ID, null);
            given(loadTransferPort.getById(10L)).willReturn(transfer);

            assertError(() -> sut.release(releaseCommand()), ProjectErrorCode.CHAPTER_TRANSFER_ALREADY_RELEASED);
        }

        @Test
        @DisplayName("권한이 없으면 CHAPTER_TRANSFER_ACCESS_DENIED")
        void 권한이_없으면_거부() {
            given(loadTransferPort.getById(10L)).willReturn(activeTransfer());
            given(checkChallengerAuthorityUseCase.isChapterPresidentInGisu(REQUESTER_ID, GISU_ID, SEOUL)).willReturn(false);

            assertError(() -> sut.release(releaseCommand()), ProjectErrorCode.CHAPTER_TRANSFER_ACCESS_DENIED);
        }
    }

    private static TransferMatchingChapterCommand command(Long toChapterId) {
        return TransferMatchingChapterCommand.builder()
            .requesterMemberId(REQUESTER_ID)
            .gisuId(GISU_ID)
            .targets(List.of(new TransferMatchingChapterCommand.Target(DESIGNER_ID, toChapterId, "인원 조정")))
            .build();
    }

    private static ReleaseMatchingChapterTransferCommand releaseCommand() {
        return ReleaseMatchingChapterTransferCommand.builder()
            .requesterMemberId(REQUESTER_ID)
            .transferId(10L)
            .reason("등록 실수")
            .build();
    }

    private static ProjectMatchingChapterTransfer activeTransfer() {
        return ProjectMatchingChapterTransfer.create(GISU_ID, DESIGNER_ID, SEOUL, GYEONGGI, null, REQUESTER_ID);
    }

    private static ChallengerInfo challenger(ChallengerPart part) {
        return ChallengerInfo.builder()
            .challengerId(100L)
            .memberId(DESIGNER_ID)
            .gisuId(GISU_ID)
            .part(part)
            .challengerStatus(ChallengerStatus.ACTIVE)
            .build();
    }

    private static ProjectApplication application(Long chapterId) {
        Project project = Project.createDraft(GISU_ID, chapterId, 50L, 30L, 50L);
        Instant now = Instant.now();
        ProjectMatchingRound round = ProjectMatchingRound.create(
            "디자인 1차", null, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, chapterId,
            now.minusSeconds(3600), now.plusSeconds(3600), now.plusSeconds(7200), now.plusSeconds(14400)
        );
        return ProjectApplication.create(ProjectApplicationForm.create(project, 500L), 1L, DESIGNER_ID, round);
    }

    private static ProjectApplication rejected(ProjectApplication application) {
        application.submit();
        application.applyAutoDecision(ProjectApplicationStatus.REJECTED, null);
        return application;
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, ProjectErrorCode code) {
        assertThatThrownBy(call)
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(code);
    }
}
