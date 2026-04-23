package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스터디 그룹 요약 정보")
public record StudyGroupSummaryResponse(
        @Schema(description = "스터디 그룹 ID", example = "1") Long groupId,
        @Schema(description = "스터디 그룹명", example = "React A팀") String name,
        @Schema(description = "운영진 목록") List<Organizer> organizers,
        @Schema(description = "멤버 목록") List<Member> members) {

    public static StudyGroupSummaryResponse
    from(StudyGroupListInfo.StudyGroupInfo g) {
        return new StudyGroupSummaryResponse(
                g.groupId(),
                g.name(),
                g.organizers().stream().map(Organizer::from).toList(),
                g.members().stream().map(Member::from).toList());
    }

    @Schema(description = "스터디 파트장 요약 정보")
    public record Organizer(
            @Schema(description = "멤버 ID", example = "101") Long memberId,
            @Schema(description = "이름", example = "홍길동") String name,
            @Schema(description = "프로필 이미지 URL") String profileImageUrl) {

        public static Organizer from(StudyGroupListInfo.StudyGroupInfo.Organizer o) {
            return new Organizer(o.memberId(), o.name(), o.profileImageUrl());
        }
    }

    @Schema(description = "멤버 요약 정보")
    public record Member(
            @Schema(description = "멤버 ID", example = "102") Long memberId,
            @Schema(description = "이름", example = "김철수") String name,
            @Schema(description = "프로필 이미지 URL") String profileImageUrl) {

        public static Member from(StudyGroupListInfo.StudyGroupInfo.Member m) {
            return new Member(m.memberId(), m.name(), m.profileImageUrl());
        }
    }
}
