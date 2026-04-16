package com.umc.product.project.adapter.in.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project | 프로젝트 Command", description = "프로젝트 및 참여자 관련 생성, 수정, 삭제 등")
public class ProjectCommandController {


}
