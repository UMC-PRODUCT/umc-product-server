package com.umc.product.schedule.application.port.out;

// 일정 삭제
public interface DeleteSchedulePort {

    /**
     * Schedule의 id로 일정 삭제
     */
    void delete(Long scheduleId);
}
