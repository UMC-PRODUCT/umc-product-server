package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.workbook.EditOriginalWorkbookCommand;

public record EditOriginalWorkbookRequest(
    String title,
    String description,
    String url,
    String content
) {

    public EditOriginalWorkbookCommand toCommand(Long originalWorkbookId) {
        return EditOriginalWorkbookCommand.builder()
            .originalWorkbookId(originalWorkbookId)
            .title(title)
            .description(description)
            .url(url)
            .content(content)
            .build();
    }
}
