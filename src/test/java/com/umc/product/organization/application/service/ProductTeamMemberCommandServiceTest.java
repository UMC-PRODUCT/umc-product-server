package com.umc.product.organization.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.command.dto.ProductTeamActivityCommand;
import com.umc.product.organization.application.port.in.command.dto.ReplaceProductTeamMemberActivitiesCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamMemberProfileCommand;
import com.umc.product.organization.application.port.out.command.SaveProductTeamMemberPort;
import com.umc.product.organization.application.port.out.command.SaveProductTeamMembershipPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamMemberPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamMembershipPort;
import com.umc.product.organization.domain.ProductTeamMember;
import com.umc.product.organization.domain.enums.ProductTeamPart;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamRole;
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
    LoadProductTeamMembershipPort loadProductTeamMembershipPort;
    @Mock
    SaveProductTeamMembershipPort saveProductTeamMembershipPort;
    @Mock
    LoadProductTeamGenerationPort loadProductTeamGenerationPort;
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
        given(getFileUseCase.existsById("product-profile")).willReturn(true);

        sut.updateProfile(UpdateProductTeamMemberProfileCommand.of(1L, 100L, "새 소개", "product-profile"));

        then(saveProductTeamMemberPort).should().save(member);
    }

    @Test
    void 본인은_활동_기록을_수정할_수_없다() {
        ProductTeamMember member = productTeamMember(1L, 100L);
        given(loadProductTeamMemberPort.getById(1L)).willReturn(member);
        given(productTeamAccessPolicy.canManageAllGenerations(100L, List.of(10L))).willReturn(false);

        ReplaceProductTeamMemberActivitiesCommand command = ReplaceProductTeamMemberActivitiesCommand.of(
            1L,
            100L,
            List.of(ProductTeamActivityCommand.of(
                10L,
                ProductTeamPart.MOBILE,
                ProductTeamRole.MEMBER,
                ProductTeamPosition.ANDROID_DEVELOPER
            ))
        );

        assertThatThrownBy(() -> sut.replaceActivities(command))
            .isInstanceOf(BusinessException.class)
            .extracting("baseCode")
            .isEqualTo(OrganizationErrorCode.PRODUCT_TEAM_ACCESS_DENIED);
        then(saveProductTeamMembershipPort).should(never()).saveAll(any());
    }

    private ProductTeamMember productTeamMember(Long id, Long memberId) {
        ProductTeamMember member = ProductTeamMember.create(memberId, "소개", null);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
