package com.umc.product.common.domain.enums;

public enum MemberRoleType {
    NORMAL(0),
    ADMIN(100);

    private final int rank;

    MemberRoleType(int rank) {
        this.rank = rank;
    }

    public boolean isAtLeast(MemberRoleType required) {
        if (required == null) {
            return false;
        }
        return this.rank >= required.rank;
    }

    public boolean isAdmin() {
        return isAtLeast(ADMIN);
    }
}
