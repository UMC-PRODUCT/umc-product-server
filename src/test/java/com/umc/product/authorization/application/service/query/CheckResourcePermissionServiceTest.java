package com.umc.product.authorization.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionInfo;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Disabled
@ExtendWith(MockitoExtension.class)
class CheckResourcePermissionServiceTest {

    @Mock
    CheckPermissionUseCase checkPermissionUseCase;

    @InjectMocks
    CheckResourcePermissionService sut;

    private static final Long MEMBER_ID = 1L;
    private static final Long RESOURCE_ID = 100L;

    @Nested
    @DisplayName("hasPermission")
    class HasPermissionTest {

        @Test
        void 특정_리소스에_대한_모든_권한을_조회한다() {
            // given
            ResourceType resourceType = ResourceType.NOTICE;
            // NOTICE supports: READ, DELETE, CHECK

            given(checkPermissionUseCase.check(eq(MEMBER_ID),
                eq(ResourcePermission.of(resourceType, String.valueOf(RESOURCE_ID), PermissionType.READ))))
                .willReturn(true);
            given(checkPermissionUseCase.check(eq(MEMBER_ID),
                eq(ResourcePermission.of(resourceType, String.valueOf(RESOURCE_ID), PermissionType.DELETE))))
                .willReturn(false);
            given(checkPermissionUseCase.check(eq(MEMBER_ID),
                eq(ResourcePermission.of(resourceType, String.valueOf(RESOURCE_ID), PermissionType.CHECK))))
                .willReturn(false);

            // when
            ResourcePermissionInfo result = sut.hasPermission(MEMBER_ID, resourceType, RESOURCE_ID);

            // then
            assertThat(result.resourceType()).isEqualTo(ResourceType.NOTICE);
            assertThat(result.resourceId()).isEqualTo(RESOURCE_ID);
            assertThat(result.permissions()).containsEntry(PermissionType.READ, true);
            assertThat(result.permissions()).containsEntry(PermissionType.DELETE, false);
            assertThat(result.permissions()).containsEntry(PermissionType.CHECK, false);
        }

        @Test
        void 모든_권한이_있는_경우_전부_true를_반환한다() {
            // given
            ResourceType resourceType = ResourceType.NOTICE;

            given(checkPermissionUseCase.check(eq(MEMBER_ID), any(ResourcePermission.class)))
                .willReturn(true);

            // when
            ResourcePermissionInfo result = sut.hasPermission(MEMBER_ID, resourceType, RESOURCE_ID);

            // then
            assertThat(result.permissions().values()).allMatch(hasPermission -> hasPermission);
        }

        @Test
        void resourceId가_null이면_리소스_타입_전체에_대한_권한을_조회한다() {
            // given
            ResourceType resourceType = ResourceType.WORKBOOK_SUBMISSION;
            // WORKBOOK_SUBMISSION supports: READ

            given(checkPermissionUseCase.check(eq(MEMBER_ID),
                eq(ResourcePermission.ofType(resourceType, PermissionType.READ))))
                .willReturn(true);

            // when
            ResourcePermissionInfo result = sut.hasPermission(MEMBER_ID, resourceType, (Long) null);

            // then
            assertThat(result.resourceId()).isNull();
            assertThat(result.permissions()).containsEntry(PermissionType.READ, true);
        }

        @Test
        void 리소스_타입이_지원하는_권한_개수만큼_결과를_반환한다() {
            // given
            ResourceType resourceType = ResourceType.NOTICE;
            // NOTICE supports: READ, DELETE, CHECK (3개)

            given(checkPermissionUseCase.check(eq(MEMBER_ID), any(ResourcePermission.class)))
                .willReturn(false);

            // when
            ResourcePermissionInfo result = sut.hasPermission(MEMBER_ID, resourceType, RESOURCE_ID);

            // then
            assertThat(result.permissions()).hasSize(resourceType.getSupportedPermissions().size());
        }

        @Test
        void Evaluator가_구현되지_않은_리소스_타입이면_예외가_발생한다() {
            // given
            ResourceType resourceType = ResourceType.CURRICULUM;

            // when & then
            assertThatThrownBy(() -> sut.hasPermission(MEMBER_ID, resourceType, RESOURCE_ID))
                .isInstanceOf(AuthorizationDomainException.class);
        }
    }
}
