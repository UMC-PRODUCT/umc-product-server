package com.umc.product.test.domain.roster;

import static com.umc.product.common.domain.enums.ChallengerPart.DESIGN;
import static com.umc.product.common.domain.enums.ChallengerPart.MOBILE_PRODUCT_ENGINEER;
import static com.umc.product.common.domain.enums.ChallengerPart.PLAN;
import static com.umc.product.common.domain.enums.ChallengerPart.WEB_PRODUCT_ENGINEER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.domain.MemberNamePolicy;
import com.umc.product.member.domain.NicknamePolicy;
import com.umc.product.test.adapter.out.resource.ClasspathQaRosterAdapter;
import com.umc.product.test.domain.roster.QaAccountPlan.Account;
import com.umc.product.test.domain.startingline.StartingLinePlan;

@DisplayName("11기 QA 명단과 리허설 배역")
class RehearsalCastTest {

    private static final List<String> CHAPTERS = List.of("캥거루", "쿼카", "수달", "아홀로틀", "티라노");

    private static final Map<String, String> CHAPTER_BY_SCHOOL = QaCastFixtures.CHAPTER_BY_SCHOOL;

    /** 계획 문서의 학교별 PLAN·디자인·Web PE·Mobile PE 인원표. */
    private static final Map<String, List<Integer>> PLAN_TABLE = planTable();

    private final QaRoster roster = new ClasspathQaRosterAdapter().load();
    private final RehearsalCast cast = RehearsalCast.of(roster, CHAPTER_BY_SCHOOL);

