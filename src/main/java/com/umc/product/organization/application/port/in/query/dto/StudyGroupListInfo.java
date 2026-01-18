package com.umc.product.organization.application.port.in.query.dto;

import java.util.List;

public record StudyGroupListInfo(
        List<StudyGroupInfo> studyGroups,
        Long nextCursor,
        boolean hasNext
) {

    public record StudyGroupInfo(
            Long groupId,
            String name,
            int memberCount,
            LeaderInfo leader,
            List<MemberSummaryInfo> members
    ) {
        public record LeaderInfo(
                Long challengerId,
                String name,
                String profileImageUrl
        ) {
        }

        public record MemberSummaryInfo(
                Long challengerId,
                String name,
                String profileImageUrl) {
        }


    }

}
