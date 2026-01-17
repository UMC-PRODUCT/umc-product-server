package com.umc.product.organization.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import com.umc.product.organization.application.port.out.command.ManageChapterPort;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.UseCaseTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class GetSchoolUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetSchoolUseCase getSchoolUseCase;

    @Autowired
    private ManageSchoolPort manageSchoolPort;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Autowired
    private ManageChapterPort manageChapterPort;

    @Test
    void 조건_없이_전체_학교_목록을_조회한다() {
        // given
        manageSchoolPort.save(School.create("동국대학교", "비고1"));
        manageSchoolPort.save(School.create("중앙대학교", "비고2"));
        manageSchoolPort.save(School.create("한성대학교", "비고3"));

        SchoolSearchCondition condition = new SchoolSearchCondition(null, null);
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<SchoolListItemInfo> result = getSchoolUseCase.getSchools(condition, pageRequest);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void keyword로_학교를_검색한다() {
        // given
        manageSchoolPort.save(School.create("동국대학교", "비고1"));
        manageSchoolPort.save(School.create("중앙대학교", "비고2"));
        manageSchoolPort.save(School.create("한성대학교", "비고3"));

        SchoolSearchCondition condition = new SchoolSearchCondition("동국", null);
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<SchoolListItemInfo> result = getSchoolUseCase.getSchools(condition, pageRequest);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).schoolName()).isEqualTo("동국대학교");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void 지부로_학교를_필터링한다() {
        // given
        Gisu gisu = manageGisuPort.save(createGisu(8L));
        Chapter leoChapter = manageChapterPort.save(Chapter.builder().gisu(gisu).name("Leo지부").build());
        Chapter scorpioChapter = manageChapterPort.save(Chapter.builder().gisu(gisu).name("Scorpio지부").build());

        School school1 = School.create("동국대학교", "비고1");
        school1.updateChapterSchool(leoChapter);
        manageSchoolPort.save(school1);

        School school2 = School.create("중앙대학교", "비고2");
        school2.updateChapterSchool(scorpioChapter);
        manageSchoolPort.save(school2);

        manageSchoolPort.save(School.create("한성대학교", "비고3"));

        SchoolSearchCondition condition = new SchoolSearchCondition(null, leoChapter.getId());
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<SchoolListItemInfo> result = getSchoolUseCase.getSchools(condition, pageRequest);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).schoolName()).isEqualTo("동국대학교");
        assertThat(result.getContent().get(0).chapterId()).isEqualTo(leoChapter.getId());
        assertThat(result.getContent().get(0).chapterName()).isEqualTo("Leo지부");
    }

    @Test
    void Pagination_동작을_확인한다() {
        // given
        for (int i = 1; i <= 25; i++) {
            manageSchoolPort.save(School.create("학교" + i, "비고" + i));
        }

        SchoolSearchCondition condition = new SchoolSearchCondition(null, null);
        PageRequest firstPage = PageRequest.of(0, 10);
        PageRequest secondPage = PageRequest.of(1, 10);
        PageRequest thirdPage = PageRequest.of(2, 10);

        // when
        Page<SchoolListItemInfo> firstResult = getSchoolUseCase.getSchools(condition, firstPage);
        Page<SchoolListItemInfo> secondResult = getSchoolUseCase.getSchools(condition, secondPage);
        Page<SchoolListItemInfo> thirdResult = getSchoolUseCase.getSchools(condition, thirdPage);

        // then
        // First page
        assertThat(firstResult.getContent()).hasSize(10);
        assertThat(firstResult.getTotalElements()).isEqualTo(25);
        assertThat(firstResult.getTotalPages()).isEqualTo(3);
        assertThat(firstResult.getNumber()).isEqualTo(0);
        assertThat(firstResult.hasNext()).isTrue();
        assertThat(firstResult.hasPrevious()).isFalse();

        // Second page
        assertThat(secondResult.getContent()).hasSize(10);
        assertThat(secondResult.getNumber()).isEqualTo(1);
        assertThat(secondResult.hasNext()).isTrue();
        assertThat(secondResult.hasPrevious()).isTrue();

        // Third page
        assertThat(thirdResult.getContent()).hasSize(5);
        assertThat(thirdResult.getNumber()).isEqualTo(2);
        assertThat(thirdResult.hasNext()).isFalse();
        assertThat(thirdResult.hasPrevious()).isTrue();
    }

    @Test
    void 검색_결과가_없으면_빈_페이지를_반환한다() {
        // given
        manageSchoolPort.save(School.create("동국대학교", "비고1"));

        SchoolSearchCondition condition = new SchoolSearchCondition("존재하지않는학교", null);
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<SchoolListItemInfo> result = getSchoolUseCase.getSchools(condition, pageRequest);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void 활성_기수에_속한_학교는_isActive가_true다() {
        // given
        Gisu gisu = manageGisuPort.save(createGisu(8L));
        Chapter chapter = manageChapterPort.save(Chapter.builder().gisu(gisu).name("Ain지부").build());

        School activeSchool = School.create("동국대학교", "비고1");
        activeSchool.updateChapterSchool(chapter);
        manageSchoolPort.save(activeSchool);

        manageSchoolPort.save(School.create("중앙대학교", "비고2"));

        SchoolSearchCondition condition = new SchoolSearchCondition(null, null);
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<SchoolListItemInfo> result = getSchoolUseCase.getSchools(condition, pageRequest);

        // then
        SchoolListItemInfo activeSchoolInfo = result.getContent().stream()
                .filter(s -> s.schoolName().equals("동국대학교"))
                .findFirst()
                .orElseThrow();
        SchoolListItemInfo inactiveSchoolInfo = result.getContent().stream()
                .filter(s -> s.schoolName().equals("중앙대학교"))
                .findFirst()
                .orElseThrow();

        assertThat(activeSchoolInfo.isActive()).isTrue();
        assertThat(activeSchoolInfo.chapterId()).isNotNull();
        assertThat(activeSchoolInfo.chapterName()).isEqualTo("Ain지부");

        assertThat(inactiveSchoolInfo.isActive()).isFalse();
        assertThat(inactiveSchoolInfo.chapterId()).isNull();
        assertThat(inactiveSchoolInfo.chapterName()).isNull();
    }

    @Test
    void keyword와_chapterId를_함께_사용하여_학교를_검색한다() {
        // given
        Gisu gisu = manageGisuPort.save(createGisu(8L));
        Chapter scorpioChapter = manageChapterPort.save(Chapter.builder().gisu(gisu).name("Scorpio지부").build());

        School school1 = School.create("동국대학교", "비고1");
        school1.updateChapterSchool(scorpioChapter);
        manageSchoolPort.save(school1);

        School school2 = School.create("동덕여자대학교", "비고2");
        school2.updateChapterSchool(scorpioChapter);
        manageSchoolPort.save(school2);

        manageSchoolPort.save(School.create("중앙대학교", "비고3"));

        SchoolSearchCondition condition = new SchoolSearchCondition("동국", scorpioChapter.getId());
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<SchoolListItemInfo> result = getSchoolUseCase.getSchools(condition, pageRequest);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).schoolName()).isEqualTo("동국대학교");
        assertThat(result.getContent().get(0).chapterId()).isEqualTo(scorpioChapter.getId());
    }

    private Gisu createGisu(Long generation) {
        return Gisu.builder()
                .generation(generation)
                .isActive(true)
                .startAt(LocalDateTime.of(2024, 3, 1, 0, 0))
                .endAt(LocalDateTime.of(2024, 8, 31, 23, 59))
                .build();
    }
}
