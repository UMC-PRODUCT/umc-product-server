package com.umc.product.test.domain.roster;

import java.util.ArrayList;
import java.util.List;

import com.umc.product.member.domain.MemberSystemRoleType;

/**
 * 명단 밖에서 챌린저 없이 만드는 계정({@code standalone-accounts.csv})과 계정별 시스템 역할({@code system-roles.csv}).
 * 두 표 모두 계정을 이메일 앞부분(별칭)으로 가리킨다.
 */
public record QaSystemRoles(List<StandaloneAccount> standaloneAccounts, List<Grant> grants) {
    private static final String STANDALONE_HEADER = "alias,name";
    private static final String GRANT_HEADER = "alias,role";

    public QaSystemRoles {
        standaloneAccounts = List.copyOf(standaloneAccounts);
        grants = List.copyOf(grants);
    }

    public static QaSystemRoles parse(List<String> standaloneLines, List<String> grantLines) {
        List<StandaloneAccount> standaloneAccounts = rows(standaloneLines, STANDALONE_HEADER).stream()
            .map(columns -> new StandaloneAccount(columns[0], columns[1]))
            .toList();
        List<Grant> grants = rows(grantLines, GRANT_HEADER).stream()
            .map(columns -> new Grant(columns[0], MemberSystemRoleType.valueOf(columns[1])))
            .toList();
        return new QaSystemRoles(standaloneAccounts, grants);
    }

    public record StandaloneAccount(String alias, String name) {
        public StandaloneAccount {
            if (alias.isEmpty() || name.isEmpty()) {
                throw new IllegalArgumentException("명단 밖 계정의 별칭과 이름이 필요합니다: " + alias + "," + name);
            }
        }
    }

    public record Grant(String alias, MemberSystemRoleType role) {
        public Grant {
            if (alias.isEmpty()) {
                throw new IllegalArgumentException("시스템 역할을 받을 계정 별칭이 필요합니다: " + role);
            }
        }
    }

    private static List<String[]> rows(List<String> lines, String header) {
        if (lines.isEmpty() || !lines.getFirst().trim().equals(header)) {
            throw new IllegalArgumentException("CSV 헤더가 " + header + " 이어야 합니다.");
        }
        int width = header.split(",").length;
        List<String[]> rows = new ArrayList<>();
        for (String line : lines.subList(1, lines.size())) {
            if (line.isBlank()) {
                continue;
            }
            String[] columns = line.split(",", -1);
            if (columns.length != width) {
                throw new IllegalArgumentException("열 수가 잘못됐습니다: " + line);
            }
            for (int index = 0; index < columns.length; index++) {
                columns[index] = columns[index].trim();
            }
            rows.add(columns);
        }
        return rows;
    }
}
