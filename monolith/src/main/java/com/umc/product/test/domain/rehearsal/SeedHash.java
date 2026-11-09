package com.umc.product.test.domain.rehearsal;

import java.nio.charset.StandardCharsets;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;

/**
 * 상태 없는 결정 난수. 모든 무작위 판단을 (seed, 목적, 안정 키)의 해시로 정한다.
 * 호출 순서나 횟수에 기대지 않으므로, 계획을 다시 계산하거나 일부가 이미 처리됐어도 같은 사람에게 같은 답이 나온다.
 */
public final class SeedHash {
    /** 프로젝트 인기도·지터·개발 파트처럼 리허설을 다시 해도 같아야 하는 값에 쓰는 seed. */
    public static final long FIXED_SEED = 0L;

    private static final long FNV_OFFSET = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x100000001b3L;

    private SeedHash() {
    }

    public enum Purpose {
        ENGINEER_PART, APPLY_SELECT, PROJECT_PICK, POPULARITY, JITTER, DECISION_ORDER
    }

    /**
     * [0, 1) 사이의 값.
     */
    public static double unit(long seed, Purpose purpose, String... keys) {
        long hash = fnv1a(FNV_OFFSET, purpose.name());
        for (String key : keys) {
            hash = fnv1a(hash ^ 0x1f, key);
        }
        return (splitMix64(seed ^ hash) >>> 11) * 0x1.0p-53;
    }

    /**
     * 차수 id 대신 (지부 이름, 유형, 차수 번호)로 정한다. 프로젝트 초기화로 차수 id가 바뀌어도 같은 그림이 나온다.
     */
    public static long defaultSeed(String chapterName, MatchingType type, MatchingPhase phase) {
        return splitMix64(fnv1a(FNV_OFFSET, chapterName + "|" + type.name() + "|" + phase.name()));
    }

    private static long fnv1a(long hash, String value) {
        long result = hash;
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            result ^= b & 0xff;
            result *= FNV_PRIME;
        }
        return result;
    }

    private static long splitMix64(long value) {
        long mixed = value + 0x9e3779b97f4a7c15L;
        mixed = (mixed ^ (mixed >>> 30)) * 0xbf58476d1ce4e5b9L;
        mixed = (mixed ^ (mixed >>> 27)) * 0x94d049bb133111ebL;
        return mixed ^ (mixed >>> 31);
    }
}
