package com.umc.product.member.adapter.out.external.challenger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.member.application.port.out.dto.BusinessCardActivity;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessCardActivityAdapter")
class BusinessCardActivityAdapterTest {

    private static final Long MEMBER_ID = 1L;

    @Mock
    GetChallengerUseCase getChallengerUseCase;

    @Mock
    GetGisuUseCase getGisuUseCase;

    @InjectMocks
    BusinessCardActivityAdapter sut;

    @Test
    @DisplayName("챌린저 기록에 기수 번호를 붙여 활동 이력으로 반환한다")
    void 챌린저_기록에_기수_번호를_붙여_활동_이력으로_반환한다() {
        // given
        given(getChallengerUseCase.getAllBasicByMemberIds(Set.of(MEMBER_ID))).willReturn(Map.of(MEMBER_ID, List.of(
            new ChallengerBasicInfo(11L, MEMBER_ID, 100L, ChallengerPart.WEB, false, ChallengerStatus.GRADUATED),
            new ChallengerBasicInfo(12L, MEMBER_ID, 200L, null, false, ChallengerStatus.ACTIVE)
        )));
        given(getGisuUseCase.getByIds(Set.of(100L, 200L))).willReturn(List.of(gisu(100L, 8L)));

        // when
        List<BusinessCardActivity> activities = sut.listByMemberId(MEMBER_ID);

        // then
        // 기수 정보를 찾지 못한 기록은 기수 번호를 임의로 채우지 않는다.
        assertThat(activities).containsExactly(
            new BusinessCardActivity(100L, 8L, ChallengerPart.WEB),
            new BusinessCardActivity(200L, null, null)
        );
    }

    @Test
    @DisplayName("챌린저 기록이 없으면 기수를 조회하지 않고 빈 목록을 반환한다")
    void 챌린저_기록이_없으면_기수를_조회하지_않고_빈_목록을_반환한다() {
        // given
        given(getChallengerUseCase.getAllBasicByMemberIds(Set.of(MEMBER_ID))).willReturn(Map.of());

        // when & then
        assertThat(sut.listByMemberId(MEMBER_ID)).isEmpty();
        then(getGisuUseCase).should(never()).getByIds(anySet());
    }

    private GisuInfo gisu(Long gisuId, Long generation) {
        return new GisuInfo(gisuId, generation, null, null, false);
    }
}
