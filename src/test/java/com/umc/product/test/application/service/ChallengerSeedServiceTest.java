package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.command.RegisterEmailMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.EmailRegisterMemberCommand;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.test.application.port.in.command.dto.SeedChallengersCommand;
import com.umc.product.test.application.port.in.command.dto.SeedChallengersResult;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChallengerSeedServiceTest {

    @Mock
    DummyMemberFactory dummyMemberFactory;
    @Mock
    GetMemberUseCase getMemberUseCase;
    @Mock
    RegisterEmailMemberUseCase registerEmailMemberUseCase;
    @Mock
    GetGisuUseCase getGisuUseCase;
    @Mock
    GetChapterUseCase getChapterUseCase;
    @Mock
    ManageChallengerUseCase manageChallengerUseCase;

    @InjectMocks
    ChallengerSeedService sut;

    @BeforeEach
    void setUp() {
        lenient().when(getMemberUseCase.countAll()).thenReturn(0L);
        lenient().when(dummyMemberFactory.nextEmailCommandWithSchool(anyLong(), anyLong()))
            .thenReturn(mock(EmailRegisterMemberCommand.class));
        AtomicLong memberIdCounter = new AtomicLong(1L);
        lenient().when(registerEmailMemberUseCase.register(any()))
            .thenAnswer(inv -> memberIdCounter.getAndIncrement());
    }

    @Test
    @DisplayName("parts 가 null 이면 ADMIN 제외 모든 파트가 대상이 된다")
    void parts_기본값_ADMIN_제외() {
        // Given
        Long gisuId = 9L;
        ChapterWithSchoolsInfo chapter = new ChapterWithSchoolsInfo(
            1L, "서울", List.of(new ChapterWithSchoolsInfo.SchoolInfo(101L, "건국대"))
        );
        given(getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId)).willReturn(List.of(chapter));
        given(manageChallengerUseCase.createChallengerBulk(any())).willReturn(List.of(1L));

        // When
        SeedChallengersResult result = sut.seed(new SeedChallengersCommand(gisuId, 1, null, null));

        // Then
        long expectedParts = java.util.Arrays.stream(ChallengerPart.values())
            .filter(p -> p != ChallengerPart.ADMIN).count();
        assertThat(result.perCellSummary()).hasSize((int) expectedParts);
        assertThat(result.perCellSummary())
            .noneMatch(s -> s.part() == ChallengerPart.ADMIN);
    }

    @Test
    @DisplayName("chapterIds 필터가 있으면 해당 Chapter 만 시딩한다")
    void chapterIds_필터_적용() {
        // Given
        Long gisuId = 9L;
        ChapterWithSchoolsInfo c1 = new ChapterWithSchoolsInfo(
            1L, "서울", List.of(new ChapterWithSchoolsInfo.SchoolInfo(101L, "건국대"))
        );
        ChapterWithSchoolsInfo c2 = new ChapterWithSchoolsInfo(
            2L, "경기", List.of(new ChapterWithSchoolsInfo.SchoolInfo(201L, "성균관대"))
        );
        given(getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId)).willReturn(List.of(c1, c2));
        given(manageChallengerUseCase.createChallengerBulk(any())).willReturn(List.of(1L));

        // When
        SeedChallengersResult result = sut.seed(new SeedChallengersCommand(
            gisuId, 1, List.of(ChallengerPart.WEB), List.of(2L)
        ));

        // Then
        assertThat(result.perCellSummary())
            .hasSize(1)
            .allSatisfy(s -> {
                assertThat(s.chapterId()).isEqualTo(2L);
                assertThat(s.schoolId()).isEqualTo(201L);
                assertThat(s.part()).isEqualTo(ChallengerPart.WEB);
            });
    }

    @Test
    @DisplayName("한 셀에서 createChallengerBulk 가 실패해도 다른 셀 시딩은 진행된다")
    void 셀_실패_격리() {
        // Given
        Long gisuId = 9L;
        ChapterWithSchoolsInfo chapter = new ChapterWithSchoolsInfo(
            1L, "서울", List.of(
            new ChapterWithSchoolsInfo.SchoolInfo(101L, "건국대"),
            new ChapterWithSchoolsInfo.SchoolInfo(102L, "동국대")
        )
        );
        given(getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId)).willReturn(List.of(chapter));
        given(manageChallengerUseCase.createChallengerBulk(any()))
            .willThrow(new RuntimeException("boom on first cell"))
            .willReturn(List.of(99L));

        // When
        SeedChallengersResult result = sut.seed(new SeedChallengersCommand(
            gisuId, 1, List.of(ChallengerPart.WEB), null
        ));

        // Then
        assertThat(result.perCellSummary()).hasSize(2);
        assertThat(result.totalCreated()).isEqualTo(1);
        verify(manageChallengerUseCase, times(2)).createChallengerBulk(any());
    }

    @Test
    @DisplayName("멤버 생성 실패는 memberFailed, 챌린저 bulk 실패는 challengerFailed 로 분리 보고된다")
    void 실패_단계_분리_보고() {
        // Given - 1셀, count=2, 멤버 1명 실패 + 1명 성공 → 챌린저 bulk 실패
        Long gisuId = 9L;
        ChapterWithSchoolsInfo chapter = new ChapterWithSchoolsInfo(
            1L, "서울", List.of(new ChapterWithSchoolsInfo.SchoolInfo(101L, "건국대"))
        );
        given(getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId)).willReturn(List.of(chapter));
        given(registerEmailMemberUseCase.register(any()))
            .willThrow(new RuntimeException("member boom"))
            .willReturn(500L);
        given(manageChallengerUseCase.createChallengerBulk(any()))
            .willThrow(new RuntimeException("challenger boom"));

        // When
        SeedChallengersResult result = sut.seed(new SeedChallengersCommand(
            gisuId, 2, List.of(ChallengerPart.WEB), null
        ));

        // Then
        assertThat(result.perCellSummary()).hasSize(1);
        SeedChallengersResult.PerCellSummary cell = result.perCellSummary().get(0);
        assertThat(cell.created()).isZero();
        assertThat(cell.memberFailed()).isEqualTo(1);
        assertThat(cell.challengerFailed()).isEqualTo(1);
        assertThat(cell.totalFailed()).isEqualTo(2);
    }

    @Test
    @DisplayName("gisuId 가 null 이면 활성 기수를 사용한다")
    void gisuId_null_시_활성_기수_조회() {
        // Given
        given(getGisuUseCase.getActiveGisuId()).willReturn(10L);
        given(getChapterUseCase.getChaptersWithSchoolsByGisuId(10L)).willReturn(List.of());

        // When
        SeedChallengersResult result = sut.seed(new SeedChallengersCommand(
            null, 1, List.of(ChallengerPart.WEB), null
        ));

        // Then
        assertThat(result.gisuId()).isEqualTo(10L);
    }
}
