package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRoundInfo;
import java.time.Instant;
import java.util.List;

public interface GetProjectMatchingRoundUseCase {

    List<ProjectMatchingRoundInfo> list(Long chapterId, Instant time);
}
