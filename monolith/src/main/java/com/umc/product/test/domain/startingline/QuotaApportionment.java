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
 * 지부 매칭 파트 인원을 시딩 프로젝트별 TO로 나눈다.
 * <p>
 * 파트마다 합계는 {@code floor(인원 × n / (n + reserveSlots))}다. 나머지 몫은 QA가 QA 프로젝트와 직접 등록 프로젝트에
 * TO를 넣을 자리라, QA가 그 TO를 넣어도 지부 TO 상한(PROJECT-0251)에 걸리지 않는다.
 */
public final class QuotaApportionment {

    private QuotaApportionment() {
    }

    /**
     * @param headcount    지부 매칭 파트별 인원
     * @param owners       시딩 프로젝트 PO, 명단 순서
     * @param reserveSlots QA가 나중에 TO를 넣을 프로젝트 수
     * @return PO별 TO, owners 순서
     */
    public static Map<RosterKey, ProjectQuota> apportion(
        Map<ChallengerPart, Integer> headcount, List<RosterKey> owners, int reserveSlots
    ) {
        int n = owners.size();
        Map<RosterKey, ProjectQuota> result = new LinkedHashMap<>();
        if (n == 0) {
            return result;
        }
        int design = budget(headcount, DESIGN, n, reserveSlots);
        int web = budget(headcount, WEB_PRODUCT_ENGINEER, n, reserveSlots);
        int mobile = budget(headcount, MOBILE_PRODUCT_ENGINEER, n, reserveSlots);
        int mobileProjects = mobileProjectCount(n, web, mobile);
        if (n - mobileProjects > web) {
            throw new IllegalStateException("Web PE TO가 프로젝트 수보다 적습니다: web=" + web + ", mobile=" + mobile
                + ", projects=" + n);
        }

        List<RosterKey> hashed = owners.stream()
            .sorted(Comparator.comparingDouble(
                (RosterKey owner) -> SeedHash.unit(SeedHash.FIXED_SEED, SeedHash.Purpose.ENGINEER_PART, owner.toString()))
                .thenComparing(RosterKey::toString))
            .toList();
        List<RosterKey> mobileOwners = hashed.subList(0, mobileProjects);
        List<RosterKey> webOwners = hashed.subList(mobileProjects, n);
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

    private static int budget(Map<ChallengerPart, Integer> headcount, ChallengerPart part, int n, int reserveSlots) {
        return headcount.getOrDefault(part, 0) * n / (n + reserveSlots);
    }

    /**
     * 개발 파트 인원 비율로 Mobile PE 프로젝트 수를 정한다. 두 파트 모두 TO가 있으면 각각 1개 이상이다.
     */
    private static int mobileProjectCount(int n, int web, int mobile) {
        if (mobile == 0) {
            return 0;
        }
        if (web == 0) {
            return Math.min(n, mobile);
        }
        int byRatio = (int) Math.round((double) n * mobile / (web + mobile));
        int bounded = Math.max(1, Math.min(n - 1, byRatio));
        return Math.min(bounded, mobile);
    }

    /**
     * total을 owners에 고르게 나눈다. 나머지는 owners 앞쪽부터 1씩 더한다.
     */
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
