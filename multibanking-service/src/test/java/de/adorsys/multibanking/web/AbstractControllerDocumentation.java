package de.adorsys.multibanking.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

/**
 * Created by alexg on 20.02.17.
 */
public class AbstractControllerDocumentation {

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;
    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    protected RestDocumentationResultHandler document;
    protected MockMvc mockMvc;

    public static final MediaType APPLICATION_HAL_JSON_UTF8 = MediaType.valueOf("application/hal+json;charset=UTF-8");

    @Before
    public void setUp() {
        objectMapper.registerModule(new Jackson2HalModule());
        this.document = document("{ClassName}/{methodName}", Preprocessors.preprocessRequest(new OperationPreprocessor[]{Preprocessors.removeHeaders(new String[]{"Host"}), prettyPrint()}));
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).apply(MockMvcRestDocumentation.documentationConfiguration(this.restDocumentation)).alwaysDo(this.document).build();
    }
}
