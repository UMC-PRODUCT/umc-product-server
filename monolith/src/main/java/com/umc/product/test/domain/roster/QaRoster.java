package com.umc.product.test.domain.roster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;

/**
 * 커밋된 11기 QA 명단({@code g11-roster.csv})과 학교표({@code g11-schools.csv}). 순서는 CSV 순서를 그대로 지킨다.
 */
public final class QaRoster {
    private static final String ROSTER_HEADER = "school,seq,part,infra,roles";
    private static final String SCHOOL_HEADER = "code,name,short_name";

    private final List<RosterEntry> entries;
    private final Map<SchoolCode, School> schools;

    private QaRoster(List<RosterEntry> entries, Map<SchoolCode, School> schools) {
        this.entries = List.copyOf(entries);
        this.schools = Map.copyOf(schools);
    }

    public record School(SchoolCode code, String name, String shortName) {
    }

    public static QaRoster parse(List<String> rosterLines, List<String> schoolLines) {
        Map<SchoolCode, School> schools = parseSchools(schoolLines);
        requireHeader(rosterLines, ROSTER_HEADER);
        List<RosterEntry> entries = new ArrayList<>();
        Map<SchoolCode, Integer> lastSeq = new HashMap<>();
        for (int index = 1; index < rosterLines.size(); index++) {
            String line = rosterLines.get(index);
            if (line.isBlank()) {
                continue;
            }
            RosterEntry entry = parseEntry(line, index + 1);
            if (!schools.containsKey(entry.key().school())) {
                throw new IllegalArgumentException("학교표에 없는 학교입니다: " + line);
            }
            int expected = lastSeq.getOrDefault(entry.key().school(), 0) + 1;
            if (entry.key().seq() != expected) {
                throw new IllegalArgumentException("학교 안 순번이 1부터 연속이어야 합니다: " + line);
            }
            lastSeq.put(entry.key().school(), entry.key().seq());
            entries.add(entry);
        }
        return new QaRoster(entries, schools);
    }

    public List<RosterEntry> entries() {
        return entries;
    }

    public School school(SchoolCode code) {
        School school = schools.get(code);
        if (school == null) {
            throw new IllegalArgumentException("학교표에 없는 학교입니다: " + code.value());
        }
        return school;
    }

    public RosterEntry entry(RosterKey key) {
        return entries.stream().filter(entry -> entry.key().equals(key)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("명단에 없는 계정입니다: " + key));
    }

    /**
     * 역할 보유자를 명단 순서로 돌려준다. 담당 파트가 null이면 담당 파트 없는 역할만 본다.
     */
    public List<RosterKey> holders(ChallengerRoleType type, ChallengerPart responsiblePart) {
        RosterRole wanted = RosterRole.of(type, responsiblePart);
        return entries.stream().filter(entry -> entry.roles().contains(wanted)).map(RosterEntry::key).toList();
    }

    private static Map<SchoolCode, School> parseSchools(List<String> lines) {
        requireHeader(lines, SCHOOL_HEADER);
        Map<SchoolCode, School> schools = new LinkedHashMap<>();
        for (String line : lines.subList(1, lines.size())) {
            if (line.isBlank()) {
                continue;
            }
            String[] columns = line.split(",", -1);
            if (columns.length != 3) {
                throw new IllegalArgumentException("학교표 열 수가 잘못됐습니다: " + line);
            }
            School school = new School(SchoolCode.of(columns[0].trim()), columns[1].trim(), columns[2].trim());
            if (schools.put(school.code(), school) != null) {
                throw new IllegalArgumentException("학교 코드가 중복됐습니다: " + line);
            }
        }
        return schools;
    }

    private static RosterEntry parseEntry(String line, int lineNumber) {
        String[] columns = line.split(",", -1);
        if (columns.length != 5) {
            throw new IllegalArgumentException("명단 " + lineNumber + "행의 열 수가 잘못됐습니다: " + line);
        }
        RosterKey key = RosterKey.of(columns[0].trim(), Integer.parseInt(columns[1].trim()));
        ChallengerPart part = columns[2].isBlank() ? null : ChallengerPart.valueOf(columns[2].trim());
        boolean infra = parseBoolean(columns[3].trim(), line);
        List<RosterRole> roles = columns[4].isBlank()
            ? List.of()
            : Arrays.stream(columns[4].split("\\|")).map(RosterRole::parse).toList();
        return new RosterEntry(key, part, infra, roles);
    }

    private static boolean parseBoolean(String value, String line) {
        return switch (value) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException("infra 값은 true 또는 false여야 합니다: " + line);
        };
    }

    private static void requireHeader(List<String> lines, String header) {
        if (lines.isEmpty() || !lines.getFirst().trim().equals(header)) {
            throw new IllegalArgumentException("CSV 헤더가 " + header + " 이어야 합니다.");
        }
    }
}
