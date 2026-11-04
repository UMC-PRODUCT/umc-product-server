package com.umc.product.test.application.service.rehearsal;

import java.util.Map;

import com.umc.product.test.domain.roster.QaAccountPlan;
import com.umc.product.test.domain.roster.RehearsalCast;
import com.umc.product.test.domain.roster.RosterKey;

/**
 * 리허설 한 번의 고정 입력. 11기 배역표, 기수 ID, 지부 이름별 ID.
 */
record RehearsalContext(Long gisuId, RehearsalCast cast, Map<String, Long> chapterIds) {
    RehearsalContext {
        chapterIds = Map.copyOf(chapterIds);
    }

    String email(RosterKey key) {
        return key.email();
    }

    String superadminEmail() {
        return QaAccountPlan.legacyEmail(QaAccountPlan.SUPERADMIN);
    }

    String displayName(RosterKey key) {
        return key.displayName(cast.roster().school(key.school()).shortName());
    }
}
