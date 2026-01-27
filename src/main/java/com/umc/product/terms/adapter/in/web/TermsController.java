package com.umc.product.terms.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.security.annotation.Public;
import com.umc.product.terms.adapter.in.web.dto.request.CreateTermRequest;
import com.umc.product.terms.adapter.in.web.dto.response.TermsResponse;
import com.umc.product.terms.application.port.in.command.ManageTermsUseCase;
import com.umc.product.terms.application.port.in.command.dto.CreateTermCommand;
import com.umc.product.terms.application.port.in.query.GetTermsUseCase;
import com.umc.product.terms.domain.enums.TermsType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/terms")
@Tag(name = Constants.TERMS)
public class TermsController {

    private final GetTermsUseCase getTermsUseCase;
    private final ManageTermsUseCase manageTermsUseCase;

    @GetMapping("type/{termsType}")
    @Public
    @Operation(summary = "약관 유형으로 약관 조회")
    TermsResponse getTerms(@PathVariable TermsType termsType) {
        return TermsResponse.from(getTermsUseCase.getTermsByType(termsType));
    }

    @GetMapping("{termsId}")
    @Public
    @Operation(summary = "약관 ID로 약관 조회")
    TermsResponse getTermsById(@PathVariable Long termsId) {
        return TermsResponse.from(getTermsUseCase.getTermsById(termsId));
    }

    @PostMapping
    @Operation(summary = "약관 생성")
    Long createTerms(@RequestBody CreateTermRequest request) {
        return manageTermsUseCase.createTerms(
                CreateTermCommand.builder()
                        .title(request.title())
                        .content(request.content())
                        .version(request.version())
                        .required(request.isMandatory())
                        .type(request.termsType())
                        .effectiveDate(request.effectiveDate())
                        .build()
        );
    }

    @Operation(summary = "WIP: 약관 삭제")
    @DeleteMapping("{termId}")
    void deleteTerms(@PathVariable Long termId) {
        throw new NotImplementedException();
    }
}
