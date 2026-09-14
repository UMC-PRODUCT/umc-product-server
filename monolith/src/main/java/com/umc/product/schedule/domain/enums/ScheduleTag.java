package com.umc.product.schedule.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScheduleTag {
    LEADERSHIP("리더십"),
    STUDY("스터디"),
    DUES("회비"),
    MEETING("회의"),
    NETWORKING("네트워킹"),
    HACKATHON("해커톤"),
    PROJECT("프로젝트"),
    PRESENTATION("발표"),
    WORKSHOP("워크숍"),
    RETROSPECTIVE("회고"),
    AFTER_PARTY("뒤풀이"),
    ORIENTATION("오리엔테이션"),
    GENERAL("일반");

    private final String description;
}
