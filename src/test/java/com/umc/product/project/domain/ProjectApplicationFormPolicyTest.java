package com.umc.product.project.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.FormSectionType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProjectApplicationFormPolicyTest {

    @Nested
    class createCommon {

        @Test
        void COMMON_타입으로_생성되며_allowedParts는_빈_리스트다() {
            ProjectApplicationFormPolicy policy =
                ProjectApplicationFormPolicy.createCommon(null, 100L);

            assertThat(policy.getType()).isEqualTo(FormSectionType.COMMON);
            assertThat(policy.getFormSectionId()).isEqualTo(100L);
            assertThat(policy.getAllowedParts()).isEmpty();
        }
    }

    @Nested
    class createForParts {

        @Test
        void PART_타입으로_생성된다() {
            ProjectApplicationFormPolicy policy = ProjectApplicationFormPolicy.createForParts(
                null, 100L, Set.of(ChallengerPart.WEB, ChallengerPart.IOS)
            );

            assertThat(policy.getType()).isEqualTo(FormSectionType.PART);
            assertThat(policy.getAllowedParts())
                .containsExactlyInAnyOrder(ChallengerPart.WEB, ChallengerPart.IOS);
        }

        @Test
        void allowedParts가_비어있으면_PROJECT_0013_예외() {
            assertThatThrownBy(() ->
                ProjectApplicationFormPolicy.createForParts(null, 100L, Set.of())
            )
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.APPLICATION_FORM_POLICY_PARTS_EMPTY);
        }

        @Test
        void allowedParts가_null이면_PROJECT_0013_예외() {
            assertThatThrownBy(() ->
                ProjectApplicationFormPolicy.createForParts(null, 100L, null)
            )
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.APPLICATION_FORM_POLICY_PARTS_EMPTY);
        }
    }

    @Nested
    class updatePolicy {

        @Test
        void COMMON으로_변경하면_allowedParts가_비워진다() {
            ProjectApplicationFormPolicy policy = ProjectApplicationFormPolicy.createForParts(
                null, 100L, Set.of(ChallengerPart.WEB)
            );

            policy.updatePolicy(FormSectionType.COMMON, Set.of(ChallengerPart.IOS));

            assertThat(policy.getType()).isEqualTo(FormSectionType.COMMON);
            assertThat(policy.getAllowedParts()).isEmpty();
        }

        @Test
        void PART로_변경하면_allowedParts가_갱신된다() {
            ProjectApplicationFormPolicy policy = ProjectApplicationFormPolicy.createCommon(null, 100L);

            policy.updatePolicy(FormSectionType.PART, Set.of(ChallengerPart.SPRINGBOOT));

            assertThat(policy.getType()).isEqualTo(FormSectionType.PART);
            assertThat(policy.getAllowedParts()).containsExactly(ChallengerPart.SPRINGBOOT);
        }

        @Test
        void PART로_변경하면서_allowedParts가_비면_PROJECT_0013_예외() {
            ProjectApplicationFormPolicy policy = ProjectApplicationFormPolicy.createCommon(null, 100L);

            assertThatThrownBy(() -> policy.updatePolicy(FormSectionType.PART, Set.of()))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.APPLICATION_FORM_POLICY_PARTS_EMPTY);
        }
    }

    @Nested
    class canAccess {

        @Test
        void COMMON은_모든_파트에_대해_true() {
            ProjectApplicationFormPolicy policy = ProjectApplicationFormPolicy.createCommon(null, 100L);

            for (ChallengerPart part : ChallengerPart.values()) {
                assertThat(policy.canAccess(part)).isTrue();
            }
        }

        @Test
        void PART는_allowedParts에_포함된_경우만_true() {
            ProjectApplicationFormPolicy policy = ProjectApplicationFormPolicy.createForParts(
                null, 100L, Set.of(ChallengerPart.WEB, ChallengerPart.IOS)
            );

            assertThat(policy.canAccess(ChallengerPart.WEB)).isTrue();
            assertThat(policy.canAccess(ChallengerPart.IOS)).isTrue();
            assertThat(policy.canAccess(ChallengerPart.DESIGN)).isFalse();
            assertThat(policy.canAccess(ChallengerPart.PLAN)).isFalse();
        }
    }

    @Nested
    class validateSectionAccessPermission {

        @Test
        void 접근_불가_파트면_PROJECT_0007_예외() {
            ProjectApplicationFormPolicy policy = ProjectApplicationFormPolicy.createForParts(
                null, 100L, Set.of(ChallengerPart.WEB)
            );

            assertThatThrownBy(() -> policy.validateSectionAccessPermission(ChallengerPart.DESIGN))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.APPLICATION_FORM_ACCESS_NOT_ALLOWED);
        }

        @Test
        void 접근_가능_파트면_예외_없음() {
            ProjectApplicationFormPolicy policy = ProjectApplicationFormPolicy.createForParts(
                null, 100L, Set.of(ChallengerPart.WEB)
            );

            policy.validateSectionAccessPermission(ChallengerPart.WEB);
        }
    }
}
