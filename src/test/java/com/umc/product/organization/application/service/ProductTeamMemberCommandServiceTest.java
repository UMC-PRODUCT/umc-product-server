package com.umc.product.organization.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.command.dto.ProductTeamFunctionalMembershipCommand;
import com.umc.product.organization.application.port.in.command.dto.ReplaceProductTeamMemberFunctionalMembershipsCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamMemberProfileCommand;
import com.umc.product.organization.application.port.out.command.SaveProductTeamFunctionalMembershipPort;
import com.umc.product.organization.application.port.out.command.SaveProductTeamMemberPort;
import com.umc.product.organization.application.port.out.command.SaveProductTeamSquadParticipantPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamMemberPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamSquadPort;
import com.umc.product.organization.domain.ProductTeamMember;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalRole;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.exception.OrganizationErrorCode;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProductTeamMemberCommandServiceTest {

    @Mock
    LoadProductTeamMemberPort loadProductTeamMemberPort;
    @Mock
    SaveProductTeamMemberPort saveProductTeamMemberPort;
    @Mock
    SaveProductTeamFunctionalMembershipPort saveProductTeamFunctionalMembershipPort;
    @Mock
    SaveProductTeamSquadParticipantPort saveProductTeamSquadParticipantPort;
    @Mock
    LoadProductTeamGenerationPort loadProductTeamGenerationPort;
    @Mock
    LoadProductTeamFunctionalUnitPort loadProductTeamFunctionalUnitPort;
    @Mock
    LoadProductTeamSquadPort loadProductTeamSquadPort;
    @Mock
    GetMemberUseCase getMemberUseCase;
    @Mock
    GetFileUseCase getFileUseCase;
    @Mock
    ProductTeamAccessPolicy productTeamAccessPolicy;

    @InjectMocks
    ProductTeamMemberCommandService sut;

    @Test
    void 본인은_프로필만_수정할_수_있다() {
        ProductTeamMember member = productTeamMember(1L, 100L);
        given(loadProductTeamMemberPort.getById(1L)).willReturn(member);
        given(productTeamAccessPolicy.canManageMemberProfile(100L, 100L)).willReturn(true);
        given(getFileUseCase.existsById("product-profile")).willReturn(true);

        sut.updateProfile(UpdateProductTeamMemberProfileCommand.of(1L, 100L, "새 소개", "product-profile"));

        then(saveProductTeamMemberPort).should().save(member);
    }

    @Test
    void 본인은_기능_조직_멤버십을_수정할_수_없다() {
        ProductTeamMember member = productTeamMember(1L, 100L);
        given(loadProductTeamMemberPort.getById(1L)).willReturn(member);
        given(productTeamAccessPolicy.canManageProductTeam(100L)).willReturn(false);

        ReplaceProductTeamMemberFunctionalMembershipsCommand command =
            ReplaceProductTeamMemberFunctionalMembershipsCommand.of(
                1L,
                100L,
                List.of(ProductTeamFunctionalMembershipCommand.of(
                    10L,
                    20L,
                    ProductTeamFunctionalRole.PART_LEAD,
                    ProductTeamPosition.SERVER_DEVELOPER,
                    "API 설계",
                    "Server 파트 API 계약을 담당합니다."
                ))
            );

        assertThatThrownBy(() -> sut.replaceFunctionalMemberships(command))
            .isInstanceOf(BusinessException.class)
            .extracting("baseCode")
            .isEqualTo(OrganizationErrorCode.PRODUCT_TEAM_ACCESS_DENIED);
        then(saveProductTeamFunctionalMembershipPort).should(never()).saveAll(any());
    }

    private ProductTeamMember productTeamMember(Long id, Long memberId) {
        ProductTeamMember member = ProductTeamMember.create(memberId, "소개", null);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
