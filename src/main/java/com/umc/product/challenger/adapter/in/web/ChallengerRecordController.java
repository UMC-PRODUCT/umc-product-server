package com.umc.product.challenger.adapter.in.web;

import com.umc.product.challenger.adapter.in.web.dto.request.AddChallengerRecordRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/challenger/record")
@RequiredArgsConstructor
@Tag(name = "Challenger | 이전 기수 챌린저 기록 추가", description = "챌린저 관련 API")
public class ChallengerRecordController {

    @PostMapping
    public void addChallengerRecord(@RequestBody AddChallengerRecordRequest request) {
        // TODO: 챌린저 기록 추가 로직 구현
    }
}
