package com.umc.product.support;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.organization.adapter.in.web.ChapterQueryController;
import com.umc.product.organization.adapter.in.web.GisuController;
import com.umc.product.organization.adapter.in.web.GisuQueryController;
import com.umc.product.organization.adapter.in.web.SchoolController;
import com.umc.product.organization.adapter.in.web.SchoolQueryController;
import com.umc.product.organization.application.port.in.command.ManageGisuUseCase;
import com.umc.product.organization.application.port.in.command.ManageSchoolUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        SchoolController.class,
        SchoolQueryController.class,
        ChapterQueryController.class,
        GisuController.class,
        GisuQueryController.class,
})
@Import({
        RestDocsConfig.class,
})
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class DocumentationTest {


    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RestDocumentationResultHandler restDocsHandler;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    protected ManageSchoolUseCase manageSchoolUseCase;

    @MockitoBean
    protected GetSchoolUseCase getSchoolUseCase;

    @MockitoBean
    protected GetChapterUseCase getChapterUseCase;

    @MockitoBean
    protected ManageGisuUseCase manageGisuUseCase;

    @MockitoBean
    protected GetGisuUseCase getGisuUseCase;
}
