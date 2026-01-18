package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.LeaderInfo;
import com.umc.product.organization.application.port.in.query.dto.MemberSummaryInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 스터디 그룹 목록 관련 DTO
 * CursorResponse&lt;Summary&gt; 형태로 사용됩니다.
 */
public final class StudyGroupListResponse {

        private StudyGroupListResponse() {
        }

        @Schema(description = "스터디 그룹 요약 정보")
        public record Summary(
                        @Schema(description = "스터디 그룹 ID", example = "1") Long groupId,
                        @Schema(description = "스터디 그룹명", example = "React A팀") String name,
                        @Schema(description = "멤버 수", example = "4") int memberCount,
                        @Schema(description = "리더 정보") Leader leader,
                        @Schema(description = "멤버 목록") List<Member> members) {
                public static Summary from(StudyGroupInfo g) {
                        return new Summary(
                                        g.groupId(),
                                        g.name(),
                                        g.memberCount(),
                                        Leader.from(g.leader()),
                                        Member.fromList(g.members()));
                }

                public static List<Summary> fromList(List<StudyGroupInfo> list) {
                        return list.stream().map(Summary::from).toList();
                }
        }

        @Schema(description = "리더 요약 정보")
        public record Leader(
                        @Schema(description = "챌린저 ID", example = "101") Long challengerId,
                        @Schema(description = "이름", example = "홍길동") String name,
                        @Schema(description = "프로필 이미지 URL") String profileImageUrl) {
                public static Leader from(LeaderInfo leader) {
                        if (leader == null)
                                return null;
                        return new Leader(leader.challengerId(), leader.name(), leader.profileImageUrl());
                }
        }

        @Schema(description = "멤버 요약 정보")
        public record Member(
                        @Schema(description = "챌린저 ID", example = "102") Long challengerId,
                        @Schema(description = "이름", example = "김철수") String name,
                        @Schema(description = "프로필 이미지 URL") String profileImageUrl) {
                public static Member from(MemberSummaryInfo m) {
                        return new Member(m.challengerId(), m.name(), m.profileImageUrl());
                }

                public static List<Member> fromList(List<MemberSummaryInfo> list) {
                        if (list == null)
                                return List.of();
                        return list.stream().map(Member::from).toList();
                }
        }
}
