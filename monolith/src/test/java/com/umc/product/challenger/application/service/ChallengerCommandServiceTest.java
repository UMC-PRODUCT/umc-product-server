package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.challenger.application.port.in.command.dto.ChallengerDeactivationType;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerCommand;
import com.umc.product.challenger.application.port.in.command.dto.DeactivateChallengerCommand;
import com.umc.product.challenger.application.port.in.command.dto.DeleteChallengerCommand;
import com.umc.product.challenger.application.port.in.command.dto.GrantChallengerPointCommand;
import com.umc.product.challenger.application.port.in.command.dto.UpdateChallengerCommand;
import com.umc.product.challenger.application.port.in.command.dto.UpdateChallengerPointCommand;
import com.umc.product.challenger.application.port.out.LoadChallengerPointPort;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPointPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerPoint;
import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengerCommandService")
class ChallengerCommandServiceTest {

    @Mock
    Environment environment;

    @Mock
    LoadChallengerPort loadChallengerPort;

    @Mock
    SaveChallengerPort saveChallengerPort;

    @Mock
    LoadChallengerPointPort loadChallengerPointPort;

    @Mock
    SaveChallengerPointPort saveChallengerPointPort;

    @Mock
    EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;

    @Mock
    GetGisuUseCase getGisuUseCase;

    @InjectMocks
    ChallengerCommandService sut;

    @Nested
    @DisplayName("createChallenger")
    class CreateChallenger {

        @Test
        @DisplayName("동일 기수 챌린저가 없으면 생성한다")
        void 동일_기수_챌린저가_없으면_생성한다() {
            CreateChallengerCommand command = CreateChallengerCommand.builder()
                .memberId(1L)
                .part(ChallengerPart.SPRINGBOOT)
                .gisuId(9L)
                .build();
            given(loadChallengerPort.findByMemberIdAndGisuId(1L, 9L)).willReturn(Optional.empty());
            given(saveChallengerPort.save(any(Challenger.class))).willAnswer(invocation -> {
                Challenger challenger = invocation.getArgument(0);
                ReflectionTestUtils.setField(challenger, "id", 100L);
                return challenger;
            });

            Long result = sut.createChallenger(command);

            assertThat(result).isEqualTo(100L);
            then(saveChallengerPort).should().save(any(Challenger.class));
        }

        @Test
        @DisplayName("동일 기수 챌린저가 없으면 생성 후 해당 회원의 권한 snapshot 캐시를 제거한다")
        void 동일_기수_챌린저가_없으면_생성_후_해당_회원의_권한_snapshot_캐시를_제거한다() {
            CreateChallengerCommand command = CreateChallengerCommand.builder()
                .memberId(1L)
                .part(ChallengerPart.SPRINGBOOT)
                .gisuId(9L)
                .build();
            given(loadChallengerPort.findByMemberIdAndGisuId(1L, 9L)).willReturn(Optional.empty());
            given(saveChallengerPort.save(any(Challenger.class))).willAnswer(invocation -> invocation.getArgument(0));

            sut.createChallenger(command);

            then(evictAuthoritySnapshotCacheUseCase).should().evictByMemberId(1L);
        }