    @Test
    @DisplayName("명단 583행과 superadmin, 10기 9개, 명단 밖 4개를 합쳐 서로 다른 이메일의 계정 597개를 계획한다")
    void accountPlanHas597DistinctAccounts() {
        QaAccountPlan plan = QaAccountPlan.from(roster, new ClasspathQaRosterAdapter().loadSystemRoles());

        assertThat(roster.entries()).hasSize(583);
        assertThat(plan.accounts()).hasSize(597);
        assertThat(new HashSet<>(plan.emails())).hasSize(597);
        assertThat(plan.accounts().getFirst().email()).isEqualTo("gachon_g11_qa01@university.neordinary.com");
        assertThat(plan.accounts().getFirst().displayName()).isEqualTo("가천QA1");
        assertThat(plan.emails()).contains("superadmin@university.neordinary.com", "cau_springboot@university.neordinary.com",
            "cau_g11_qa38@university.neordinary.com");
        assertThat(plan.accounts()).allSatisfy(account -> {
            MemberNamePolicy.validate(account.memberName());
            NicknamePolicy.validate(account.nickname());
        });
        assertThat(plan.accounts()).extracting(Account::nickname).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("학교별 파트 인원이 계획 문서의 인원표와 같다")
    void partCountsPerSchoolMatchPlanTable() {
        Map<String, List<Integer>> actual = new LinkedHashMap<>();
        for (RosterEntry entry : roster.entries()) {
            String school = roster.school(entry.key().school()).name();
            if (!CHAPTER_BY_SCHOOL.containsKey(school)) {
                continue;
            }
            List<ChallengerPart> parts = List.of(PLAN, DESIGN, WEB_PRODUCT_ENGINEER, MOBILE_PRODUCT_ENGINEER);
            actual.put(school, parts.stream()
                .map(part -> (int) roster.entries().stream()
                    .filter(row -> roster.school(row.key().school()).name().equals(school))
                    .filter(row -> row.hasPart(part)).count())
                .toList());
        }

        assertThat(actual).isEqualTo(PLAN_TABLE);
        assertThat(count(entry -> entry.part() == null)).isEqualTo(34 + 15);
    }

    @Test
    @DisplayName("지부별 PLAN은 캥거루 13, 쿼카 19, 수달 13, 아홀로틀 13, 티라노 11명이다")
    void plannersPerChapter() {
        assertThat(cast.chapters()).containsExactlyElementsOf(CHAPTERS);
        assertThat(CHAPTERS.stream().map(chapter -> cast.plannersInRosterOrder(chapter).size()))
            .containsExactly(13, 19, 13, 13, 11);
    }

    @Test
    @DisplayName("출발선은 지부마다 QA 프로젝트 2개와 무프로젝트 PLAN 2명을 두고 프로젝트 59개 중 49개를 시딩한다")
    void startingLineSplitsPlannersPerChapter() {
        List<StartingLinePlan> plans = CHAPTERS.stream().map(cast::startingLine).toList();

        assertThat(plans).allSatisfy(plan -> {
            assertThat(plan.qaOwners()).hasSize(2);
            assertThat(plan.withoutProject()).hasSize(2);
        });
        assertThat(plans.stream().mapToInt(plan -> plan.qaOwners().size() + plan.seededOwners().size()).sum())
            .isEqualTo(59);
        assertThat(plans.stream().mapToInt(plan -> plan.seededOwners().size()).sum()).isEqualTo(49);
        StartingLinePlan kangaroo = plans.getFirst();
        assertThat(kangaroo.qaOwners()).containsExactly(RosterKey.of("gachon", 9), RosterKey.of("gachon", 10));
        assertThat(kangaroo.withoutProject())
            .containsExactly(RosterKey.of("hansung", 13), RosterKey.of("hansung", 26));
    }

    @Test
    @DisplayName("QA 몫 A·B·C는 지부마다 매칭 파트별 3명이고 지부 이동 4명은 들어가지 않는다")
    void qaReservedApplicantsPerChapter() {
        List<RosterKey> transferred = QaTransfers.ALL.stream().map(QaTransfers.QaTransfer::member).toList();

        for (String chapter : CHAPTERS) {
            List<QaReservedApplicant> reserved = cast.qaReserved(chapter);
            assertThat(reserved).hasSize(9);
            assertThat(reserved).extracting(QaReservedApplicant::key).doesNotContainAnyElementsOf(transferred);
            assertThat(reserved).allSatisfy(applicant -> {
                assertThat(roster.entry(applicant.key()).isGeneralChallenger()).isTrue();
                assertThat(cast.matchingChapter(applicant.key())).contains(chapter);
            });
            assertThat(reserved).extracting(QaReservedApplicant::label)
                .containsExactly('A', 'B', 'C', 'A', 'B', 'C', 'A', 'B', 'C');
        }
        assertThat(cast.qaReserved("캥거루")).extracting(QaReservedApplicant::key).startsWith(
            RosterKey.of("gachon", 12), RosterKey.of("swu", 8), RosterKey.of("swu", 9));
        assertThat(cast.qaReserved("아홀로틀")).extracting(QaReservedApplicant::key).startsWith(
            RosterKey.of("catholic", 23), RosterKey.of("duksung", 13), RosterKey.of("duksung", 14));
    }

    @Test
    @DisplayName("지부가 없는 명지·상명·인천가톨릭은 매칭에서 빠지고 이동 4명은 매칭 지부를 옮긴다")
    void matchingChapterAppliesTransfersAndSkipsSchoolsWithoutChapter() {
        for (String school : List.of("mju", "smu", "iccu")) {
            assertThat(cast.matchingChapter(RosterKey.of(school, 1))).isEmpty();
        }
        assertThat(cast.schoolChapter(RosterKey.of("dongyang", 4))).contains("쿼카");
        assertThat(cast.matchingChapter(RosterKey.of("dongyang", 4))).contains("캥거루");
        assertThat(cast.matchingChapter(RosterKey.of("soongsil", 31))).contains("수달");

        Map<ChallengerPart, Integer> total = new LinkedHashMap<>();
        CHAPTERS.forEach(chapter -> cast.matchingHeadcount(chapter)
            .forEach((part, headcount) -> total.merge(part, headcount, Integer::sum)));
        assertThat(total).containsExactlyInAnyOrderEntriesOf(
            Map.of(DESIGN, 72, WEB_PRODUCT_ENGINEER, 284, MOBILE_PRODUCT_ENGINEER, 109));
        assertThat(cast.matchingHeadcount("캥거루").get(DESIGN))
            .isEqualTo(1 + 8 + 0 + 2 + 1);
    }

    @Test
    @DisplayName("중앙 총괄·부총괄과 지부장 5명이 명단 역할에서 나온다")
    void roleHoldersComeFromRoster() {
        assertThat(roster.holders(ChallengerRoleType.CENTRAL_PRESIDENT, null))
            .containsExactly(RosterKey.of("duksung", 3));
        assertThat(roster.holders(ChallengerRoleType.CENTRAL_VICE_PRESIDENT, null))
            .containsExactly(RosterKey.of("gachon", 30));
        assertThat(roster.holders(ChallengerRoleType.CHAPTER_PRESIDENT, null)).containsExactly(
            RosterKey.of("gachon", 2), RosterKey.of("soongsil", 1), RosterKey.of("cau", 1),
            RosterKey.of("hongik", 1), RosterKey.of("dongduk", 1));
        assertThat(roster.holders(ChallengerRoleType.SCHOOL_PART_LEADER, WEB_PRODUCT_ENGINEER))
            .containsExactly(RosterKey.of("cau", 5));
    }

    @Test
    @DisplayName("담당 파트가 없는 파트장, 끊긴 순번, 학교표에 없는 학교는 명단 파싱에서 거부한다")
    void parseRejectsInvalidRows() {
        List<String> schools = List.of("code,name,short_name", "cau,중앙대학교,중앙");

        assertThatThrownBy(() -> QaRoster.parse(
            List.of("school,seq,part,infra,roles", "cau,1,,false,SCHOOL_PART_LEADER"), schools))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QaRoster.parse(
            List.of("school,seq,part,infra,roles", "cau,1,PLAN,false,", "cau,3,PLAN,false,"), schools))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QaRoster.parse(
            List.of("school,seq,part,infra,roles", "snu,1,PLAN,false,"), schools))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QaRoster.parse(
            List.of("school,seq,part,infra,roles", "cau,1,DESIGN,true,"), schools))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private long count(Predicate<RosterEntry> predicate) {
        return roster.entries().stream().filter(predicate).count();
    }

    private static Map<String, List<Integer>> planTable() {
        Map<String, List<Integer>> table = new LinkedHashMap<>();
        table.put("가천대학교", List.of(3, 1, 12, 5));
        table.put("서울여자대학교", List.of(2, 8, 10, 6));
        table.put("한국항공대학교", List.of(1, 0, 17, 3));
        table.put("한성대학교", List.of(7, 2, 15, 5));
        table.put("동양미래대학교", List.of(3, 6, 14, 7));
        table.put("숭실대학교", List.of(5, 10, 14, 16));
        table.put("이화여자대학교", List.of(4, 2, 12, 3));
        table.put("인하대학교", List.of(4, 0, 15, 3));
        table.put("한양대학교 ERICA", List.of(3, 2, 6, 5));
        table.put("성신여자대학교", List.of(4, 5, 13, 10));
        table.put("숙명여자대학교", List.of(2, 0, 11, 1));
        table.put("중앙대학교", List.of(2, 5, 15, 6));
        table.put("한국외국어대학교", List.of(5, 1, 6, 13));
        table.put("가톨릭대학교", List.of(2, 2, 17, 3));
        table.put("덕성여자대학교", List.of(7, 3, 15, 4));
        table.put("세종대학교", List.of(0, 0, 3, 1));
        table.put("안양대학교", List.of(1, 0, 6, 0));
        table.put("홍익대학교 서울캠퍼스", List.of(3, 7, 29, 5));
        table.put("단국대학교", List.of(3, 1, 8, 1));
        table.put("동국대학교", List.of(2, 0, 18, 0));
        table.put("동덕여자대학교", List.of(3, 4, 18, 6));
        table.put("서경대학교", List.of(3, 1, 10, 6));
        table.put("홍익대학교 세종캠퍼스", List.of(0, 12, 0, 0));
        return table;
    }
}
