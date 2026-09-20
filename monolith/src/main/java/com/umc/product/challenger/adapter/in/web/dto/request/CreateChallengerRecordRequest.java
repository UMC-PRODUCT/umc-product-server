package com.umc.product.challenger.adapter.in.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateChallengerRecordRequest(
    @NotNull(message = "기수 ID는 필수입니다") Long gisuId,
    Long chapterId,
    @NotNull(message = "학교 ID는 필수입니다") Long schoolId,
    ChallengerPart part,
    boolean infra,
    @NotBlank(message = "회원 이름은 필수입니다") @Size(max = 30, message = "회원 이름은 30자 이하여야 합니다") String memberName,
    ChallengerRoleType challengerRoleType
) {
    @AssertTrue(message = "수강 코드는 파트가 필수이고, infra는 웹/모바일 프로덕트 엔지니어 파트에만 지정할 수 있습니다") @JsonIgnore
    public boolean isLearningSelectionValid() {
        // 운영진 코드는 파트 없이 발급될 수 있다(예: 순수 중앙 운영진).
        if (challengerRoleType == null && part == null) {
            return false;
        }
        return !infra || (part != null && part.canHaveInfra());
    }

    @AssertTrue(message = "수강하지 않는 중앙 운영진 코드만 지부를 생략할 수 있습니다") @JsonIgnore
    public boolean isChapterSelectionValid() {
        return chapterId != null || (challengerRoleType != null
            && challengerRoleType.organizationType() == OrganizationType.CENTRAL
            && part == null);
    }
}
