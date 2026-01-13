package com.umc.product.organization.application.port.service.command;

import com.umc.product.organization.application.port.in.command.ManageChapterUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateChapterCommand;
import org.springframework.stereotype.Service;

@Service
public class ChapterService implements ManageChapterUseCase {

    @Override
    public void create(CreateChapterCommand command) {

    }
}
