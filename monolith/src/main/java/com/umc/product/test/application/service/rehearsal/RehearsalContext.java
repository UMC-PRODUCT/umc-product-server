package com.umc.product.test.application.service.rehearsal;

import java.util.Map;

import com.umc.product.test.domain.roster.QaAccountPlan;
import com.umc.product.test.domain.roster.RehearsalCast;
import com.umc.product.test.domain.roster.RosterKey;

/**
 * 리허설 한 번의 고정 입력. 11기 배역표, 기수 ID, 지부 이름별 ID, QA 계정 이메일 도메인.
 */
record RehearsalContext(Long gisuId, RehearsalCast cast, Map<String, Long> chapterIds, String emailDomain) {
    RehearsalContext {
        chapterIds = Map.copyOf(chapterIds);
    }

    String email(RosterKey key) {
        return key.email(emailDomain);
    }

    String superadminEmail() {
        return QaAccountPlan.legacyEmail(QaAccountPlan.SUPERADMIN, emailDomain);
    }

    String displayName(RosterKey key) {
        return key.displayName(cast.roster().school(key.school()).shortName());
    }
}
