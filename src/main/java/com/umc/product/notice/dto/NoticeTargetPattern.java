package com.umc.product.notice.dto;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum NoticeTargetPattern {

    // =============================
    // 전체 기수 대상
    // =============================

    // 전체 기수 전체 대상
    ALL_GISU_ALL_TARGET(false, false, false, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.isCentralCore(memberId);
        }
    },

    // 전체 기수 특정 학교 대상
    ALL_GISU_SPECIFIC_SCHOOL(false, false, true, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.isSchoolCore(memberId, info.targetSchoolId());
        }
    },

    // (오류) 전체 기수 특정 학교 특정 파트 대상
    ALL_GISU_SPECIFIC_SCHOOL_WITH_PART(false, false, true, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
                "전체 기수를 대상으로 하는 경우, 교내 특정 파트를 한정한 공지는 불가능합니다.");
        }
    },

    // (오류) 전체 기수 특정 지부
    ALL_GISU_WITH_CHAPTER(false, true, false, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
                "기수가 주어지지 않은 상태에서 지부 대상으로 공지를 작성할 수 없습니다.");
        }
    },

    // =============================
    // 특정 기수 대상
    // =============================

    // 특정 기수 전체 대상
    SPECIFIC_GISU_ALL_TARGET(true, false, false, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.isCentralCore(memberId);
        }
    },

    // 특정 기수 특정 파트
    SPECIFIC_GISU_SPECIFIC_PART(true, false, false, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.hasRole(memberId, ChallengerRoleType.CENTRAL_EDUCATION_TEAM_MEMBER);
        }
    },

    // 특정 기수 특정 학교
    SPECIFIC_GISU_SPECIFIC_SCHOOL(true, false, true, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.isSchoolCore(memberId, info.targetSchoolId());
        }
    },

    // 특정 기수 특정 학교 특정 파트
    SPECIFIC_GISU_SPECIFIC_SCHOOL_WITH_PART(true, false, true, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.isSchoolCore(memberId, info.targetSchoolId());
        }
    },

    // 특정 기수 특정 지부
    SPECIFIC_GISU_SPECIFIC_CHAPTER(true, true, false, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.isChapterPresident(memberId, info.targetChapterId());
        }
    },

    // 특정 기수 특정 지부 특정 파트
    SPECIFIC_GISU_SPECIFIC_CHAPTER_WITH_PART(true, true, false, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.isChapterPresident(memberId, info.targetChapterId());
        }
    },

    // (오류) 특정 기수 특정 지부 특정 학교
    INVALID_GISU_CHAPTER_SCHOOL(true, true, true, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
                "기수, 지부, 학교는 동시에 지정할 수 없습니다.");
        }
    };

    private final boolean hasGisu;
    private final boolean hasChapter;
    private final boolean hasSchool;
    private final boolean hasPart;

    NoticeTargetPattern(boolean hasGisu, boolean hasChapter, boolean hasSchool, boolean hasPart) {
        this.hasGisu = hasGisu;
        this.hasChapter = hasChapter;
        this.hasSchool = hasSchool;
        this.hasPart = hasPart;
    }

    public static NoticeTargetPattern from(NoticeTargetInfo info) {
        // id 값들이 null이 아니면 기수 정보가 있는거임
        boolean hasGisu = info.targetGisuId() != null;
        boolean hasChapter = info.targetChapterId() != null;
        boolean hasSchool = info.targetSchoolId() != null;
        // 파트 정보가 null이 아니고, 비어있지 않아야만 파트 정보가 있는거임
        boolean hasPart = info.targetParts() != null && !info.targetParts().isEmpty();

        for (NoticeTargetPattern pattern : values()) {
            if (pattern.matches(hasGisu, hasChapter, hasSchool, hasPart)) {
                log.info("매칭된 NoticeTargetPattern: {}, hasGisu: {}, hasChapter: {}, hasSchool: {}, hasPart: {}",
                    pattern.name(), hasGisu, hasChapter, hasSchool, hasPart);
                return pattern;
            }
        }

        throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
            "지원하지 않는 대상 설정입니다. 관리자에게 문의해주세요. hasGisu: " + hasGisu + ", hasChapter: " + hasChapter + ", hasSchool: "
                + hasSchool + ", hasPart: " + hasPart);
    }

    public abstract boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase);

    private boolean matches(boolean hasGisu, boolean hasChapter, boolean hasSchool, boolean hasPart) {
        return (this.hasGisu == hasGisu)
            && (this.hasChapter == hasChapter)
            && (this.hasSchool == hasSchool)
            && (this.hasPart == hasPart);
    }
}
