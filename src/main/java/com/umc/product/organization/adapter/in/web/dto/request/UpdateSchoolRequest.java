package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UpdateSchoolCommand;

public record UpdateSchoolRequest(
        String schoolName,
        String chapterId,
        String remark
) {
    public UpdateSchoolCommand toCommand() {
        return new UpdateSchoolCommand(schoolName, chapterId, remark);
    }
}
