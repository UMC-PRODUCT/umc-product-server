package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.command.ManageCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.command.ManageOriginalWorkbookMissionUseCase;
import com.umc.product.curriculum.application.port.in.command.ManageOriginalWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.ManageWeeklyCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.CreateCurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.CreateWeeklyCurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.CreateOriginalWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.CreateOriginalWorkbookMissionCommand;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumUseCase;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.test.application.port.in.command.dto.SeedCurriculumCommand;
import com.umc.product.test.application.port.in.command.dto.SeedCurriculumResult;

@ExtendWith(MockitoExtension.class)
class CurriculumSeedServiceTest {

    @Mock private DummyCurriculumFactory dummyCurriculumFactory;
    @Mock private GetGisuUseCase getGisuUseCase;
    @Mock private ManageCurriculumUseCase manageCurriculumUseCase;
    @Mock private GetCurriculumUseCase getCurriculumUseCase;
    @Mock private ManageWeeklyCurriculumUseCase manageWeeklyCurriculumUseCase;
    @Mock private ManageOriginalWorkbookUseCase manageOriginalWorkbookUseCase;
    @Mock private ManageOriginalWorkbookMissionUseCase manageOriginalWorkbookMissionUseCase;
    @InjectMocks private CurriculumSeedService sut;

    @BeforeEach
    void setUp() {
        lenient().when(getGisuUseCase.getById(anyLong()))
            .thenAnswer(invocation -> new GisuInfo(invocation.getArgument(0), 9L, null, null, true));
        lenient().when(dummyCurriculumFactory.nextCurriculumCommand(anyLong(), any()))
            .thenCallRealMethod();
        lenient().when(dummyCurriculumFactory.nextWeeklyCurriculumCommand(anyLong(), anyLong()))
            .thenReturn(mock(CreateWeeklyCurriculumCommand.class));
        lenient().when(dummyCurriculumFactory.nextOriginalWorkbookCommand(anyLong(), anyLong()))
            .thenReturn(mock(CreateOriginalWorkbookCommand.class));
        lenient().when(dummyCurriculumFactory.nextOriginalWorkbookMissionCommand(anyLong(), anyInt()))
            .thenReturn(mock(CreateOriginalWorkbookMissionCommand.class));
    }

    @Test
    @DisplayName("기본 파트 사용 시 ADMIN 제외 모든 파트의 커리큘럼을 생성한다")
    void 기본_파트_ADMIN_제외() {
        AtomicLong ids = new AtomicLong(1L);
        given(manageCurriculumUseCase.create(any())).willAnswer(invocation -> ids.getAndIncrement());

        SeedCurriculumResult result = sut.seed(new SeedCurriculumCommand(9L, 0, 0, null, null));

        ArgumentCaptor<CreateCurriculumCommand> captor = ArgumentCaptor.forClass(CreateCurriculumCommand.class);
        verify(manageCurriculumUseCase, org.mockito.Mockito.times(10)).create(captor.capture());
        assertThat(captor.getAllValues()).extracting(CreateCurriculumCommand::part)
            .doesNotContain(ChallengerPart.ADMIN)
            .contains(ChallengerPart.WEB_PRODUCT_ENGINEER, ChallengerPart.MOBILE_PRODUCT_ENGINEER,
                ChallengerPart.INFRA);
        assertThat(result.createdCurriculumIds()).hasSize(10);
    }

    @Test
    @DisplayName("요청한 단일 파트에 주차와 워크북 및 미션을 생성한다")
    void 요청한_파트의_학습_골격을_생성한다() {
        given(manageCurriculumUseCase.create(any())).willReturn(10L);
        given(manageWeeklyCurriculumUseCase.createBulk(any())).willReturn(List.of(20L, 21L));
        given(manageOriginalWorkbookUseCase.createBulk(any())).willReturn(List.of(30L, 31L));
        given(manageOriginalWorkbookMissionUseCase.create(any())).willReturn(40L, 41L);

        SeedCurriculumResult result = sut.seed(new SeedCurriculumCommand(
            9L, 2, 1, List.of(ChallengerPart.INFRA), null));

        assertThat(result.createdCurriculumIds()).containsExactly(10L);
        assertThat(result.createdWeeklyCurriculumIds()).containsExactly(20L, 21L);
        assertThat(result.createdOriginalWorkbookIds()).containsExactly(30L, 31L);
        assertThat(result.createdMissionIds()).containsExactly(40L, 41L);
    }

    @Test
    @DisplayName("파트 목록에 null이 있으면 커리큘럼 생성 전에 거부한다")
    void null_파트를_거부한다() {
        assertThatThrownBy(() -> sut.seed(new SeedCurriculumCommand(
            9L, 1, 1, java.util.Arrays.asList(ChallengerPart.PLAN, null), null)))
            .isInstanceOf(CurriculumDomainException.class)
            .extracting("baseCode").isEqualTo(CurriculumErrorCode.INVALID_CURRICULUM_LEARNING_TYPE);
    }

    @Test
    @DisplayName("gisuId가 없으면 활성 기수를 사용한다")
    void 활성_기수를_사용한다() {
        given(getGisuUseCase.getActiveGisuId()).willReturn(11L);
        given(manageCurriculumUseCase.create(any())).willReturn(1L);

        SeedCurriculumResult result = sut.seed(new SeedCurriculumCommand(
            null, 0, 0, List.of(ChallengerPart.PLAN), null));

        assertThat(result.gisuId()).isEqualTo(11L);
    }
}
