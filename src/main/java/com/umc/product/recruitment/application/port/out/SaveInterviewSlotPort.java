package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.InterviewSlot;
import java.util.List;

public interface SaveInterviewSlotPort {
    void saveAll(List<InterviewSlot> slots);
}
