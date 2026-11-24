package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.application.port.in.query.dto.ExportChallengerDeactivationQuery;

public interface ExportChallengerDeactivationUseCase {

    byte[] exportCsv(Long memberId, ExportChallengerDeactivationQuery query);
}
