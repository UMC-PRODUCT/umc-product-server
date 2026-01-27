package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.domain.enums.MissionType;
import java.time.LocalDate;
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
            LocalDate startDate,
            LocalDate endDate,
            MissionType missionType
    ) {
        private static final LocalDate DEFAULT_DATE = LocalDate.of(2099, 12, 31);
        private static final MissionType DEFAULT_MISSION_TYPE = MissionType.LINK;


        // 웹은 아직 UI가 바뀌기 전이라서 기본 값을 넣습니다.
        // TODO: 디자인 개선 후 없앨 예정
        public LocalDate resolveStartDate() {
            return startDate != null ? startDate : DEFAULT_DATE;
        }

        public LocalDate resolveEndDate() {
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
