package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.organization.application.port.out.query.LoadChapterPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChapterQueryService implements GetChapterUseCase {

    private final LoadChapterPort loadChapterPort;

    @Override
    public List<ChapterInfo> getAllChapters() {

        return loadChapterPort.findAll().stream().map(ChapterInfo::from).toList();

    }
}
