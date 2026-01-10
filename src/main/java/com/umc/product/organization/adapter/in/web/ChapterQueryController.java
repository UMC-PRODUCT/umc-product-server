package com.umc.product.organization.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/chapters")
@RequiredArgsConstructor
public class ChapterQueryController {

//    private final GetChapterUseCase getChapterUseCase;
//
//    @GetMapping
//    public ChapterListResponse getAllChapter() {
//        List<ChapterInfo> chapters = getChapterUseCase.getAllChapter();
//        return ChapterListResponse.from(chapters);
//    }
}
