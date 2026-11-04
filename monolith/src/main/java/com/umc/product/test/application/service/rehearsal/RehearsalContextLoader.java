package com.umc.product.test.application.service.rehearsal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.test.application.port.out.LoadQaRosterPort;
import com.umc.product.test.domain.roster.QaAccountPlan;
import com.umc.product.test.domain.roster.RehearsalCast;

import lombok.RequiredArgsConstructor;

@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class RehearsalContextLoader {

    private final LoadQaRosterPort loadQaRosterPort;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChapterUseCase getChapterUseCase;

    RehearsalContext load() {
        return find().orElseThrow(() -> new IllegalStateException(QaAccountPlan.CURRENT_GENERATION + "기가 없습니다."));
    }

    Optional<RehearsalContext> find() {
        return getGisuUseCase.getList().stream()
            .filter(gisu -> gisu.generation().intValue() == QaAccountPlan.CURRENT_GENERATION)
            .map(GisuInfo::gisuId)
            .findFirst()
            .map(this::build);
    }

    private RehearsalContext build(Long gisuId) {
        Map<String, String> chapterBySchool = new HashMap<>();
        Map<String, Long> chapterIds = new HashMap<>();
        for (ChapterWithSchoolsInfo chapter : getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId)) {
            chapterIds.put(chapter.chapterName(), chapter.chapterId());
            chapter.schools().forEach(school -> chapterBySchool.put(school.schoolName(), chapter.chapterName()));
        }
        RehearsalCast cast = RehearsalCast.of(loadQaRosterPort.load(), chapterBySchool);
        return new RehearsalContext(gisuId, cast, chapterIds);
    }
}
