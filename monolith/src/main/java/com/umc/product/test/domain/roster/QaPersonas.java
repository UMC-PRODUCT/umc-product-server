package com.umc.product.test.domain.roster;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;

/**
 * QA 시나리오가 쓰는 계정을 명단 좌석·역할 보유자·10기 기존 계정으로 옮긴 표. 시나리오 코드는 이 상수만 쓴다.
 */
public final class QaPersonas {

    public static final Persona SUPERADMIN = new Legacy(QaAccountPlan.SUPERADMIN);
    public static final Persona G10_CAU_SCHOOL_PRESIDENT = new Legacy("cau_schoolpresident");
    public static final Persona G10_XENON_CHAPTER_PRESIDENT = new Legacy("xenon_chapterpresident");
    public static final Persona G10_CAU_PLAN = new Legacy("cau_plan");
    public static final Persona G10_CAU_DESIGN = new Legacy("cau_design");
    public static final Persona G10_CAU_WEB = new Legacy("cau_web");
    public static final Persona G10_CAU_ANDROID = new Legacy("cau_android");
    public static final Persona G10_CAU_IOS = new Legacy("cau_ios");
    public static final Persona G10_CAU_NODE = new Legacy("cau_node");
    public static final Persona G10_CAU_SPRINGBOOT = new Legacy("cau_springboot");

    public static final Persona CENTRAL_PRESIDENT = new RoleHolder(ChallengerRoleType.CENTRAL_PRESIDENT, null);
    public static final Persona CENTRAL_EDUCATION = new RoleHolder(
        ChallengerRoleType.CENTRAL_EDUCATION_TEAM_MEMBER, ChallengerPart.WEB_PRODUCT_ENGINEER);

    /** 중앙대 회장 겸 수달 지부장. */
    public static final Persona CAU_SCHOOL_PRESIDENT = seat(1);
    public static final Persona CAU_PLAN_LEADER = seat(3);
    public static final Persona CAU_DESIGN_LEADER = seat(4);
    public static final Persona CAU_WEB_LEADER = seat(5);
    public static final Persona CAU_MOBILE_LEADER = seat(6);
    public static final Persona CAU_PLAN = seat(9);
    public static final Persona CAU_WEB = seat(11);
    public static final Persona CAU_WEB_INFRA = seat(12);
    public static final Persona CAU_MOBILE = seat(26);
    public static final Persona CAU_MOBILE_INFRA = seat(27);
    public static final Persona CAU_DESIGN = seat(32);

    private QaPersonas() {
    }

    public static List<Persona> g10Learners() {
        return List.of(G10_CAU_PLAN, G10_CAU_DESIGN, G10_CAU_WEB, G10_CAU_ANDROID, G10_CAU_IOS, G10_CAU_NODE,
            G10_CAU_SPRINGBOOT);
    }

    private static Persona seat(int seq) {
        return new RosterSeat(RosterKey.of("cau", seq));
    }

    public sealed interface Persona permits RosterSeat, RoleHolder, Legacy {
        String email(QaRoster roster, String emailDomain);
    }

    public record RosterSeat(RosterKey key) implements Persona {
        @Override
        public String email(QaRoster roster, String emailDomain) {
            return roster.entry(key).key().email(emailDomain);
        }
    }

    /**
     * 명단 역할 보유자. 보유자가 여럿이면 명단 순서상 첫 사람이다.
     */
    public record RoleHolder(ChallengerRoleType type, ChallengerPart responsiblePart) implements Persona {
        @Override
        public String email(QaRoster roster, String emailDomain) {
            List<RosterKey> holders = roster.holders(type, responsiblePart);
            if (holders.isEmpty()) {
                throw new IllegalStateException("명단에 역할 보유자가 없습니다: " + type + ":" + responsiblePart);
            }
            return holders.getFirst().email(emailDomain);
        }
    }

    public record Legacy(String alias) implements Persona {
        @Override
        public String email(QaRoster roster, String emailDomain) {
            return QaAccountPlan.legacyEmail(alias, emailDomain);
        }
    }
}
