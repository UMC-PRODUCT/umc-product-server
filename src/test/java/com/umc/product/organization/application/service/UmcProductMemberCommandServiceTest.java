package com.umc.product.organization.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import com.umc.product.global.exception.BusinessException;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.command.dto.ReplaceUmcProductMemberFunctionalMembershipsCommand;
import com.umc.product.organization.application.port.in.command.dto.UmcProductFunctionalMembershipCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateUmcProductMemberProfileCommand;
import com.umc.product.organization.application.port.out.command.SaveUmcProductFunctionalMembershipPort;
import com.umc.product.organization.application.port.out.command.SaveUmcProductMemberPort;
import com.umc.product.organization.application.port.out.command.SaveUmcProductSquadParticipantPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductMemberPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductSquadPort;
import com.umc.product.organization.domain.UmcProductFunctionalUnit;
import com.umc.product.organization.domain.UmcProductGeneration;
import com.umc.product.organization.domain.UmcProductMember;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;
import com.umc.product.organization.domain.enums.UmcProductPosition;
import com.umc.product.organization.exception.OrganizationErrorCode;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;

@ExtendWith(MockitoExtension.class)
class UmcProductMemberCommandServiceTest {

    @Mock
    LoadUmcProductMemberPort loadUmcProductMemberPort;
    @Mock
    SaveUmcProductMemberPort saveUmcProductMemberPort;
    @Mock
    SaveUmcProductFunctionalMembershipPort saveUmcProductFunctionalMembershipPort;
    @Mock
    SaveUmcProductSquadParticipantPort saveUmcProductSquadParticipantPort;
    @Mock
    LoadUmcProductGenerationPort loadUmcProductGenerationPort;
    @Mock
    LoadUmcProductFunctionalUnitPort loadUmcProductFunctionalUnitPort;
    @Mock
    LoadUmcProductSquadPort loadUmcProductSquadPort;
    @Mock
    GetMemberUseCase getMemberUseCase;
    @Mock
    GetFileUseCase getFileUseCase;
    @Mock
    UmcProductAccessPolicy umcProductAccessPolicy;

    @InjectMocks
    UmcProductMemberCommandService sut;

    @Test
    void 본인은_프로필만_수정할_수_있다() {
        UmcProductMember member = umcProductMember(1L, 100L);
        given(loadUmcProductMemberPort.getById(1L)).willReturn(member);
        given(umcProductAccessPolicy.canManageMemberProfile(100L, 100L)).willReturn(true);
        given(getFileUseCase.existsById("product-profile")).willReturn(true);

        sut.updateProfile(UpdateUmcProductMemberProfileCommand.of(1L, 100L, "새 소개", "product-profile"));

        then(saveUmcProductMemberPort).should().save(member);
    }

    @Test
    void 본인은_기능_조직_멤버십을_수정할_수_없다() {
        UmcProductMember member = umcProductMember(1L, 100L);
        given(loadUmcProductMemberPort.getById(1L)).willReturn(member);
        given(umcProductAccessPolicy.canManageUmcProduct(100L)).willReturn(false);

        ReplaceUmcProductMemberFunctionalMembershipsCommand command =
            ReplaceUmcProductMemberFunctionalMembershipsCommand.of(
                1L,
                100L,
                List.of(UmcProductFunctionalMembershipCommand.of(
                    10L,
                    20L,
                    UmcProductFunctionalRole.PART_LEAD,
                    UmcProductPosition.SERVER_DEVELOPER,
                    "API 설계",
                    "Server 파트 API 계약을 담당합니다."
                ))
            );

        assertThatThrownBy(() -> sut.replaceFunctionalMemberships(command))
            .isInstanceOf(BusinessException.class)
            .extracting("baseCode")
            .isEqualTo(OrganizationErrorCode.UMC_PRODUCT_ACCESS_DENIED);
        then(saveUmcProductFunctionalMembershipPort).should(never()).saveAll(any());
    }

    @Test
    void 기능_조직_멤버십_교체_시_기수와_기능_조직을_벌크_조회한다() {
        UmcProductMember member = umcProductMember(1L, 100L);
        UmcProductGeneration generation = umcProductGeneration(10L);
        UmcProductFunctionalUnit functionalUnit = umcProductFunctionalUnit(20L, 10L);
        given(loadUmcProductMemberPort.getById(1L)).willReturn(member);
        given(umcProductAccessPolicy.canManageUmcProduct(999L)).willReturn(true);
        given(loadUmcProductGenerationPort.listByIds(eq(Set.of(10L)))).willReturn(List.of(generation));
        given(loadUmcProductFunctionalUnitPort.listByIds(eq(Set.of(20L)))).willReturn(List.of(functionalUnit));

        sut.replaceFunctionalMemberships(ReplaceUmcProductMemberFunctionalMembershipsCommand.of(
            1L,
            999L,
            List.of(UmcProductFunctionalMembershipCommand.of(
                10L,
                20L,
                UmcProductFunctionalRole.PART_LEAD,
                UmcProductPosition.SERVER_DEVELOPER,
                "API 설계",
                "Server 파트 API 계약을 담당합니다."
            ))
        ));

        then(loadUmcProductGenerationPort).should(never()).getById(any());
        then(loadUmcProductFunctionalUnitPort).should(never()).getById(any());
        then(saveUmcProductFunctionalMembershipPort).should().saveAll(any());
    }

    private UmcProductMember umcProductMember(Long id, Long memberId) {
        UmcProductMember member = UmcProductMember.create(memberId, "소개", null);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private UmcProductGeneration umcProductGeneration(Long id) {
        UmcProductGeneration generation = UmcProductGeneration.create(
            1L,
            java.time.Instant.parse("2026-01-01T00:00:00Z"),
            java.time.Instant.parse("2026-12-31T23:59:59Z"),
            false
        );
        ReflectionTestUtils.setField(generation, "id", id);
        return generation;
    }

    private UmcProductFunctionalUnit umcProductFunctionalUnit(Long id, Long generationId) {
        UmcProductFunctionalUnit functionalUnit = UmcProductFunctionalUnit.create(
            generationId,
            null,
            UmcProductFunctionalUnitType.PART,
            "SERVER",
            "Server 파트",
            null,
            1,
            true
        );
        ReflectionTestUtils.setField(functionalUnit, "id", id);
        return functionalUnit;
    }
}
