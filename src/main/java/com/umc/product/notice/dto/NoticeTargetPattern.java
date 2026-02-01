package com.umc.product.notice.dto;

import com.umc.product.authorization.application.port.in.query.GetMemberRolesUseCase;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;

public enum NoticeTargetPattern {
    // 전체 기수 대상
    ALL_GISU_ALL_TARGET(null, null, null, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetMemberRolesUseCase useCase) {
            return useCase.isCentralCore(memberId);
        }
    },

    ALL_GISU_SPECIFIC_SCHOOL(null, null, true, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetMemberRolesUseCase useCase) {
            return useCase.isSchoolCore(memberId, info.targetSchoolId());
        }
    },

    ALL_GISU_SPECIFIC_SCHOOL_WITH_PART(null, null, true, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetMemberRolesUseCase useCase) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
                "전체 기수를 대상으로 하는 경우, 교내 특정 파트를 한정한 공지는 불가능합니다.");
        }
    },

    ALL_GISU_WITH_CHAPTER(null, true, null, null) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetMemberRolesUseCase useCase) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
                "기수가 주어지지 않은 상태에서 지부 대상으로 공지를 작성할 수 없습니다.");
        }
    },

    // 특정 기수 대상
    SPECIFIC_GISU_ALL_TARGET(true, null, null, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetMemberRolesUseCase useCase) {
            return useCase.isCentralCore(memberId);
        }
    },

    SPECIFIC_GISU_SPECIFIC_PART(true, null, null, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetMemberRolesUseCase useCase) {
            return useCase.hasRole(memberId, ChallengerRoleType.CENTRAL_EDUCATION_TEAM_MEMBER);
        }
    },

    SPECIFIC_GISU_SPECIFIC_SCHOOL(true, null, true, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetMemberRolesUseCase useCase) {
            return useCase.isSchoolCore(memberId, info.targetSchoolId());
        }
    },

    SPECIFIC_GISU_SPECIFIC_SCHOOL_WITH_PART(true, null, true, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetMemberRolesUseCase useCase) {
            return useCase.isSchoolCore(memberId, info.targetSchoolId());
        }
    },

    SPECIFIC_GISU_SPECIFIC_CHAPTER(true, true, null, false) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetMemberRolesUseCase useCase) {
            return useCase.isChapterPresident(memberId, info.targetChapterId());
        }
    },

    SPECIFIC_GISU_SPECIFIC_CHAPTER_WITH_PART(true, true, null, true) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetMemberRolesUseCase useCase) {
            return useCase.isChapterPresident(memberId, info.targetChapterId());
        }
    },

    INVALID_GISU_CHAPTER_SCHOOL(true, true, true, null) {
        @Override
        public boolean validatePermission(NoticeTargetInfo info, Long memberId, GetMemberRolesUseCase useCase) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
                "기수, 지부, 학교는 동시에 지정할 수 없습니다.");
        }
    };

    private final Boolean hasGisu;
    private final Boolean hasChapter;
    private final Boolean hasSchool;
    private final Boolean hasPart;

    NoticeTargetPattern(Boolean hasGisu, Boolean hasChapter, Boolean hasSchool, Boolean hasPart) {
        this.hasGisu = hasGisu;
        this.hasChapter = hasChapter;
        this.hasSchool = hasSchool;
        this.hasPart = hasPart;
    }

    public static NoticeTargetPattern from(NoticeTargetInfo info) {
        boolean hasGisu = info.targetGisuId() != null;
        boolean hasChapter = info.targetChapterId() != null;
        boolean hasSchool = info.targetSchoolId() != null;
        boolean hasPart = info.targetParts() != null && !info.targetParts().isEmpty();

        for (NoticeTargetPattern pattern : values()) {
            if (pattern.matches(hasGisu, hasChapter, hasSchool, hasPart)) {
                return pattern;
            }
        }

        throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
            "지원하지 않는 대상 설정입니다.");
    }

    public abstract boolean validatePermission(NoticeTargetInfo info, Long memberId, GetMemberRolesUseCase useCase);

    private boolean matches(boolean hasGisu, boolean hasChapter, boolean hasSchool, boolean hasPart) {
        return (this.hasGisu == null || this.hasGisu == hasGisu)
            && (this.hasChapter == null || this.hasChapter == hasChapter)
            && (this.hasSchool == null || this.hasSchool == hasSchool)
            && (this.hasPart == null || this.hasPart == hasPart);
    }
}
