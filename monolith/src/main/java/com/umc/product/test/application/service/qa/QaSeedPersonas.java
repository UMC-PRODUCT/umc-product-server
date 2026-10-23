package com.umc.product.test.application.service.qa;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerStatus;

public final class QaSeedPersonas {
    public static final String EMAIL_DOMAIN = "university.neordinary.com";
    public static final String CAU = "중앙대학교";

    private QaSeedPersonas() {
    }

    public static List<Persona> all() {
        return List.of(
            new Persona("superadmin", CAU, List.of()),
            staff("cau_schoolpresident", 10, ChallengerRoleType.SCHOOL_PRESIDENT, null),
            staff("xenon_chapterpresident", 10, ChallengerRoleType.CHAPTER_PRESIDENT, null),
            learner("cau_plan", 10, ChallengerPart.PLAN),
            learner("cau_design", 10, ChallengerPart.DESIGN),
            learner("cau_web", 10, ChallengerPart.WEB),
            learner("cau_android", 10, ChallengerPart.ANDROID),
            learner("cau_ios", 10, ChallengerPart.IOS),
            learner("cau_node", 10, ChallengerPart.NODEJS),
            learner("cau_springboot", 10, ChallengerPart.SPRINGBOOT),
            staff("central_g11_president", 11, ChallengerRoleType.CENTRAL_PRESIDENT, null),
            staff("central_g11_vicepresident", 11, ChallengerRoleType.CENTRAL_VICE_PRESIDENT, null),
            staff("central_g11_operating", 11, ChallengerRoleType.CENTRAL_OPERATING_TEAM_MEMBER, null),
            staff("central_g11_education", 11, ChallengerRoleType.CENTRAL_EDUCATION_TEAM_MEMBER,
                ChallengerPart.WEB_PRODUCT_ENGINEER),
            staff("sudal_g11_chapterpresident", 11, ChallengerRoleType.CHAPTER_PRESIDENT, null),
            staff("cau_g11_schoolpresident", 11, ChallengerRoleType.SCHOOL_PRESIDENT, null),
            staff("cau_g11_schoolvicepresident", 11, ChallengerRoleType.SCHOOL_VICE_PRESIDENT, null),
            staff("cau_g11_etcadmin", 11, ChallengerRoleType.SCHOOL_ETC_ADMIN, null),
            staff("cau_g11_plan_leader", 11, ChallengerRoleType.SCHOOL_PART_LEADER, ChallengerPart.PLAN),
            staff("cau_g11_design_leader", 11, ChallengerRoleType.SCHOOL_PART_LEADER, ChallengerPart.DESIGN),
            staff("cau_g11_web_leader", 11, ChallengerRoleType.SCHOOL_PART_LEADER,
                ChallengerPart.WEB_PRODUCT_ENGINEER),
            staff("cau_g11_mobile_leader", 11, ChallengerRoleType.SCHOOL_PART_LEADER,
                ChallengerPart.MOBILE_PRODUCT_ENGINEER),
            learner("cau_g11_plan", 11, ChallengerPart.PLAN),
            learner("cau_g11_design", 11, ChallengerPart.DESIGN),
            learner("cau_g11_web", 11, ChallengerPart.WEB_PRODUCT_ENGINEER),
            learner("cau_g11_mobile", 11, ChallengerPart.MOBILE_PRODUCT_ENGINEER),
            new Persona("cau_g11_web_infra", CAU,
                List.of(enrollment(11, ChallengerPart.WEB_PRODUCT_ENGINEER, true, ChallengerStatus.ACTIVE, null, null))),
            new Persona("cau_g11_mobile_infra", CAU,
                List.of(enrollment(11, ChallengerPart.MOBILE_PRODUCT_ENGINEER, true, ChallengerStatus.ACTIVE, null, null))),
            new Persona("cau_member_only", CAU, List.of()),
            new Persona("sookmyung_g11_web", "숙명여자대학교",
                List.of(enrollment(11, ChallengerPart.WEB_PRODUCT_ENGINEER, false, ChallengerStatus.ACTIVE, null, null))),
            new Persona("gachon_g11_web", "가천대학교",
                List.of(enrollment(11, ChallengerPart.WEB_PRODUCT_ENGINEER, false, ChallengerStatus.ACTIVE, null, null))),
            new Persona("cau_multigisu", CAU, List.of(
                enrollment(10, ChallengerPart.SPRINGBOOT, false, ChallengerStatus.ACTIVE, null, null),
                enrollment(11, ChallengerPart.WEB_PRODUCT_ENGINEER, false, ChallengerStatus.ACTIVE,
                    ChallengerRoleType.SCHOOL_ETC_ADMIN, null))),
            status("cau_g11_graduated", ChallengerStatus.GRADUATED),
            status("cau_g11_withdrawn", ChallengerStatus.WITHDRAWN),
            status("cau_g11_expelled", ChallengerStatus.EXPELLED)
        );
    }

    private static Persona learner(String alias, int generation, ChallengerPart part) {
        return new Persona(alias, CAU,
            List.of(enrollment(generation, part, false, ChallengerStatus.ACTIVE, null, null)));
    }

    private static Persona staff(String alias, int generation, ChallengerRoleType role, ChallengerPart responsiblePart) {
        return new Persona(alias, CAU,
            List.of(enrollment(generation, null, false, ChallengerStatus.ACTIVE, role, responsiblePart)));
    }

    private static Persona status(String alias, ChallengerStatus status) {
        return new Persona(alias, CAU,
            List.of(enrollment(11, ChallengerPart.WEB_PRODUCT_ENGINEER, false, status, null, null)));
    }

    private static Enrollment enrollment(int generation, ChallengerPart part, boolean infra,
        ChallengerStatus status, ChallengerRoleType role, ChallengerPart responsiblePart) {
        return new Enrollment(generation, part, infra, status, role, responsiblePart);
    }

    public record Persona(String alias, String school, List<Enrollment> enrollments) {
        public String email() {
            return alias + "@" + EMAIL_DOMAIN;
        }
    }

    public record Enrollment(int generation, ChallengerPart part, boolean infra, ChallengerStatus status,
        ChallengerRoleType role, ChallengerPart responsiblePart) {
    }
}
