package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import java.util.List;

public interface GetChapterUseCase {

    List<ChapterInfo> getAllChapter();
}
