package com.umc.product.organization.application.port.in.query.dto;

import java.util.List;

public record StudyGroupListInfo(List<StudyGroupInfo> studyGroups, Long nextCursor, boolean hasNext) {

    public record StudyGroupInfo(Long groupId, String name, List<Organizer> organizers, List<Member> members) {
        public record Organizer(Long memberId, String name, String profileImageUrl) { }
        public record Member(Long memberId, String name, String profileImageUrl) { }
    }
}
