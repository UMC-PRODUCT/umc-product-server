package com.umc.product.organization.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record DeleteSchoolsRequest(
        @NotEmpty List<Long> schoolIds
) {
}
