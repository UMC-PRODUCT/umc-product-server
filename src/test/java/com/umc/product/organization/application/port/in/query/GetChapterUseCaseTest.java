package com.umc.product.organization.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.ChapterWithSchoolsInfo;
import com.umc.product.organization.application.port.out.command.ManageChapterPort;
import com.umc.product.organization.application.port.out.command.ManageChapterSchoolPort;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.ChapterSchool;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.UseCaseTestSupport;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GetChapterUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetChapterUseCase getChapterUseCase;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Autowired
    private ManageChapterPort manageChapterPort;

    @Autowired
    private ManageSchoolPort manageSchoolPort;

    @Autowired
    private ManageChapterSchoolPort manageChapterSchoolPort;

    @Test
    void 전체_지부_목록을_조회한다() {
        // given
        Gisu gisu = createGisu(8L);
        manageGisuPort.save(gisu);

        manageChapterPort.save(Chapter.builder().gisu(gisu).name("서울").build());
        manageChapterPort.save(Chapter.builder().gisu(gisu).name("경기").build());
        manageChapterPort.save(Chapter.builder().gisu(gisu).name("인천").build());

        // when
        List<ChapterInfo> result = getChapterUseCase.getAllChapters();

        // then
        assertThat(result).hasSize(3).extracting(ChapterInfo::name)
                .containsExactlyInAnyOrder("서울", "경기", "인천");
    }

    @Test
    void 기수별_지부와_소속_학교_목록을_조회한다() {
        // given
        Gisu gisu9 = manageGisuPort.save(createGisu(9L));
        Gisu gisu10 = manageGisuPort.save(createGisu(10L));

        Chapter scorpioChapter = manageChapterPort.save(Chapter.builder().gisu(gisu9).name("Scorpio").build());
        Chapter leoChapter = manageChapterPort.save(Chapter.builder().gisu(gisu9).name("Leo").build());
        manageChapterPort.save(Chapter.builder().gisu(gisu10).name("Ain").build());

        School school1 = manageSchoolPort.save(School.create("한성대", null));
        School school2 = manageSchoolPort.save(School.create("동국대", null));
        School school3 = manageSchoolPort.save(School.create("중앙대", null));

        manageChapterSchoolPort.save(ChapterSchool.create(scorpioChapter, school1));
        manageChapterSchoolPort.save(ChapterSchool.create(scorpioChapter, school2));
        manageChapterSchoolPort.save(ChapterSchool.create(leoChapter, school3));

        // when
        List<ChapterWithSchoolsInfo> result = getChapterUseCase.getChaptersWithSchoolsByGisuId(gisu9.getId());

        // then
        assertThat(result).hasSize(2);

        ChapterWithSchoolsInfo scorpioResult = result.stream()
                .filter(c -> c.chapterName().equals("Scorpio"))
                .findFirst().orElseThrow();
        assertThat(scorpioResult.schools()).hasSize(2)
                .extracting(ChapterWithSchoolsInfo.SchoolInfo::schoolName)
                .containsExactlyInAnyOrder("한성대", "동국대");

        ChapterWithSchoolsInfo leoResult = result.stream()
                .filter(c -> c.chapterName().equals("Leo"))
                .findFirst().orElseThrow();
        assertThat(leoResult.schools()).hasSize(1)
                .extracting(ChapterWithSchoolsInfo.SchoolInfo::schoolName)
                .containsExactly("중앙대");
    }

    @Test
    void 학교가_없는_지부도_조회된다() {
        // given
        Gisu gisu = manageGisuPort.save(createGisu(9L));
        manageChapterPort.save(Chapter.builder().gisu(gisu).name("Scorpio").build());
        manageChapterPort.save(Chapter.builder().gisu(gisu).name("Leo").build());

        // when
        List<ChapterWithSchoolsInfo> result = getChapterUseCase.getChaptersWithSchoolsByGisuId(gisu.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> c.schools().isEmpty());
    }

    private Gisu createGisu(Long generation) {
        return Gisu.builder()
                .generation(generation)
                .isActive(true)
                .startAt(Instant.parse("2024-03-01T00:00:00Z"))
                .endAt(Instant.parse("2024-08-31T23:59:59Z"))
                .build();
    }
}
