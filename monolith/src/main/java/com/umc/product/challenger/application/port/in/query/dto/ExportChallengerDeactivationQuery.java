package com.umc.product.challenger.application.port.in.query.dto;

import java.time.LocalDate;
import java.util.Set;

import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;

public record ExportChallengerDeactivationQuery(
    SearchChallengerDeactivationQuery scope,
    LocalDate startDate,
    LocalDate endDate,
    Set<Long> deactivationIds,
    Set<Type> types,
    Set<Column> columns
) {
    public static final int MAX_SELECTED_IDS = 1_000;

    public ExportChallengerDeactivationQuery {
        deactivationIds = deactivationIds == null ? Set.of() : Set.copyOf(deactivationIds);
        types = types == null ? Set.of() : Set.copyOf(types);
        columns = columns == null
            ? Set.of(Column.ORGANIZATION, Column.REASON, Column.REGISTERED_DATE, Column.REGISTRAR_ROLE)
            : Set.copyOf(columns);
    }

    public enum Type {
        VOLUNTARY(null),
        POINT_THRESHOLD(ChallengerDeactivationReason.INVOLUNTARY_POINT_THRESHOLD),
        EVENT_NO_SHOW(ChallengerDeactivationReason.INVOLUNTARY_EVENT_NO_SHOW),
        LOW_PROJECT_PARTICIPATION(ChallengerDeactivationReason.INVOLUNTARY_LOW_PROJECT_PARTICIPATION),
        INVOLUNTARY_OTHER(ChallengerDeactivationReason.INVOLUNTARY_OTHER);

        private final ChallengerDeactivationReason reason;

        Type(ChallengerDeactivationReason reason) {
            this.reason = reason;
        }

        public ChallengerDeactivationReason reason() {
            return reason;
        }
    }

    public enum Column {
        ORGANIZATION,
        REASON,
        REGISTERED_DATE,
        REGISTRAR_ROLE,
        MEMO,
        NAME
    }
}
