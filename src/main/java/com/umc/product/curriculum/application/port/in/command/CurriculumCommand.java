package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.domain.enums.MissionType;
import java.time.Instant;
import java.util.List;

public record CurriculumCommand(
        ChallengerPart part,
        String title,
        List<WorkbookCommand> workbooks
) {
    public record WorkbookCommand(
            Long id,
            Integer weekNo,
            String title,
            String description,
            String workbookUrl,
            Instant startDate,
            Instant endDate,
            MissionType missionType
    ) {
        private static final Instant DEFAULT_DATE = Instant.parse("2099-12-31T00:00:00Z");
        private static final MissionType DEFAULT_MISSION_TYPE = MissionType.LINK;


        // 웹은 아직 UI가 바뀌기 전이라서 기본 값을 넣습니다.
        // TODO: 디자인 개선 후 없앨 예정
        public Instant resolveStartDate() {
            return startDate != null ? startDate : DEFAULT_DATE;
        }

        public Instant resolveEndDate() {
            return endDate != null ? endDate : DEFAULT_DATE;
        }

        public MissionType resolveMissionType() {
            return missionType != null ? missionType : DEFAULT_MISSION_TYPE;
        }

        public boolean hasId() {
            return id != null;
        }
    }
}
