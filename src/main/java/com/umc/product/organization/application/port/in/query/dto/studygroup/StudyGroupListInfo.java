package com.umc.product.organization.application.port.in.query.dto.studygroup;

import java.util.List;

public record StudyGroupListInfo(List<StudyGroupInfo> studyGroups, Long nextCursor, boolean hasNext) {

    public record StudyGroupInfo(Long groupId, String name, List<Mentor> mentors, List<Member> members) {
        public record Mentor(Long memberId, String name, String profileImageUrl) {
        }

        public record Member(Long memberId, String name, String profileImageUrl) {
        }
    }
}
