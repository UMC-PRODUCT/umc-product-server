package com.umc.product.organization.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.organization.application.port.in.command.dto.UpdateUmcProductFunctionalUnitCommand;
import com.umc.product.organization.application.port.out.command.SaveUmcProductFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductGenerationPort;
import com.umc.product.organization.domain.UmcProductFunctionalUnit;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;
import com.umc.product.organization.exception.OrganizationErrorCode;

@ExtendWith(MockitoExtension.class)
class UmcProductFunctionalUnitCommandServiceTest {

    @Mock
    LoadUmcProductGenerationPort loadUmcProductGenerationPort;
    @Mock
    LoadUmcProductFunctionalUnitPort loadUmcProductFunctionalUnitPort;
    @Mock
    SaveUmcProductFunctionalUnitPort saveUmcProductFunctionalUnitPort;
    @Mock
    UmcProductAccessPolicy umcProductAccessPolicy;

    @InjectMocks
    UmcProductFunctionalUnitCommandService sut;

    @Test
    void 기능_조직은_자기_자신을_상위_조직으로_지정할_수_없다() {
        UmcProductFunctionalUnit functionalUnit = functionalUnit(20L, 10L);
        given(umcProductAccessPolicy.canManageUmcProduct(999L)).willReturn(true);
        given(loadUmcProductFunctionalUnitPort.getById(20L)).willReturn(functionalUnit);

        assertThatThrownBy(() -> sut.update(UpdateUmcProductFunctionalUnitCommand.of(
            20L,
            999L,
            20L,
            UmcProductFunctionalUnitType.PART,
            "SERVER",
            "Server 파트",
            null,
            1,
            true
        )))
            .isInstanceOf(BusinessException.class)
            .extracting("baseCode")
            .isEqualTo(OrganizationErrorCode.UMC_PRODUCT_FUNCTIONAL_UNIT_PARENT_INVALID);

        then(saveUmcProductFunctionalUnitPort).should(never()).save(any());
    }

    private UmcProductFunctionalUnit functionalUnit(Long id, Long generationId) {
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
