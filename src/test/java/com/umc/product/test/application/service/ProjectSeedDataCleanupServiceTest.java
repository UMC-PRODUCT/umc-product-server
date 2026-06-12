package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
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

    ProjectSeedDataCleanupService sut;

    @BeforeEach
    void setUp() {
        sut = new ProjectSeedDataCleanupService(getGisuUseCase, deleteSeedProjectDataPort);
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
}
