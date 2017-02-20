package de.adorsys.multibanking.web;

import de.adorsys.multibanking.Application;
import de.adorsys.multibanking.domain.User;
import de.adorsys.multibanking.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, de.adorsys.multibanking.web.UserControllerDocumentation.TestConfiguration.class})
@WebAppConfiguration
@ActiveProfiles("ewu")
public class UserControllerDocumentation extends AbstractControllerDocumentation {

    @Test
    public void testGet() throws Exception {
        mockMvc
                .perform(
                        get("/api/v1/users/{id}", new ObjectId().toString()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_HAL_JSON_UTF8))
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("id").description("Benutzer ID")),
                        responseFields(
                                fieldWithPath("id").description("Benutzer ID")
                        )
                ));
    }


    @Test
    public void testPost() throws Exception {
        mockMvc
                .perform(
                        post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user())))
                .andExpect(status().isCreated())
                .andDo(document.document(
                        requestFields(
                                fieldWithPath("id").description("Benutzer ID (wird am Server vergeben)"))));
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

            when(mockedRepository.findById(any(String.class)))
                    .thenReturn(Optional.of(User.builder().id(new ObjectId().toString()).build()));

        }

        @Bean
        @Primary
        public UserRepository getUserRepositoryMock() {
            return mockedRepository;
        }
    }
}
