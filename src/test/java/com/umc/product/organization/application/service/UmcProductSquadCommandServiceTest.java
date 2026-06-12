package com.umc.product.organization.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.organization.application.port.in.command.dto.ReplaceUmcProductSquadParticipantsCommand;
import com.umc.product.organization.application.port.in.command.dto.UmcProductSquadParticipantCommand;
import com.umc.product.organization.application.port.out.command.SaveUmcProductSquadParticipantPort;
import com.umc.product.organization.application.port.out.command.SaveUmcProductSquadPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductMemberPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductSquadPort;
import com.umc.product.organization.domain.UmcProductMember;
import com.umc.product.organization.domain.UmcProductSquad;
import com.umc.product.organization.domain.enums.UmcProductPosition;
import com.umc.product.organization.domain.enums.UmcProductSquadRole;

@ExtendWith(MockitoExtension.class)
class UmcProductSquadCommandServiceTest {

    @Mock
    LoadUmcProductSquadPort loadUmcProductSquadPort;
    @Mock
    SaveUmcProductSquadPort saveUmcProductSquadPort;
    @Mock
    LoadUmcProductMemberPort loadUmcProductMemberPort;
    @Mock
    SaveUmcProductSquadParticipantPort saveUmcProductSquadParticipantPort;
    @Mock
    UmcProductAccessPolicy umcProductAccessPolicy;

    @InjectMocks
    UmcProductSquadCommandService sut;

    @Test
    void Squad_참여자_교체_시_UMC_Product_인원을_벌크_조회한다() {
        UmcProductSquad squad = squad(10L);
        UmcProductMember member = member(20L);
        given(umcProductAccessPolicy.canManageUmcProduct(999L)).willReturn(true);
        given(loadUmcProductSquadPort.getById(10L)).willReturn(squad);
        given(loadUmcProductMemberPort.listByIds(eq(Set.of(20L)))).willReturn(List.of(member));

        sut.replaceParticipants(ReplaceUmcProductSquadParticipantsCommand.of(
            10L,
            999L,
            List.of(UmcProductSquadParticipantCommand.of(
                20L,
                UmcProductSquadRole.SQUAD_LEAD,
                UmcProductPosition.PRODUCT_OWNER,
                "모집 정책",
                null
            ))
        ));

        then(loadUmcProductMemberPort).should(never()).getById(any());
        then(saveUmcProductSquadParticipantPort).should().saveAll(any());
    }

    private UmcProductSquad squad(Long id) {
        UmcProductSquad squad = UmcProductSquad.create("RECRUIT", "모집 Squad", null, null, null, 1, true);
        ReflectionTestUtils.setField(squad, "id", id);
        return squad;
    }

    private UmcProductMember member(Long id) {
        UmcProductMember member = UmcProductMember.create(100L, "소개", null);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
