package com.umc.product.test.application.port.out;

import com.umc.product.test.domain.roster.QaRoster;
import com.umc.product.test.domain.roster.QaSystemRoles;

public interface LoadQaRosterPort {
    QaRoster load();

    QaSystemRoles loadSystemRoles();
}
