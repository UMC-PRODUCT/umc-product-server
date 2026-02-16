package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.ChapterListResponse;
import com.umc.product.organization.adapter.in.web.dto.response.ChapterListResponse.ChapterItem;
import com.umc.product.organization.adapter.in.web.dto.response.ChapterWithSchoolsResponse;
import com.umc.product.organization.adapter.in.web.swagger.ChapterQueryControllerApi;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.ChapterWithSchoolsInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chapters")
@RequiredArgsConstructor
public class ChapterQueryController implements ChapterQueryControllerApi {

    private final GetChapterUseCase getChapterUseCase;

    @Public
    @GetMapping("{chapterId}")
    public ChapterItem getChapterById(@PathVariable Long chapterId) {
        ChapterInfo chapterInfo = getChapterUseCase.getChapterById(chapterId);
        return ChapterItem.from(chapterInfo);
    }

    @Public
    @Override
    @GetMapping
    public ChapterListResponse getAllChapters() {
        List<ChapterInfo> chapters = getChapterUseCase.getAllChapters();
        return ChapterListResponse.from(chapters);
    }

    @Public
    @Override
    @GetMapping("/with-schools")
    public ChapterWithSchoolsResponse getChaptersWithSchoolsByGisuId(@RequestParam Long gisuId) {
        List<ChapterWithSchoolsInfo> chapters = getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId);
        return ChapterWithSchoolsResponse.from(chapters);
    }
}
