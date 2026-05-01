package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.domain.enums.PartQuotaStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * 프로젝트 목록 검색 요청 (PROJECT-001).
 * <p>
 * {@link #toQuery(boolean, Pageable)}에서 요청자 권한(Admin 여부)에 따라
 * {@link SearchProjectQuery#forChallenger} 또는 {@link SearchProjectQuery#forAdmin}을 선택합니다.
 */
public record SearchProjectRequest(
    @NotNull(message = "기수 ID는 필수입니다")
    Long gisuId,

    String keyword,

    Long chapterId,

    List<Long> schoolIds,

    List<ChallengerPart> parts,

    PartQuotaStatus partQuotaStatus,

    List<ProjectStatus> statuses
) {
    public SearchProjectQuery toQuery(boolean isAdmin, Pageable pageable) {
        return isAdmin
            ? SearchProjectQuery.forAdmin(
                gisuId, keyword, chapterId, schoolIds,
                parts, partQuotaStatus, statuses, pageable)
            : SearchProjectQuery.forChallenger(
                gisuId, keyword, chapterId, schoolIds,
                parts, partQuotaStatus, pageable);
    }
}
