package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.project.application.port.in.command.ResyncMatchingRoundScheduleUseCase;
import com.umc.product.test.application.port.in.command.dto.DeleteSeedProjectDataCommand;
import com.umc.product.test.application.port.in.command.dto.DeleteSeedProjectDataResult;
import com.umc.product.test.application.port.out.DeleteSeedProjectDataPort;
import com.umc.product.test.application.port.out.dto.ProjectDataDeletionCounts;

@ExtendWith(MockitoExtension.class)
class ProjectSeedDataCleanupServiceTest {

    @Mock
    GetGisuUseCase getGisuUseCase;
    @Mock
    DeleteSeedProjectDataPort deleteSeedProjectDataPort;
    @Mock
    ResyncMatchingRoundScheduleUseCase resyncMatchingRoundScheduleUseCase;
    @Mock
    PlatformTransactionManager transactionManager;

    ProjectSeedDataCleanupService sut;

    @BeforeEach
    void setUp() {
        sut = new ProjectSeedDataCleanupService(getGisuUseCase, deleteSeedProjectDataPort,
            resyncMatchingRoundScheduleUseCase, transactionManager);
    }

    @Test
    @DisplayName("gisuId가 지정되면 해당 기수 프로젝트 데이터를 삭제한다")
    void deleteSpecifiedGisuProjectData() {
        // Given
        ProjectDataDeletionCounts counts = ProjectDataDeletionCounts.builder()
            .deletedProjects(2)
            .deletedProjectMembers(20)
            .build();
        given(deleteSeedProjectDataPort.deleteByGisuId(9L)).willReturn(counts);

        // When
        DeleteSeedProjectDataResult result = sut.delete(DeleteSeedProjectDataCommand.of(9L));

        // Then
        assertThat(result.gisuId()).isEqualTo(9L);
        assertThat(result.deletedProjects()).isEqualTo(2);
        assertThat(result.deletedProjectMembers()).isEqualTo(20);
        then(getGisuUseCase).should().getById(9L);
        then(getGisuUseCase).should(never()).getActiveGisuId();
        then(deleteSeedProjectDataPort).should().deleteByGisuId(9L);
    }

    @Test
    @DisplayName("gisuId가 null이면 활성 기수 프로젝트 데이터를 삭제한다")
    void deleteActiveGisuProjectData() {
        // Given
        given(getGisuUseCase.getActiveGisuId()).willReturn(10L);
        ProjectDataDeletionCounts counts = ProjectDataDeletionCounts.builder()
            .deletedProjects(1)
            .build();
        given(deleteSeedProjectDataPort.deleteByGisuId(10L)).willReturn(counts);

        // When
        DeleteSeedProjectDataResult result = sut.delete(DeleteSeedProjectDataCommand.of(null));

        // Then
        assertThat(result.gisuId()).isEqualTo(10L);
        assertThat(result.deletedProjects()).isEqualTo(1);
        then(getGisuUseCase).should().getActiveGisuId();
        then(getGisuUseCase).should().getById(10L);
        then(deleteSeedProjectDataPort).should().deleteByGisuId(10L);
    }

    @Test
    @DisplayName("삭제가 커밋된 뒤 지운 차수마다 알람을 다시 맞춰 남은 알람을 취소한다")
    void resyncsDeletedRoundsAfterCommit() {
        // Given
        ProjectDataDeletionCounts counts = ProjectDataDeletionCounts.builder()
            .deletedProjectMatchingRounds(2)
            .deletedProjectMatchingRoundIds(List.of(31L, 32L))
            .build();
        given(deleteSeedProjectDataPort.deleteByGisuId(9L)).willReturn(counts);

        // When
        sut.delete(DeleteSeedProjectDataCommand.of(9L));

        // Then
        InOrder order = inOrder(transactionManager, resyncMatchingRoundScheduleUseCase);
        order.verify(transactionManager).commit(any());
        order.verify(resyncMatchingRoundScheduleUseCase).resync(31L);
        order.verify(resyncMatchingRoundScheduleUseCase).resync(32L);
    }
}
