package com.umc.product.organization.application.port.in.command.dto;

import java.util.List;

public record UpdateSchoolCommand(
        String schoolName,
        Long chapterId,
        String remark,
        String logoImageId,
        List<SchoolLinkCommand> links
) {

}
