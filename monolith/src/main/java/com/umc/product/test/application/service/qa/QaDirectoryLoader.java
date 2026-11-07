package com.umc.product.test.application.service.qa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolNameInfo;
import com.umc.product.test.application.port.out.LoadQaRosterPort;
import com.umc.product.test.application.service.QaGisuIdLoader;
import com.umc.product.test.domain.roster.QaAccountPlan;
import com.umc.product.test.domain.roster.QaRoster;

import lombok.RequiredArgsConstructor;

@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class QaDirectoryLoader {
    private static final List<Integer> GENERATIONS = List.of(QaAccountPlan.LEGACY_GENERATION,
        QaAccountPlan.CURRENT_GENERATION);

    private final LoadQaRosterPort loadQaRosterPort;
    private final GetMemberUseCase getMemberUseCase;
    private final QaGisuIdLoader qaGisuIdLoader;
    private final GetSchoolUseCase getSchoolUseCase;
    private final GetChapterUseCase getChapterUseCase;

    QaDirectory load() {
        QaRoster roster = loadQaRosterPort.load();
        QaAccountPlan accounts = QaAccountPlan.from(roster, loadQaRosterPort.loadSystemRoles());
        Map<String, Long> memberIds = getMemberUseCase.findIdsByEmails(new HashSet<>(accounts.emails()));

        Map<Integer, Long> gisuIds = qaGisuIdLoader.load(GENERATIONS);

        Set<String> duplicatedNames = new HashSet<>();
        Map<String, Long> schoolIds = new HashMap<>();
        for (SchoolNameInfo school : getSchoolUseCase.getAllSchoolNames()) {
            if (schoolIds.put(school.schoolName(), school.schoolId()) != null) {
                duplicatedNames.add(school.schoolName());
            }
        }
        duplicatedNames.forEach(schoolIds::remove);

        Map<String, Long> chapterIds = new HashMap<>();
        Map<String, Long> chapterIdsBySchool = new HashMap<>();
        for (Map.Entry<Integer, Long> gisu : gisuIds.entrySet()) {
            for (ChapterWithSchoolsInfo chapter : getChapterUseCase.getChaptersWithSchoolsByGisuId(gisu.getValue())) {
                chapterIds.put(gisu.getKey() + ":" + chapter.chapterName(), chapter.chapterId());
                chapter.schools().forEach(school ->
                    chapterIdsBySchool.put(gisu.getKey() + ":" + school.schoolId(), chapter.chapterId()));
            }
        }
        return new QaDirectory(roster, accounts, memberIds, gisuIds, schoolIds, chapterIds, chapterIdsBySchool);
    }
}
