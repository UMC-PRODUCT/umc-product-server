package com.umc.product.term.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.term.adapter.in.web.dto.request.CreateTermRequest;
import com.umc.product.term.adapter.in.web.dto.response.TermResponse;
import com.umc.product.term.application.port.in.command.ManageTermUseCase;
import com.umc.product.term.application.port.in.command.dto.CreateTermCommand;
import com.umc.product.term.application.port.in.query.GetTermUseCase;
import com.umc.product.term.domain.enums.TermType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/terms")
@Tag(name = "Terms | 약관", description = "")
public class TermController {

    private final GetTermUseCase getTermUseCase;
    private final ManageTermUseCase manageTermUseCase;

    @GetMapping("type/{termType}")
    @Public
    @Operation(summary = "약관 유형으로 약관 조회")
    TermResponse getTerms(@PathVariable TermType termType) {
        return TermResponse.from(getTermUseCase.getTermsByType(termType));
    }

    @GetMapping("{termsId}")
    @Public
    @Operation(summary = "약관 ID로 약관 조회")
    TermResponse getTermsById(@PathVariable Long termsId) {
        return TermResponse.from(getTermUseCase.getTermsById(termsId));
    }

    @PostMapping
    @Operation(summary = "약관 생성", description = "약관 삭제는 지원하지 않습니다. 새로운 약관을 생성하면 기존 약관이 비활성화됩니다.")
    Long createTerms(@Valid @RequestBody CreateTermRequest request) {
        return manageTermUseCase.createTerms(
            CreateTermCommand.builder()
                .link(request.link())
                .required(request.isMandatory())
                .type(request.termType())
                .build()
        );
    }
}
