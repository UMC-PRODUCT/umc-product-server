package com.umc.product.authorization.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionQuery;
import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionInfo;
import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckResourcePermissionServiceTest {

    @Mock
    CheckPermissionUseCase checkPermissionUseCase;

    @Mock
    ResourcePermissionEvaluator noticeEvaluator;

    @Mock
    ResourcePermissionEvaluator workbookSubmissionEvaluator;

    CheckResourcePermissionService sut;

    @BeforeEach
    void setUp() {
        given(noticeEvaluator.supportedResourceType()).willReturn(ResourceType.NOTICE);
        given(workbookSubmissionEvaluator.supportedResourceType()).willReturn(ResourceType.WORKBOOK_SUBMISSION);

        sut = new CheckResourcePermissionService(
            checkPermissionUseCase,
            List.of(noticeEvaluator, workbookSubmissionEvaluator)
        );
    }

    private static final Long MEMBER_ID = 1L;
    private static final Long RESOURCE_ID = 100L;

    @Nested
    @DisplayName("권한이 있나요?")
    class HasPermissionTest {

        @Test
        void 특정_리소스에_대한_모든_권한을_조회한다() {
            // given
            ResourceType resourceType = ResourceType.NOTICE;
            // NOTICE supports: READ, EDIT, DELETE, CHECK

            given(checkPermissionUseCase.check(eq(MEMBER_ID),
                eq(ResourcePermission.of(resourceType, String.valueOf(RESOURCE_ID), PermissionType.READ))))
                .willReturn(true);
            given(checkPermissionUseCase.check(eq(MEMBER_ID),
                eq(ResourcePermission.of(resourceType, String.valueOf(RESOURCE_ID), PermissionType.EDIT))))
                .willReturn(false);
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
            assertThat(result.permissions()).containsEntry(PermissionType.EDIT, false);
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
            // NOTICE supports: READ, EDIT, DELETE, CHECK (4개)

            given(checkPermissionUseCase.check(eq(MEMBER_ID), any(ResourcePermission.class)))
                .willReturn(false);

            // when
            ResourcePermissionInfo result = sut.hasPermission(MEMBER_ID, resourceType, RESOURCE_ID);

            // then
            assertThat(result.permissions()).hasSize(resourceType.getSupportedPermissions().size());
        }

        @Test
        @DisplayName("특정 권한을 지정하면 해당 권한만 평가한다")
        void 특정_권한을_지정하면_해당_권한만_평가한다() {
            // given
            ResourceType resourceType = ResourceType.NOTICE;

            given(checkPermissionUseCase.check(eq(MEMBER_ID),
                eq(ResourcePermission.of(resourceType, String.valueOf(RESOURCE_ID), PermissionType.READ))))
                .willReturn(true);

            // when
            ResourcePermissionInfo result = sut.hasPermission(
                MEMBER_ID,
                resourceType,
                RESOURCE_ID,
                PermissionType.READ
            );

            // then
            assertThat(result.resourceType()).isEqualTo(ResourceType.NOTICE);
            assertThat(result.resourceId()).isEqualTo(RESOURCE_ID);
            assertThat(result.permissions()).containsOnlyKeys(PermissionType.READ);
            assertThat(result.permissions()).containsEntry(PermissionType.READ, true);
            verify(checkPermissionUseCase, never()).check(eq(MEMBER_ID),
                eq(ResourcePermission.of(resourceType, String.valueOf(RESOURCE_ID), PermissionType.EDIT)));
        }

        @Test
        @DisplayName("resourceId 없이 특정 권한을 지정하면 타입 단위 권한을 평가한다")
        void resourceId_없이_특정_권한을_지정하면_타입_단위_권한을_평가한다() {
            // given
            ResourceType resourceType = ResourceType.NOTICE;

            given(checkPermissionUseCase.check(eq(MEMBER_ID),
                eq(ResourcePermission.ofType(resourceType, PermissionType.CHECK))))
                .willReturn(false);

            // when
            ResourcePermissionInfo result = sut.hasPermission(
                MEMBER_ID,
                resourceType,
                null,
                PermissionType.CHECK
            );

            // then
            assertThat(result.resourceId()).isNull();
            assertThat(result.permissions()).containsOnlyKeys(PermissionType.CHECK);
            assertThat(result.permissions()).containsEntry(PermissionType.CHECK, false);
        }

        @Test
        @DisplayName("지원하지 않는 특정 권한을 지정하면 아무 권한도 평가하지 않고 예외가 발생한다")
        void 지원하지_않는_특정_권한을_지정하면_아무_권한도_평가하지_않고_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> sut.hasPermission(
                MEMBER_ID,
                ResourceType.WORKBOOK_SUBMISSION,
                RESOURCE_ID,
                PermissionType.DELETE
            ))
                .isInstanceOf(AuthorizationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthorizationErrorCode.INVALID_INPUT_VALUE);
            verify(checkPermissionUseCase, never()).check(eq(MEMBER_ID), any(ResourcePermission.class));
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

    @Nested
    @DisplayName("권한을 배치로 조회한다")
    class BatchHasPermissionTest {

        @Test
        @DisplayName("여러 리소스에 대한 특정 권한을 요청 순서대로 조회한다")
        void 여러_리소스에_대한_특정_권한을_요청_순서대로_조회한다() {
            // given
            given(checkPermissionUseCase.check(eq(MEMBER_ID),
                eq(ResourcePermission.of(ResourceType.NOTICE, 100L, PermissionType.READ))))
                .willReturn(true);
            given(checkPermissionUseCase.check(eq(MEMBER_ID),
                eq(ResourcePermission.of(ResourceType.NOTICE, 101L, PermissionType.READ))))
                .willReturn(false);
            given(checkPermissionUseCase.check(eq(MEMBER_ID),
                eq(ResourcePermission.of(ResourceType.WORKBOOK_SUBMISSION, 200L, PermissionType.READ))))
                .willReturn(true);

            List<ResourcePermissionQuery> queries = List.of(
                ResourcePermissionQuery.of(ResourceType.NOTICE, List.of(100L, 101L), List.of(PermissionType.READ)),
                ResourcePermissionQuery.of(ResourceType.WORKBOOK_SUBMISSION, List.of(200L), List.of(PermissionType.READ))
            );

            // when
            List<ResourcePermissionInfo> result = sut.batchHasPermission(MEMBER_ID, queries);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).resourceType()).isEqualTo(ResourceType.NOTICE);
            assertThat(result.get(0).resourceId()).isEqualTo(100L);
            assertThat(result.get(0).permissions()).containsEntry(PermissionType.READ, true);
            assertThat(result.get(1).resourceType()).isEqualTo(ResourceType.NOTICE);
            assertThat(result.get(1).resourceId()).isEqualTo(101L);
            assertThat(result.get(1).permissions()).containsEntry(PermissionType.READ, false);
            assertThat(result.get(2).resourceType()).isEqualTo(ResourceType.WORKBOOK_SUBMISSION);
            assertThat(result.get(2).resourceId()).isEqualTo(200L);
            assertThat(result.get(2).permissions()).containsEntry(PermissionType.READ, true);
        }

        @Test
        @DisplayName("지원하지 않는 권한이 하나라도 있으면 아무 권한도 평가하지 않고 예외가 발생한다")
        void 지원하지_않는_권한이_하나라도_있으면_아무_권한도_평가하지_않고_예외가_발생한다() {
            // given
            List<ResourcePermissionQuery> queries = List.of(
                ResourcePermissionQuery.of(ResourceType.NOTICE, List.of(100L), List.of(PermissionType.READ)),
                ResourcePermissionQuery.of(ResourceType.WORKBOOK_SUBMISSION, List.of(200L), List.of(PermissionType.DELETE))
            );

            // when & then
            assertThatThrownBy(() -> sut.batchHasPermission(MEMBER_ID, queries))
                .isInstanceOf(AuthorizationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthorizationErrorCode.INVALID_INPUT_VALUE);
            verify(checkPermissionUseCase, never()).check(eq(MEMBER_ID), any(ResourcePermission.class));
        }

        @Test
        @DisplayName("resourceIds가 null이면 타입 단위 권한을 배치로 조회한다")
        void resourceIds가_null이면_타입_단위_권한을_배치로_조회한다() {
            // given
            given(checkPermissionUseCase.check(eq(MEMBER_ID),
                eq(ResourcePermission.ofType(ResourceType.NOTICE, PermissionType.CHECK))))
                .willReturn(true);

            List<ResourcePermissionQuery> queries = List.of(
                ResourcePermissionQuery.of(ResourceType.NOTICE, null, List.of(PermissionType.CHECK))
            );

            // when
            List<ResourcePermissionInfo> result = sut.batchHasPermission(MEMBER_ID, queries);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().resourceType()).isEqualTo(ResourceType.NOTICE);
            assertThat(result.getFirst().resourceId()).isNull();
            assertThat(result.getFirst().permissions()).containsEntry(PermissionType.CHECK, true);
        }
    }
}
