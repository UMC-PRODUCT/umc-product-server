package com.umc.product.organization.application.port.in.command.dto;

import java.util.List;

public record CreateSchoolCommand(
        String schoolName,
        String remark,
        String logoImageId,
        List<SchoolLinkCommand> links
) {
}