        @Test
        @DisplayName("동일 기수 챌린저가 있으면 생성하지 않는다")
        void 동일_기수_챌린저가_있으면_생성하지_않는다() {
            CreateChallengerCommand command = CreateChallengerCommand.builder()
                .memberId(1L)
                .part(ChallengerPart.SPRINGBOOT)
                .gisuId(9L)
                .build();
            given(loadChallengerPort.findByMemberIdAndGisuId(1L, 9L))
                .willReturn(Optional.of(challenger(1L, ChallengerStatus.ACTIVE)));

            assertThatThrownBy(() -> sut.createChallenger(command))
                .isInstanceOf(ChallengerDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ChallengerErrorCode.CHALLENGER_ALREADY_EXISTS);

            then(saveChallengerPort).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateChallenger")
    class UpdateChallenger {

        @Test
        @DisplayName("변경할 파트와 상태가 모두 없으면 실패한다")
        void 변경할_파트와_상태가_모두_없으면_실패한다() {
            given(loadChallengerPort.getById(1L)).willReturn(challenger(1L, ChallengerStatus.ACTIVE));

            assertThatThrownBy(() -> sut.updateChallenger(
                UpdateChallengerCommand.forPartChange(1L, null, 99L)
            ))
                .isInstanceOf(ChallengerDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ChallengerErrorCode.BAD_CHALLENGER_UPDATE_REQUEST);

            then(saveChallengerPort).should(never()).save(any());
        }

        @Test
        @DisplayName("비활성 챌린저는 파트를 변경할 수 없다")
        void 비활성_챌린저는_파트를_변경할_수_없다() {
            given(loadChallengerPort.getById(1L)).willReturn(challenger(1L, ChallengerStatus.WITHDRAWN));

            assertThatThrownBy(() -> sut.updateChallenger(
                UpdateChallengerCommand.forPartChange(1L, ChallengerPart.WEB, 99L)
            ))
                .isInstanceOf(ChallengerDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ChallengerErrorCode.CHALLENGER_NOT_ACTIVE);

            then(saveChallengerPort).should(never()).save(any());
        }

        @Test
        @DisplayName("파트를 변경하면 해당 회원의 권한 snapshot 캐시를 제거한다")
        void 파트를_변경하면_해당_회원의_권한_snapshot_캐시를_제거한다() {
            given(loadChallengerPort.getById(1L)).willReturn(challenger(1L, ChallengerStatus.ACTIVE));

            sut.updateChallenger(UpdateChallengerCommand.forPartChange(1L, ChallengerPart.WEB, 99L));

            then(evictAuthoritySnapshotCacheUseCase).should().evictByMemberId(1L);
        }
    }

    @Test
    @DisplayName("챌린저를 대량 생성하면 생성된 회원들의 권한 snapshot 캐시를 제거한다")
    void 챌린저를_대량_생성하면_생성된_회원들의_권한_snapshot_캐시를_제거한다() {
        given(environment.getActiveProfiles()).willReturn(new String[] {"local"});
        given(saveChallengerPort.saveAll(any())).willAnswer(invocation -> invocation.getArgument(0));

        sut.createChallengerBulk(List.of(
            CreateChallengerCommand.builder()
                .memberId(1L)
                .part(ChallengerPart.SPRINGBOOT)
                .gisuId(9L)
                .build(),
            CreateChallengerCommand.builder()
                .memberId(2L)
                .part(ChallengerPart.WEB)
                .gisuId(9L)
                .build()
        ));

        then(evictAuthoritySnapshotCacheUseCase).should().evictByMemberIds(List.of(1L, 2L));
    }

    @Test
    @DisplayName("deactivateChallenger는 EXPEL 타입을 EXPELLED 상태로 매핑한다")
    void deactivateChallenger는_EXPEL_타입을_EXPELLED_상태로_매핑한다() {
        Challenger challenger = challenger(1L, ChallengerStatus.ACTIVE);
        given(loadChallengerPort.getById(1L)).willReturn(challenger);

        sut.deactivateChallenger(DeactivateChallengerCommand.of(
            1L,
            ChallengerDeactivationType.EXPEL,
            99L,
            "징계"
        ));

        assertThat(challenger.getStatus()).isEqualTo(ChallengerStatus.EXPELLED);
        assertThat(challenger.getModifiedBy()).isEqualTo(99L);
        assertThat(challenger.getModificationReason()).isEqualTo("징계");
    }

    @Test
    @DisplayName("챌린저를 비활성화하면 해당 회원의 권한 snapshot 캐시를 제거한다")
    void 챌린저를_비활성화하면_해당_회원의_권한_snapshot_캐시를_제거한다() {
        given(loadChallengerPort.getById(1L)).willReturn(challenger(1L, ChallengerStatus.ACTIVE));

        sut.deactivateChallenger(DeactivateChallengerCommand.of(
            1L,
            ChallengerDeactivationType.WITHDRAW,
            99L,
            "탈퇴"
        ));

        then(evictAuthoritySnapshotCacheUseCase).should().evictByMemberId(1L);
    }

    @Test
    @DisplayName("챌린저 삭제 후 해당 회원의 권한 snapshot 캐시를 제거한다")
    void 챌린저_삭제_후_해당_회원의_권한_snapshot_캐시를_제거한다() {
        given(loadChallengerPort.getById(1L)).willReturn(challenger(1L, ChallengerStatus.ACTIVE));

        sut.deleteChallenger(DeleteChallengerCommand.of(1L, "잘못 생성된 기록"));

        then(evictAuthoritySnapshotCacheUseCase).should().evictByMemberId(1L);
    }

    @Test
    @DisplayName("Challenger 삭제 전에 소속 Point를 port로 삭제한다")
    void Challenger_삭제_전에_소속_Point를_port로_삭제한다() {
        Challenger challenger = challenger(1L, ChallengerStatus.ACTIVE);
        given(loadChallengerPort.getById(1L)).willReturn(challenger);

        sut.deleteChallenger(DeleteChallengerCommand.of(1L, "잘못 생성"));

        InOrder order = inOrder(saveChallengerPointPort, saveChallengerPort);
        order.verify(saveChallengerPointPort).deleteAllByChallengerId(1L);
        order.verify(saveChallengerPort).delete(challenger);
    }

    @Test
    @DisplayName("비활성 챌린저에게 상벌점을 부여할 수 없다")
    void 비활성_챌린저에게_상벌점을_부여할_수_없다() {
        given(loadChallengerPort.getById(1L)).willReturn(challenger(1L, ChallengerStatus.WITHDRAWN));

        assertThatThrownBy(() -> sut.grantChallengerPoint(GrantChallengerPointCommand.builder()
            .challengerId(1L)
            .pointType(PointType.CUSTOM)
            .pointValue(1)
            .description("조정")
            .build()))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.CHALLENGER_NOT_ACTIVE);

        then(saveChallengerPort).should(never()).save(any());
        then(saveChallengerPointPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("상벌점은 Challenger 컬렉션 cascade 없이 Point port로 저장한다")
    void 상벌점은_Challenger_컬렉션_cascade_없이_Point_port로_저장한다() {
        Challenger challenger = challenger(1L, ChallengerStatus.ACTIVE);
        given(loadChallengerPort.getById(1L)).willReturn(challenger);

        sut.grantChallengerPoint(GrantChallengerPointCommand.builder()
            .challengerId(1L)
            .pointType(PointType.CUSTOM)
            .pointValue(3)
            .description("기여")
            .build());

        ArgumentCaptor<ChallengerPoint> captor = ArgumentCaptor.forClass(ChallengerPoint.class);
        then(saveChallengerPointPort).should().save(captor.capture());
        assertThat(captor.getValue().getChallengerId()).isEqualTo(1L);
        assertThat(captor.getValue().getPointValue()).isEqualTo(3.0);
        then(saveChallengerPort).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @ValueSource(longs = {10L, 11L})
    @DisplayName("기수 ID와 관계없이 10기부터 기존 우수 워크북 상점을 부여할 수 없다")
    void 기수_ID와_관계없이_10기부터_기존_우수_워크북_상점을_부여할_수_없다(Long generation) {
        // Given
        given(loadChallengerPort.getById(1L)).willReturn(challenger(1L, ChallengerStatus.ACTIVE, 2L));
        given(getGisuUseCase.getById(2L)).willReturn(gisu(2L, generation));

        // When & Then
        assertThatThrownBy(() -> sut.grantChallengerPoint(GrantChallengerPointCommand.builder()
            .challengerId(1L)
            .pointType(PointType.BEST_WORKBOOK)
            .description("우수 워크북")
            .build()))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.LEGACY_POINT_TYPE_NOT_ALLOWED);

        then(saveChallengerPointPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("기수 ID가 10보다 커도 9기는 기존 우수 워크북 상점을 부여할 수 있다")
    void 기수_ID가_10보다_커도_9기는_기존_우수_워크북_상점을_부여할_수_있다() {
        // Given
        given(loadChallengerPort.getById(1L)).willReturn(challenger(1L, ChallengerStatus.ACTIVE, 99L));
        given(getGisuUseCase.getById(99L)).willReturn(gisu(99L, 9L));

        // When
        sut.grantChallengerPoint(GrantChallengerPointCommand.builder()
            .challengerId(1L)
            .pointType(PointType.BEST_WORKBOOK)
            .description("우수 워크북")
            .build());

        // Then
        ArgumentCaptor<ChallengerPoint> captor = ArgumentCaptor.forClass(ChallengerPoint.class);
        then(saveChallengerPointPort).should().save(captor.capture());
        assertThat(captor.getValue().getPointValue()).isEqualTo(-0.5);
        then(getGisuUseCase).should().getById(99L);
    }

    @Test
    @DisplayName("새 우수 워크북 상점은 기수 조회 없이 정상 배점으로 저장한다")
    void 새_우수_워크북_상점은_기수_조회_없이_정상_배점으로_저장한다() {
        // Given
        given(loadChallengerPort.getById(1L)).willReturn(challenger(1L, ChallengerStatus.ACTIVE, 2L));

        // When
        sut.grantChallengerPoint(GrantChallengerPointCommand.builder()
            .challengerId(1L)
            .pointType(PointType.BEST_WORKBOOK_V2)
            .pointValue(2)
            .description("우수 워크북")
            .build());

        // Then
        ArgumentCaptor<ChallengerPoint> captor = ArgumentCaptor.forClass(ChallengerPoint.class);
        then(saveChallengerPointPort).should().save(captor.capture());
        assertThat(captor.getValue().getPointValue()).isEqualTo(2.0);
        then(getGisuUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("상벌점 일괄 부여는 Point port로 한 번에 저장한다")
    void 상벌점_일괄_부여는_Point_port로_한_번에_저장한다() {
        Challenger challenger = challenger(1L, ChallengerStatus.ACTIVE);
        given(environment.getActiveProfiles()).willReturn(new String[]{"test"});
        given(loadChallengerPort.getAllByIds(java.util.Set.of(1L))).willReturn(List.of(challenger));
        given(getGisuUseCase.batchGetByIds(List.of(9L))).willReturn(List.of(gisu(9L, 9L)));

        sut.grantChallengerPointBulk(List.of(
            GrantChallengerPointCommand.builder()
                .challengerId(1L)
                .pointType(PointType.BEST_WORKBOOK)
                .description("워크북")
                .build(),
            GrantChallengerPointCommand.builder()
                .challengerId(1L)
                .pointType(PointType.WARNING)
                .description("경고")
                .build()
        ));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ChallengerPoint>> captor = ArgumentCaptor.forClass(List.class);
        then(saveChallengerPointPort).should().saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2)
            .allSatisfy(point -> assertThat(point.getChallengerId()).isEqualTo(1L));
        then(saveChallengerPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("일괄 부여는 직접 지정한 상점과 벌점 값을 보존한다")
    void 일괄_부여는_직접_지정한_상점과_벌점_값을_보존한다() {
        // Given
        given(environment.getActiveProfiles()).willReturn(new String[]{"test"});
        given(loadChallengerPort.getAllByIds(Set.of(1L)))
            .willReturn(List.of(challenger(1L, ChallengerStatus.ACTIVE)));

        // When
        sut.grantChallengerPointBulk(List.of(
            GrantChallengerPointCommand.builder()
                .challengerId(1L).pointType(PointType.CUSTOM).pointValue(3).description("상점").build(),
            GrantChallengerPointCommand.builder()
                .challengerId(1L).pointType(PointType.CUSTOM).pointValue(-2).description("벌점").build()
        ));

        // Then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ChallengerPoint>> captor = ArgumentCaptor.forClass(List.class);
        then(saveChallengerPointPort).should().saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(ChallengerPoint::getPointValue).containsExactly(3.0, -2.0);
        then(getGisuUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("일괄 부여에 잘못된 고정 배점이 있으면 아무 상벌점도 저장하지 않는다")
    void 일괄_부여에_잘못된_고정_배점이_있으면_아무_상벌점도_저장하지_않는다() {
        // Given
        given(environment.getActiveProfiles()).willReturn(new String[]{"test"});
        given(loadChallengerPort.getAllByIds(Set.of(1L)))
            .willReturn(List.of(challenger(1L, ChallengerStatus.ACTIVE)));

        // When & Then
        assertThatThrownBy(() -> sut.grantChallengerPointBulk(List.of(
            GrantChallengerPointCommand.builder()
                .challengerId(1L).pointType(PointType.BEST_WORKBOOK_V2).description("정상").build(),
            GrantChallengerPointCommand.builder()
                .challengerId(1L).pointType(PointType.BEST_WORKBOOK_V2).pointValue(9).description("오류").build()
        )))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.INVALID_POINT_VALUE);

        then(saveChallengerPointPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("일괄 부여는 기존 우수 워크북 대상 기수만 한 번 조회하고 10기부터 거절한다")
    void 일괄_부여는_기존_우수_워크북_대상_기수만_한_번_조회하고_10기부터_거절한다() {
        // Given
        given(environment.getActiveProfiles()).willReturn(new String[]{"test"});
        given(loadChallengerPort.getAllByIds(Set.of(1L, 2L, 3L)))
            .willReturn(List.of(
                challenger(1L, ChallengerStatus.ACTIVE, 2L),
                challenger(2L, ChallengerStatus.ACTIVE, 2L),
                challenger(3L, ChallengerStatus.ACTIVE, 99L)
            ));
        given(getGisuUseCase.batchGetByIds(List.of(2L))).willReturn(List.of(gisu(2L, 10L)));

        // When & Then
        assertThatThrownBy(() -> sut.grantChallengerPointBulk(List.of(
            GrantChallengerPointCommand.builder()
                .challengerId(1L).pointType(PointType.BEST_WORKBOOK).description("기존 상점").build(),
            GrantChallengerPointCommand.builder()
                .challengerId(2L).pointType(PointType.BEST_WORKBOOK).description("기존 상점").build(),
            GrantChallengerPointCommand.builder()
                .challengerId(3L).pointType(PointType.BEST_WORKBOOK_V2).description("신규 상점").build()
        )))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.LEGACY_POINT_TYPE_NOT_ALLOWED);

        then(getGisuUseCase).should().batchGetByIds(List.of(2L));
        then(getGisuUseCase).shouldHaveNoMoreInteractions();
        then(saveChallengerPointPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("상벌점 설명을 수정한다")
    void 상벌점_설명을_수정한다() {
        Challenger challenger = challenger(1L, ChallengerStatus.ACTIVE);
        ChallengerPoint point = ChallengerPoint.create(challenger, PointType.CUSTOM, 1, "기존");
        given(loadChallengerPointPort.getById(10L)).willReturn(point);

        sut.updateChallengerPoint(UpdateChallengerPointCommand.of(10L, "수정"));

        assertThat(point.getDescription()).isEqualTo("수정");
        then(saveChallengerPointPort).should().save(point);
    }

    private Challenger challenger(Long id, ChallengerStatus status) {
        return challenger(id, status, 9L);
    }

    private Challenger challenger(Long id, ChallengerStatus status, Long gisuId) {
        Challenger challenger = Challenger.builder()
            .memberId(1L)
            .part(ChallengerPart.SPRINGBOOT)
            .gisuId(gisuId)
            .build();
        ReflectionTestUtils.setField(challenger, "id", id);
        ReflectionTestUtils.setField(challenger, "status", status);
        return challenger;
    }

    private GisuInfo gisu(Long gisuId, Long generation) {
        return new GisuInfo(gisuId, generation, Instant.EPOCH, Instant.EPOCH.plusSeconds(1), false);
    }
}
