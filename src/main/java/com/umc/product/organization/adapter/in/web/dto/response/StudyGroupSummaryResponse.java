package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스터디 그룹 요약 정보")
public record StudyGroupSummaryResponse(
        @Schema(description = "스터디 그룹 ID", example = "1") Long groupId,
        @Schema(description = "스터디 그룹명", example = "React A팀") String name,
        @Schema(description = "파트장 목록") List<Mentor> mentors,
        @Schema(description = "멤버 목록") List<Member> members) {

    public static StudyGroupSummaryResponse
    from(StudyGroupListInfo.StudyGroupInfo info) {
        return new StudyGroupSummaryResponse(
                info.groupId(),
                info.name(),
                info.mentors().stream().map(Mentor::from).toList(),
                info.members().stream().map(Member::from).toList());
    }

    @Schema(description = "스터디 파트장 요약 정보")
    public record Mentor(
            @Schema(description = "멤버 ID", example = "101") Long memberId,
            @Schema(description = "이름", example = "홍길동") String name,
            @Schema(description = "프로필 이미지 URL") String profileImageUrl) {

        public static Mentor from(StudyGroupListInfo.StudyGroupInfo.Mentor mentor) {
            return new Mentor(mentor.memberId(), mentor.name(), mentor.profileImageUrl());
        }
    }

    @Schema(description = "멤버 요약 정보")
    public record Member(
            @Schema(description = "멤버 ID", example = "102") Long memberId,
            @Schema(description = "이름", example = "김철수") String name,
            @Schema(description = "프로필 이미지 URL") String profileImageUrl) {

        public static Member from(StudyGroupListInfo.StudyGroupInfo.Member member) {
            return new Member(member.memberId(), member.name(), member.profileImageUrl());
        }
    }
}
