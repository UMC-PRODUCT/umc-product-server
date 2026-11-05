package com.umc.product.test.domain.startingline;

import static com.umc.product.common.domain.enums.ChallengerPart.DESIGN;
import static com.umc.product.common.domain.enums.ChallengerPart.MOBILE_PRODUCT_ENGINEER;
import static com.umc.product.common.domain.enums.ChallengerPart.WEB_PRODUCT_ENGINEER;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.test.domain.rehearsal.SeedHash;
import com.umc.product.test.domain.roster.RosterKey;

/**
 * 파트마다 합계는 시딩 프로젝트 수가 n일 때 {@code floor(인원 × n / (n + reserveSlots))}다. 나머지 몫은 QA가
 * QA 프로젝트와 직접 등록 프로젝트에 TO를 넣을 자리라, QA가 그 TO를 넣어도 지부 TO 상한(PROJECT-0251)에 걸리지 않는다.
 */
public final class QuotaApportionment {

    private QuotaApportionment() {
    }

    /**
     * @param owners       시딩 프로젝트 PO, 명단 순서
     * @param reserveSlots QA가 나중에 TO를 넣을 프로젝트 수
     * @return PO별 TO, owners 순서
     */
    public static Map<RosterKey, ProjectQuota> apportion(
        Map<ChallengerPart, Integer> headcount, List<RosterKey> owners, int reserveSlots
    ) {
        int projects = owners.size();
        Map<RosterKey, ProjectQuota> result = new LinkedHashMap<>();
        if (projects == 0) {
            return result;
        }
        int design = budget(headcount, DESIGN, projects, reserveSlots);
        int web = budget(headcount, WEB_PRODUCT_ENGINEER, projects, reserveSlots);
        int mobile = budget(headcount, MOBILE_PRODUCT_ENGINEER, projects, reserveSlots);
        int mobileProjects = mobileProjectCount(projects, web, mobile);
        if (projects - mobileProjects > web) {
            throw new IllegalStateException("Web PE TO가 프로젝트 수보다 적습니다: web=" + web + ", mobile=" + mobile
                + ", projects=" + projects);
        }

        List<RosterKey> hashed = owners.stream()
            .sorted(Comparator.comparingDouble(QuotaApportionment::engineerPartOrder)
                .thenComparing(RosterKey::toString))
            .toList();
        List<RosterKey> mobileOwners = hashed.subList(0, mobileProjects);
        List<RosterKey> webOwners = hashed.subList(mobileProjects, projects);
        Map<RosterKey, Integer> designShares = split(design, hashed);
        Map<RosterKey, Integer> webShares = split(web, webOwners);
        Map<RosterKey, Integer> mobileShares = split(mobile, mobileOwners);

        for (RosterKey owner : owners) {
            ProjectQuota quota = mobileShares.containsKey(owner)
                ? new ProjectQuota(designShares.get(owner), MOBILE_PRODUCT_ENGINEER, mobileShares.get(owner))
                : new ProjectQuota(designShares.get(owner), WEB_PRODUCT_ENGINEER, webShares.get(owner));
            result.put(owner, quota);
        }
        return result;
    }

    private static double engineerPartOrder(RosterKey owner) {
        return SeedHash.unit(SeedHash.FIXED_SEED, SeedHash.Purpose.ENGINEER_PART, owner.toString());
    }

    private static int budget(Map<ChallengerPart, Integer> headcount, ChallengerPart part, int projects,
                              int reserveSlots) {
        return headcount.getOrDefault(part, 0) * projects / (projects + reserveSlots);
    }

    private static int mobileProjectCount(int projects, int web, int mobile) {
        if (mobile == 0) {
            return 0;
        }
        if (web == 0) {
            return Math.min(projects, mobile);
        }
        int byRatio = (int) Math.round((double) projects * mobile / (web + mobile));
        int bounded = Math.max(1, Math.min(projects - 1, byRatio));
        return Math.min(bounded, mobile);
    }

    private static Map<RosterKey, Integer> split(int total, List<RosterKey> owners) {
        Map<RosterKey, Integer> shares = new HashMap<>();
        if (owners.isEmpty()) {
            return shares;
        }
        int base = total / owners.size();
        int extra = total % owners.size();
        for (int index = 0; index < owners.size(); index++) {
            shares.put(owners.get(index), base + (index < extra ? 1 : 0));
        }
        return shares;
    }
}
