package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;

public record GetInterviewSheetPartsInfo(
    List<PartKey> parts
) {
}
