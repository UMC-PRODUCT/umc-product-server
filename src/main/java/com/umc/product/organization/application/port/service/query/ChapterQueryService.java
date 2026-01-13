package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ChapterQueryService implements GetChapterUseCase {

    @Override
    public List<ChapterInfo> getAllChapter() {
        return List.of();
    }
}
