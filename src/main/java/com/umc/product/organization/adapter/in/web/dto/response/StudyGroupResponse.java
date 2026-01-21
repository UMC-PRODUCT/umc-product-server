package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Schema(description = "스터디 그룹 상세 정보")
public record StudyGroupResponse(
                @Schema(description = "스터디 그룹 ID", example = "1") Long groupId,
                @Schema(description = "스터디 그룹명", example = "React A팀") String name,
                @Schema(description = "파트", example = "WEB") String part,
                @Schema(description = "파트 표시명", example = "웹") String partDisplayName,
                @Schema(description = "소속 학교 목록") List<School> schools,
                @Schema(description = "생성일시") LocalDateTime createdAt,
                @Schema(description = "멤버 수", example = "4") int memberCount,
                @Schema(description = "리더 정보") Leader leader,
                @Schema(description = "멤버 목록 (리더 제외)") List<Member> members) {
        public static StudyGroupResponse from(StudyGroupDetailInfo detail) {
                return new StudyGroupResponse(
                                detail.groupId(),
                                detail.name(),
                                detail.part().name(),
                                detail.part().getDisplayName(),
                                School.fromList(detail.schools()),
                                LocalDateTime.ofInstant(detail.createdAt(), ZoneId.of("Asia/Seoul")),
                                detail.memberCount(),
                                Leader.from(detail.leader()),
                                Member.fromList(detail.members()));
        }

        @Schema(description = "학교 정보")
        public record School(
                        @Schema(description = "학교 ID", example = "1") Long schoolId,
                        @Schema(description = "학교명", example = "서울대학교") String schoolName) {
                public static School from(StudyGroupDetailInfo.SchoolInfo s) {
                        return new School(s.schoolId(), s.schoolName());
                }

                public static List<School> fromList(List<StudyGroupDetailInfo.SchoolInfo> list) {
                        return list.stream().map(School::from).toList();
                }
        }

        @Schema(description = "리더 정보")
        public record Leader(
                        @Schema(description = "챌린저 ID", example = "101") Long challengerId,
                        @Schema(description = "멤버 ID", example = "1") Long memberId,
                        @Schema(description = "이름", example = "홍길동") String name,
                        @Schema(description = "프로필 이미지 URL") String profileImageUrl) {
                public static Leader from(StudyGroupDetailInfo.MemberInfo leader) {
                        if (leader == null)
                                return null;
                        return new Leader(
                                        leader.challengerId(), leader.memberId(),
                                        leader.name(), leader.profileImageUrl());
                }
        }

        @Schema(description = "멤버 정보")
        public record Member(
                        @Schema(description = "챌린저 ID", example = "102") Long challengerId,
                        @Schema(description = "멤버 ID", example = "2") Long memberId,
                        @Schema(description = "이름", example = "김철수") String name,
                        @Schema(description = "프로필 이미지 URL") String profileImageUrl) {
                public static Member from(StudyGroupDetailInfo.MemberInfo m) {
                        return new Member(m.challengerId(), m.memberId(), m.name(), m.profileImageUrl());
                }

                public static List<Member> fromList(List<StudyGroupDetailInfo.MemberInfo> list) {
                        return list.stream().map(Member::from).toList();
                }
        }
}
