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
    void 호출자_시야의_스터디_그룹IDs를_받아_매핑된_일정IDs를_반환() {
        // given — Schedule 팀 흐름:
        //   1) getStudyGroupUseCase.resolveOrganizationRoleScopes(memberId) → 회장단 + 파트장 scope
        //   2) getStudyGroupUseCase.findStudyGroupIds(scopes, gisuId) → visibleGroupIds = {1, 2}
        //   3) 우리(getStudyGroupScheduleUseCase) 가 그걸로 매핑된 일정 ID 들을 batch 조회
        List<Long> visibleGroupIds = List.of(1L, 2L);
        Set<Long> mappedScheduleIds = Set.of(100L, 200L, 300L);

        given(loadStudyGroupSchedulePort.findScheduleIdsByStudyGroupIds(visibleGroupIds))
            .willReturn(mappedScheduleIds);

        // when
        Set<Long> result = sut.findScheduleIdsByStudyGroupIds(visibleGroupIds);

        // then — Service 는 단순 합성 facade. 가공 없이 그대로 통과.
        assertThat(result).containsExactlyInAnyOrder(100L, 200L, 300L);
    }

    @Test
    void 권한_없는_사용자는_visible_그룹이_없어_DB_호출_없이_빈_Set() {
        // given — Schedule 팀 흐름:
        //   회장단도 파트장도 아닌 일반 챌린저:
        //   → resolveOrganizationRoleScopes 가 빈 리스트 → findStudyGroupIds 가 빈 Set → 빈 입력으로 우리 호출
        //   우리는 풀스캔 / 무용 IN() 쿼리 방지를 위해 즉시 빈 Set 반환

        // when
        Set<Long> result = sut.findScheduleIdsByStudyGroupIds(List.of());

        // then
        assertThat(result).isEmpty();
        verify(loadStudyGroupSchedulePort, never()).findScheduleIdsByStudyGroupIds(any());
    }

    @Test
    void 시야의_그룹에_등록된_일정이_없어도_정상_빈_Set_반환() {
        // given — 회장단/파트장 권한은 있어 visibleGroupIds 가 채워졌지만,
        //         그 그룹들에 아직 schedule 매핑이 안 된 경우 (새 그룹들).
        //         예외가 아니라 자연스러운 빈 결과여야 함.
        List<Long> visibleGroupIds = List.of(1L, 2L);
        given(loadStudyGroupSchedulePort.findScheduleIdsByStudyGroupIds(visibleGroupIds))
            .willReturn(Set.of());

        // when
        Set<Long> result = sut.findScheduleIdsByStudyGroupIds(visibleGroupIds);

        // then
        assertThat(result).isEmpty();
    }
}
