package com.umc.product.organization.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductMemberInfo;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductMemberSearchCondition;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalMembershipPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductMemberPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductSquadParticipantPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductSquadPort;
import com.umc.product.organization.domain.UmcProductMember;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;

@ExtendWith(MockitoExtension.class)
@DisplayName("UmcProductMemberQueryService")
class UmcProductMemberQueryServiceTest {

    @Mock
    LoadUmcProductMemberPort loadUmcProductMemberPort;
    @Mock
    LoadUmcProductFunctionalMembershipPort loadUmcProductFunctionalMembershipPort;
    @Mock
    LoadUmcProductSquadParticipantPort loadUmcProductSquadParticipantPort;
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

    @InjectMocks
    UmcProductMemberQueryService sut;

    @Test
    @DisplayName("search는 프로덕트 프로필 이미지가 없는 멤버를 조회할 수 있다")
    void search는_프로덕트_프로필_이미지가_없는_멤버를_조회할_수_있다() {
        UmcProductMember member = umcProductMember(1L, 100L, null);
        PageRequest pageable = PageRequest.of(0, 10);
        UmcProductMemberSearchCondition condition = UmcProductMemberSearchCondition.of(
            null, null, null, null, null, null
        );
        given(loadUmcProductMemberPort.searchIds(condition, pageable))
            .willReturn(new PageImpl<>(List.of(1L), pageable, 1));
        given(loadUmcProductMemberPort.listByIds(List.of(1L))).willReturn(List.of(member));
        given(loadUmcProductFunctionalMembershipPort.listByUmcProductMemberIds(List.of(1L))).willReturn(List.of());
        given(loadUmcProductSquadParticipantPort.listByUmcProductMemberIds(List.of(1L))).willReturn(List.of());
        given(getMemberUseCase.findAllByIds(Set.of(100L))).willReturn(Map.of());

        Page<UmcProductMemberInfo> result = sut.search(condition, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().umcProductProfileImageId()).isNull();
        assertThat(result.getContent().getFirst().umcProductProfileImageUrl()).isNull();
        then(getFileUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("getById는 프로덕트 프로필 이미지가 없는 멤버를 조회할 수 있다")
    void getById는_프로덕트_프로필_이미지가_없는_멤버를_조회할_수_있다() {
        UmcProductMember member = umcProductMember(1L, 100L, null);
        given(loadUmcProductMemberPort.getById(1L)).willReturn(member);
        given(loadUmcProductFunctionalMembershipPort.listByUmcProductMemberId(1L)).willReturn(List.of());
        given(loadUmcProductSquadParticipantPort.listByUmcProductMemberId(1L)).willReturn(List.of());
        given(getMemberUseCase.findById(100L)).willReturn(Optional.empty());

        UmcProductMemberInfo result = sut.getById(1L);

        assertThat(result.umcProductProfileImageId()).isNull();
        assertThat(result.umcProductProfileImageUrl()).isNull();
        then(getFileUseCase).shouldHaveNoInteractions();
    }

    private UmcProductMember umcProductMember(Long id, Long memberId, String profileImageId) {
        UmcProductMember member = UmcProductMember.create(memberId, "소개", profileImageId);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
