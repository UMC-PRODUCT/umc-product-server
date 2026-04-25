package com.umc.product.notice.dto;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.notice.domain.enums.NoticeTargetRole;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum NoticeTargetPattern {

    // =============================
    // 챌린저 공지 - 전체 기수 대상
    // =============================

    ALL_GISU_ALL_TARGET(false, false, false, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.isCentralCore(memberId);
        }
    },

    ALL_GISU_SPECIFIC_SCHOOL(false, false, true, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.isSchoolCore(memberId, info.targetSchoolId());
        }
    },

    // (오류) 전체 기수 특정 학교 특정 파트
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
    // 챌린저 공지 - 특정 기수 대상
    // =============================

    SPECIFIC_GISU_ALL_TARGET(true, false, false, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.isCentralCore(memberId);
        }
    },

    SPECIFIC_GISU_SPECIFIC_PART(true, false, false, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.hasRoleTypeInGisu(memberId, info.targetGisuId(),
                ChallengerRoleType.CENTRAL_EDUCATION_TEAM_MEMBER);
        }
    },

    SPECIFIC_GISU_SPECIFIC_SCHOOL(true, false, true, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.isSchoolCoreInGisu(memberId, info.targetGisuId(), info.targetSchoolId());
        }
    },

    SPECIFIC_GISU_SPECIFIC_SCHOOL_WITH_PART(true, false, true, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.isSchoolAdminInGisu(memberId, info.targetGisuId(), info.targetSchoolId());
        }
    },

    SPECIFIC_GISU_SPECIFIC_CHAPTER(true, true, false, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.isChapterPresidentInGisu(memberId, info.targetGisuId(), info.targetChapterId());
        }
    },

    SPECIFIC_GISU_SPECIFIC_CHAPTER_WITH_PART(true, true, false, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return useCase.isChapterPresidentInGisu(memberId, info.targetGisuId(), info.targetChapterId());
        }
    },

    // (오류) 특정 기수 특정 지부 특정 학교
    INVALID_GISU_CHAPTER_SCHOOL(true, true, true, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
                "기수, 지부, 학교는 동시에 지정할 수 없습니다.");
        }
    },

    // =============================
    // 운영진 공지 - 특정 기수 대상
    // =============================

    // 특정 기수 전체 운영진
    STAFF_SPECIFIC_GISU(true, false, false, false, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            return hasStaffWritePermission(info, memberId, useCase);
        }
    },

    // 특정 기수 특정 학교 운영진 (교내파트장/회장단 대상)
    STAFF_SPECIFIC_GISU_SPECIFIC_SCHOOL(true, false, true, false, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            validateNoSchoolForCentralRoles(info);
            return hasStaffWritePermission(info, memberId, useCase);
        }
    },

    // 특정 기수 특정 파트 운영진 (교내파트장 대상)
    STAFF_SPECIFIC_GISU_SPECIFIC_PART(true, false, false, true, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            validatePartApplicableRoles(info);
            return hasStaffWritePermission(info, memberId, useCase);
        }
    },

    // 특정 기수 특정 학교 특정 파트 운영진 (교내파트장 대상)
    STAFF_SPECIFIC_GISU_SPECIFIC_SCHOOL_WITH_PART(true, false, true, true, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase) {
            validateNoSchoolForCentralRoles(info);
            validatePartApplicableRoles(info);
            return hasStaffWritePermission(info, memberId, useCase);
        }
    };

    private final boolean hasGisu;
    private final boolean hasChapter;
    private final boolean hasSchool;
    private final boolean hasPart;
    private final boolean hasStaffRoles;

    NoticeTargetPattern(boolean hasGisu, boolean hasChapter, boolean hasSchool, boolean hasPart) {
        this(hasGisu, hasChapter, hasSchool, hasPart, false);
    }

    NoticeTargetPattern(boolean hasGisu, boolean hasChapter, boolean hasSchool, boolean hasPart,
                        boolean hasStaffRoles) {
        this.hasGisu = hasGisu;
        this.hasChapter = hasChapter;
        this.hasSchool = hasSchool;
        this.hasPart = hasPart;
        this.hasStaffRoles = hasStaffRoles;
    }

    public static NoticeTargetPattern from(NoticeTargetInfo info) {
        boolean hasGisu = info.targetGisuId() != null;
        boolean hasChapter = info.targetChapterId() != null;
        boolean hasSchool = info.targetSchoolId() != null;
        boolean hasPart = info.targetParts() != null && !info.targetParts().isEmpty();
        boolean hasStaffRoles = info.isStaffNotice();

        if (hasStaffRoles) {
            if (!hasGisu) {
                throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
                    "운영진 공지는 기수 지정이 필수입니다.");
            }
            if (hasChapter) {
                throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
                    "운영진 공지에 지부를 지정할 수 없습니다.");
            }
        }

        for (NoticeTargetPattern pattern : values()) {
            if (pattern.matches(hasGisu, hasChapter, hasSchool, hasPart, hasStaffRoles)) {
                log.info(
                    "매칭된 NoticeTargetPattern: {}, hasGisu: {}, hasChapter: {}, hasSchool: {}, hasPart: {}, hasStaffRoles: {}",
                    pattern.name(), hasGisu, hasChapter, hasSchool, hasPart, hasStaffRoles);
                return pattern;
            }
        }

        throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
            "지원하지 않는 대상 설정입니다. 관리자에게 문의해주세요. hasGisu: " + hasGisu + ", hasChapter: " + hasChapter
                + ", hasSchool: " + hasSchool + ", hasPart: " + hasPart + ", hasStaffRoles: " + hasStaffRoles);
    }

    // 중앙 운영진을 대상으로 할 때 학교 지정 X
    private static void validateNoSchoolForCentralRoles(NoticeTargetInfo info) {
        boolean hasCentralRole = info.targetRoles().stream().anyMatch(NoticeTargetRole::isCentralRole);
        if (hasCentralRole) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
                "중앙 운영진을 대상으로 하는 공지에 학교를 지정할 수 없습니다.");
        }
    }

    // 파트 지정이 가능한 경우인지 확인
    // 교육국, 교내파트장 대상 공지의 경우 파트지정 필요
    private static void validatePartApplicableRoles(NoticeTargetInfo info) {
        if (info.targetRoles().contains(NoticeTargetRole.SCHOOL_PRESIDENT_TEAM)) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
                "교내 회장단 대상 공지에 파트를 지정할 수 없습니다.");
        }
        if (info.targetRoles().contains(NoticeTargetRole.CENTRAL_OPERATING_TEAM)) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
                "중앙 운영국 대상 공지에 파트를 지정할 수 없습니다.");
        }
    }

    private static boolean hasStaffWritePermission(NoticeTargetInfo info, Long memberId,
                                                   GetChallengerRoleUseCase useCase) {
        boolean hasCentralRole = info.targetRoles().stream().anyMatch(NoticeTargetRole::isCentralRole);
        if (hasCentralRole) {
            return useCase.isCentralCoreInGisu(memberId, info.targetGisuId());
        }
        return useCase.isCentralMemberInGisu(memberId, info.targetGisuId());
    }

    public abstract boolean validatePermission(NoticeTargetInfo info, Long memberId, GetChallengerRoleUseCase useCase);

    private boolean matches(boolean hasGisu, boolean hasChapter, boolean hasSchool, boolean hasPart,
                            boolean hasStaffRoles) {
        return (this.hasGisu == hasGisu)
            && (this.hasChapter == hasChapter)
            && (this.hasSchool == hasSchool)
            && (this.hasPart == hasPart)
            && (this.hasStaffRoles == hasStaffRoles);
    }
}
