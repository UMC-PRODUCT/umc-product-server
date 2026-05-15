package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.project.application.port.in.command.AddProjectMemberUseCase;
import com.umc.product.project.application.port.in.command.CreateDraftProjectUseCase;
import com.umc.product.project.application.port.in.command.dto.AddProjectMemberCommand;
import com.umc.product.test.application.port.in.command.dto.SeedProjectsCommand;
import com.umc.product.test.application.port.in.command.dto.SeedProjectsResult;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectSeedServiceTest {

    @Mock
    GetMemberUseCase getMemberUseCase;
    @Mock
    GetGisuUseCase getGisuUseCase;
    @Mock
    GetChapterUseCase getChapterUseCase;
    @Mock
    GetChallengerUseCase getChallengerUseCase;
    @Mock
    ManageChallengerUseCase manageChallengerUseCase;
    @Mock
    CreateDraftProjectUseCase createDraftProjectUseCase;
    @Mock
    AddProjectMemberUseCase addProjectMemberUseCase;

    PartAssignmentPolicy partAssignmentPolicy = new PartAssignmentPolicy();
    ProjectSeedService sut;

    @BeforeEach
    void setUp() {
        sut = new ProjectSeedService(
            partAssignmentPolicy,
            getMemberUseCase,
            getGisuUseCase,
            getChapterUseCase,
            getChallengerUseCase,
            manageChallengerUseCase,
            createDraftProjectUseCase,
            addProjectMemberUseCase
        );
    }

    @Test
    @DisplayName("풀이 슬롯 최대보다 작은 school 은 시딩을 스킵한다")
    void 풀_부족_시_스킵() {
        // Given - 모든 school 풀이 슬롯 최대(13)보다 작음
        ChapterWithSchoolsInfo chapter = new ChapterWithSchoolsInfo(
            1L, "서울", List.of(new ChapterWithSchoolsInfo.SchoolInfo(101L, "건국대"))
        );
        given(getChapterUseCase.getChaptersWithSchoolsByGisuId(9L)).willReturn(List.of(chapter));
        given(getMemberUseCase.findAllIdsBySchoolId(101L)).willReturn(Set.of(1L, 2L, 3L));

        // When
        SeedProjectsResult result = sut.seed(new SeedProjectsCommand(5, 9L));

        // Then
        assertThat(result.createdProjectIds()).isEmpty();
        assertThat(result.skippedChapters()).isNotEmpty();
        assertThat(result.skippedChapters().get(0).reason()).contains("INSUFFICIENT_POOL");
        verify(createDraftProjectUseCase, never()).create(any());
    }

    @Test
    @DisplayName("정상 시딩 시 PO 가 PLAN 챌린저로 등록되지 않은 경우 자동 등록한다")
    void PO_PLAN_챌린저_자동_등록() {
        // Given
        Long gisuId = 9L;
        ChapterWithSchoolsInfo chapter = new ChapterWithSchoolsInfo(
            1L, "서울", List.of(new ChapterWithSchoolsInfo.SchoolInfo(101L, "건국대"))
        );
        Set<Long> pool = bigPool(13);
        given(getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId)).willReturn(List.of(chapter));
        given(getMemberUseCase.findAllIdsBySchoolId(101L)).willReturn(pool);
        given(getChallengerUseCase.findByMemberIdAndGisuId(anyLong(), eq(gisuId)))
            .willReturn(Optional.empty());
        given(createDraftProjectUseCase.create(any())).willReturn(1000L);

        // When
        SeedProjectsResult result = sut.seed(new SeedProjectsCommand(1, gisuId));

        // Then
        assertThat(result.createdProjectIds()).hasSize(1);
        verify(manageChallengerUseCase, times(1)).createChallenger(any());
        verify(createDraftProjectUseCase, times(1)).create(any());
        verify(addProjectMemberUseCase, atLeast(11)).add(any());
    }

    @Test
    @DisplayName("같은 호출 안에서 한 멤버가 두 프로젝트에 중복 배정되지 않는다")
    void 멤버_중복_배정_차단() {
        // Given - 한 school 에 30명 풀, 2개 프로젝트 시딩
        Long gisuId = 9L;
        ChapterWithSchoolsInfo chapter = new ChapterWithSchoolsInfo(
            1L, "서울", List.of(new ChapterWithSchoolsInfo.SchoolInfo(101L, "건국대"))
        );
        Set<Long> pool = bigPool(30);
        given(getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId)).willReturn(List.of(chapter));
        given(getMemberUseCase.findAllIdsBySchoolId(101L)).willReturn(pool);
        given(getChallengerUseCase.findByMemberIdAndGisuId(anyLong(), eq(gisuId)))
            .willReturn(Optional.empty());
        AtomicLong projectIdSeq = new AtomicLong(1000L);
        given(createDraftProjectUseCase.create(any())).willAnswer(inv -> projectIdSeq.getAndIncrement());

        // When
        SeedProjectsResult result = sut.seed(new SeedProjectsCommand(2, gisuId));

        // Then
        assertThat(result.createdProjectIds()).hasSize(2);
        ArgumentCaptor<AddProjectMemberCommand> captor = ArgumentCaptor.forClass(AddProjectMemberCommand.class);
        verify(addProjectMemberUseCase, atLeast(22)).add(captor.capture());
        Set<Long> usedMemberIds = new HashSet<>();
        for (AddProjectMemberCommand cmd : captor.getAllValues()) {
            assertThat(usedMemberIds.add(cmd.memberId()))
                .as("memberId %d already used in another project", cmd.memberId())
                .isTrue();
        }
    }

    @Test
    @DisplayName("gisuId 가 null 이면 활성 기수를 사용한다")
    void gisuId_null_시_활성_기수_사용() {
        // Given
        given(getGisuUseCase.getActiveGisuId()).willReturn(10L);
        given(getChapterUseCase.getChaptersWithSchoolsByGisuId(10L)).willReturn(List.of());

        // When
        SeedProjectsResult result = sut.seed(new SeedProjectsCommand(1, null));

        // Then
        assertThat(result.createdProjectIds()).isEmpty();
        verify(getGisuUseCase, times(1)).getActiveGisuId();
    }

    private static Set<Long> bigPool(int size) {
        Set<Long> pool = new HashSet<>();
        for (long i = 1; i <= size; i++) {
            pool.add(i);
        }
        return pool;
    }
}
