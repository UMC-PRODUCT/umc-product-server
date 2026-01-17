package com.umc.product.organization.adapter.in.web;

import com.umc.product.organization.adapter.in.web.dto.response.ChapterListResponse;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/chapters")
@RequiredArgsConstructor
public class ChapterQueryController implements ChapterQueryControllerApi {

    private final GetChapterUseCase getChapterUseCase;

    @Override
    @GetMapping
    public ChapterListResponse getAllChapters() {
        List<ChapterInfo> chapters = getChapterUseCase.getAllChapters();
        return ChapterListResponse.from(chapters);
    }
}
