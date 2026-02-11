package com.umc.product.organization.application.port.in.command.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record CreateSchoolCommand(
    String schoolName,
    String remark,
    String logoImageId,
    List<SchoolLinkCommand> links
) {
}
