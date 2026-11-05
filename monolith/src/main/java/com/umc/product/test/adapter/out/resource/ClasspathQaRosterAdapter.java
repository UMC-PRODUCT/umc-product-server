package com.umc.product.test.adapter.out.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.umc.product.test.application.port.out.LoadQaRosterPort;
import com.umc.product.test.domain.roster.QaRoster;
import com.umc.product.test.domain.roster.QaSystemRoles;

@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class ClasspathQaRosterAdapter implements LoadQaRosterPort {
    static final String ROSTER_PATH = "seed/qa/g11-roster.csv";
    static final String SCHOOLS_PATH = "seed/qa/g11-schools.csv";
    static final String STANDALONE_ACCOUNTS_PATH = "seed/qa/standalone-accounts.csv";
    static final String SYSTEM_ROLES_PATH = "seed/qa/system-roles.csv";

    private volatile QaRoster roster;
    private volatile QaSystemRoles systemRoles;

    @Override
    public QaRoster load() {
        QaRoster loaded = roster;
        if (loaded == null) {
            loaded = QaRoster.parse(readLines(ROSTER_PATH), readLines(SCHOOLS_PATH));
            roster = loaded;
        }
        return loaded;
    }

    @Override
    public QaSystemRoles loadSystemRoles() {
        QaSystemRoles loaded = systemRoles;
        if (loaded == null) {
            loaded = QaSystemRoles.parse(readLines(STANDALONE_ACCOUNTS_PATH), readLines(SYSTEM_ROLES_PATH));
            systemRoles = loaded;
        }
        return loaded;
    }

    private static List<String> readLines(String path) {
        try (InputStream input = new ClassPathResource(path).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
        } catch (IOException e) {
            throw new UncheckedIOException("QA 시딩 CSV를 읽지 못했습니다: " + path, e);
        }
    }
}
