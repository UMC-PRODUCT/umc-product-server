package com.umc.product.test.application.service.rehearsal;

import java.util.Map;

import com.umc.product.test.domain.roster.QaAccountPlan;
import com.umc.product.test.domain.roster.RehearsalCast;
import com.umc.product.test.domain.roster.RosterKey;

record RehearsalContext(Long gisuId, RehearsalCast cast, Map<String, Long> chapterIds) {
    RehearsalContext {
        chapterIds = Map.copyOf(chapterIds);
    }

    String superadminEmail() {
        return QaAccountPlan.legacyEmail(QaAccountPlan.SUPERADMIN);
    }

    String displayName(RosterKey key) {
        return key.displayName(cast.roster().school(key.school()).shortName());
    }
}
