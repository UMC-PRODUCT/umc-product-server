package com.umc.product.organization.application.port.in.query.dto.gisu;

import java.util.List;

public record GisuOrganizationQuery(
    Selector selector,
    List<Long> ids,
    List<Long> generations,
    boolean active,
    boolean includeChapter,
    boolean includeSchool
) {

    public GisuOrganizationQuery {
        ids = ids == null ? List.of() : List.copyOf(ids);
        generations = generations == null ? List.of() : List.copyOf(generations);
    }

    public static GisuOrganizationQuery byIds(List<Long> ids, boolean includeChapter, boolean includeSchool) {
        return new GisuOrganizationQuery(Selector.ID, ids, List.of(), false, includeChapter, includeSchool);
    }

    public static GisuOrganizationQuery byGenerations(
        List<Long> generations,
        boolean includeChapter,
        boolean includeSchool
    ) {
        return new GisuOrganizationQuery(Selector.GENERATION, List.of(), generations, false, includeChapter,
            includeSchool);
    }

    public static GisuOrganizationQuery active(boolean includeChapter, boolean includeSchool) {
        return new GisuOrganizationQuery(Selector.ACTIVE, List.of(), List.of(), true, includeChapter, includeSchool);
    }

    public enum Selector {
        ID,
        GENERATION,
        ACTIVE
    }
}
