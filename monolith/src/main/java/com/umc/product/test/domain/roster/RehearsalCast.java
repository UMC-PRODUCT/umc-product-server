package com.umc.product.test.domain.roster;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.test.domain.roster.QaTransfers.QaTransfer;
import com.umc.product.test.domain.startingline.StartingLinePlan;

/**
 * 명단, 고정 이동 4건, 11기 지부 배치에서 계산한 리허설 배역. 계정 시딩, 출발선, 봇의 QA 몫, 요약은 이 객체만 본다.
 */
public final class RehearsalCast {
    public static final List<ChallengerPart> MATCHING_PARTS = List.of(
        ChallengerPart.DESIGN, ChallengerPart.WEB_PRODUCT_ENGINEER, ChallengerPart.MOBILE_PRODUCT_ENGINEER);
    private static final int PLANNERS_WITHOUT_PROJECT = 2;
    private static final int QA_PROJECTS = 2;
    private static final char[] QA_RESERVED_LABELS = {'A', 'B', 'C'};

    private final QaRoster roster;
    private final Map<SchoolCode, String> chapterBySchool;
    private final Map<RosterKey, String> transfers;
    private final List<String> chapters;

    private RehearsalCast(QaRoster roster, Map<SchoolCode, String> chapterBySchool, Map<RosterKey, String> transfers) {
        this.roster = roster;
        this.chapterBySchool = Map.copyOf(chapterBySchool);
        this.transfers = Map.copyOf(transfers);
        Set<String> ordered = new LinkedHashSet<>();
        roster.entries().forEach(entry -> schoolChapter(entry.key()).ifPresent(ordered::add));
        this.chapters = List.copyOf(ordered);
    }

    /**
     * @param chapterBySchoolName 11기 학교 이름 → 지부 이름. 지부가 없는 학교는 빠진다.
     */
    public static RehearsalCast of(QaRoster roster, Map<String, String> chapterBySchoolName) {
        Map<SchoolCode, String> chapterBySchool = new HashMap<>();
        for (RosterEntry entry : roster.entries()) {
            SchoolCode code = entry.key().school();
            String chapter = chapterBySchoolName.get(roster.school(code).name());
            if (chapter != null) {
                chapterBySchool.put(code, chapter);
            }
        }
        Map<RosterKey, String> transfers = new HashMap<>();
        for (QaTransfer transfer : QaTransfers.ALL) {
            RosterEntry entry = roster.entry(transfer.member());
            String from = chapterBySchool.get(transfer.member().school());
            if (entry.part() == null || entry.part() == ChallengerPart.PLAN) {
                throw new IllegalArgumentException("기획·파트 없음은 매칭 지부를 옮길 수 없습니다: " + transfer.member());
            }
            if (from == null || from.equals(transfer.toChapter())
                || !chapterBySchool.containsValue(transfer.toChapter())) {
                throw new IllegalArgumentException("매칭 지부 이동 대상 지부가 잘못됐습니다: " + transfer);
            }
            transfers.put(transfer.member(), transfer.toChapter());
        }
        return new RehearsalCast(roster, chapterBySchool, transfers);
    }

    public QaRoster roster() {
        return roster;
    }

    public List<String> chapters() {
        return chapters;
    }

    public Optional<String> schoolChapter(RosterKey key) {
        return Optional.ofNullable(chapterBySchool.get(key.school()));
    }

    /**
     * 고정 이동 대상이면 이동한 지부, 아니면 학교 지부. 지부가 없는 학교(명지·상명·인천가톨릭)는 매칭에서 빠진다.
     */
    public Optional<String> matchingChapter(RosterKey key) {
        String transferred = transfers.get(key);
        return transferred != null ? Optional.of(transferred) : schoolChapter(key);
    }

    public boolean isTransferred(RosterKey key) {
        return transfers.containsKey(key);
    }

    public List<RosterKey> plannersInRosterOrder(String chapter) {
        return roster.entries().stream()
            .filter(entry -> entry.hasPart(ChallengerPart.PLAN))
            .filter(entry -> matchingChapter(entry.key()).filter(chapter::equals).isPresent())
            .map(RosterEntry::key)
            .toList();
    }

    public StartingLinePlan startingLine(String chapter) {
        List<RosterKey> planners = plannersInRosterOrder(chapter);
        int owners = planners.size() - PLANNERS_WITHOUT_PROJECT;
        if (owners < QA_PROJECTS) {
            throw new IllegalStateException(chapter + " 지부의 PLAN이 출발선에 모자랍니다: " + planners.size());
        }
        return new StartingLinePlan(chapter,
            planners.subList(owners, planners.size()),
            planners.subList(0, QA_PROJECTS),
            planners.subList(QA_PROJECTS, owners));
    }

    /**
     * 매칭 파트마다 지부 안 학교 가나다순, 학교 안 순번으로 첫 일반 챌린저 3명. 고정 이동 대상은 빼고 고른다.
     */
    public List<QaReservedApplicant> qaReserved(String chapter) {
        Collator collator = Collator.getInstance(Locale.KOREAN);
        Comparator<RosterEntry> order = Comparator
            .comparing((RosterEntry entry) -> roster.school(entry.key().school()).name(), collator)
            .thenComparingInt(entry -> entry.key().seq());
        List<QaReservedApplicant> reserved = new ArrayList<>();
        for (ChallengerPart part : MATCHING_PARTS) {
            List<RosterEntry> picked = roster.entries().stream()
                .filter(RosterEntry::isGeneralChallenger)
                .filter(entry -> entry.hasPart(part))
                .filter(entry -> !isTransferred(entry.key()))
                .filter(entry -> matchingChapter(entry.key()).filter(chapter::equals).isPresent())
                .sorted(order)
                .limit(QA_RESERVED_LABELS.length)
                .toList();
            if (picked.size() < QA_RESERVED_LABELS.length) {
                throw new IllegalStateException(chapter + " 지부 " + part + " 일반 챌린저가 QA 몫보다 적습니다.");
            }
            for (int index = 0; index < picked.size(); index++) {
                reserved.add(new QaReservedApplicant(picked.get(index).key(), part, QA_RESERVED_LABELS[index]));
            }
        }
        return List.copyOf(reserved);
    }

    public Map<ChallengerPart, Integer> matchingHeadcount(String chapter) {
        Map<ChallengerPart, Integer> headcount = new EnumMap<>(ChallengerPart.class);
        MATCHING_PARTS.forEach(part -> headcount.put(part, 0));
        for (RosterEntry entry : roster.entries()) {
            if (entry.part() != null && MATCHING_PARTS.contains(entry.part())
                && matchingChapter(entry.key()).filter(chapter::equals).isPresent()) {
                headcount.merge(entry.part(), 1, Integer::sum);
            }
        }
        return Collections.unmodifiableMap(headcount);
    }
}
