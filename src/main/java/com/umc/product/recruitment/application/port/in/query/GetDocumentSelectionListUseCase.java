package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.DocumentSelectionApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetDocumentSelectionApplicationListQuery;

public interface GetDocumentSelectionListUseCase {
    DocumentSelectionApplicationListInfo get(GetDocumentSelectionApplicationListQuery query);
}
