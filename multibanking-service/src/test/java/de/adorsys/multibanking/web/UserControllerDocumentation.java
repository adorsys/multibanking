package de.adorsys.multibanking.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.multibanking.Application;
import de.adorsys.multibanking.domain.User;
import de.adorsys.multibanking.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, de.adorsys.multibanking.web.UserControllerDocumentation.TestConfiguration.class})
@WebAppConfiguration
@ActiveProfiles("ewu")
public class UserControllerDocumentation {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;
    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    private MockMvc mockMvc;
    private RestDocumentationResultHandler document;


    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation)).build();
    }

    @Test
    public void testPost() throws Exception {

        mockMvc
                .perform(
                        post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user())))
                .andExpect(status().isCreated())
                .andDo(document("users-create-example",
                        requestFields(
                                fieldWithPath("id").description("The user id"))));
    }
    public static User user() {
        return User.builder()
                .build();
    }

    public static class TestConfiguration {

        private static UserRepository mockedRepository = mock(UserRepository.class);

        static {
            when(mockedRepository.save(any(User.class))).thenAnswer(invocationOnMock ->
            {
                User user = (User) invocationOnMock.getArguments()[0];
                user.setId(new ObjectId().toString());
                return user;
            });
        }

        @Bean
        @Primary
        public UserRepository getUserRepositoryMock() {
            return mockedRepository;
        }
    }
}
