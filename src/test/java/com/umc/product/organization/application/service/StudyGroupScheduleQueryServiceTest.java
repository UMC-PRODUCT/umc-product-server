package com.umc.product.organization.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.umc.product.organization.application.port.out.query.LoadStudyGroupSchedulePort;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudyGroupScheduleQueryServiceTest {

    @Mock
    LoadStudyGroupSchedulePort loadStudyGroupSchedulePort;

    @InjectMocks
    StudyGroupScheduleQueryService sut;

    @Test
    void findScheduleIdsByStudyGroupIds_정상_위임() {
        // given
        List<Long> groupIds = List.of(1L, 2L);
        given(loadStudyGroupSchedulePort.findScheduleIdsByStudyGroupIds(groupIds))
            .willReturn(Set.of(100L, 200L, 300L));

        // when
        Set<Long> result = sut.findScheduleIdsByStudyGroupIds(groupIds);

        // then
        assertThat(result).containsExactlyInAnyOrder(100L, 200L, 300L);
    }

    @Test
    void findScheduleIdsByStudyGroupIds_빈_입력이면_port_호출없이_빈_Set() {
        // when
        Set<Long> result = sut.findScheduleIdsByStudyGroupIds(List.of());

        // then
        assertThat(result).isEmpty();
        verify(loadStudyGroupSchedulePort, never()).findScheduleIdsByStudyGroupIds(any());
    }

    @Test
    void findScheduleIdsByStudyGroupIds_null_입력이면_port_호출없이_빈_Set() {
        // when
        Set<Long> result = sut.findScheduleIdsByStudyGroupIds(null);

        // then
        assertThat(result).isEmpty();
        verify(loadStudyGroupSchedulePort, never()).findScheduleIdsByStudyGroupIds(any());
    }
}
