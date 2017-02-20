package de.adorsys.multibanking.web;

import de.adorsys.multibanking.Application;
import de.adorsys.multibanking.banking.OnlineBankingService;
import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.repository.BankAccessRepository;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
@SpringBootTest(classes = {Application.class, BankAccessControllerDocumentation.TestConfiguration.class})
@WebAppConfiguration
@ActiveProfiles("ewu")
public class BankAccessControllerDocumentation extends AbstractControllerDocumentation {

    @Test
    public void testGetAll() throws Exception {
        mockMvc
                .perform(
                        get("/api/v1/users/{userId}/bankaccesses", new ObjectId().toString(), new ObjectId().toString()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_HAL_JSON_UTF8))
                .andDo(document.document(
                        responseFields(
                                fieldWithPath("_embedded.bankAccessList").description("Die Liste der Bankzugänge")
                        )
                ));
    }

    @Test
    public void testGetSingle() throws Exception {
        mockMvc
                .perform(
                        get("/api/v1/users/{userId}/bankaccesses/{accessId}", new ObjectId().toString(), new ObjectId().toString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_HAL_JSON_UTF8))
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("userId").description("Benutzer ID"),
                                parameterWithName("accessId").description("Bankzugang ID")),
                        responseFields(
                                fieldWithPath("id").description("Bankzugang ID"),
                                fieldWithPath("userId").description("Benutzer ID"),
                                fieldWithPath("bankLogin").description("Benutzername zum Bankzugang"),
                                fieldWithPath("bankCode").description("BLZ des Bankzugangs")
                        )
                ));
    }


    @Test
    public void testPost() throws Exception {
        mockMvc
                .perform(
                        post("/api/v1/users/{userId}/bankaccesses", new ObjectId().toString()).contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bankAccessBuilder().id(null).build())))
                .andExpect(status().isCreated())
                .andDo(document.document(
                        requestFields(
                                fieldWithPath("id").description("Bankzugang ID (wird am Server vergeben)"),
                                fieldWithPath("userId").description("Benutzer ID"),
                                fieldWithPath("bankLogin").description("Benutzername zum Bankzugang"),
                                fieldWithPath("bankCode").description("BLZ des Bankzugangs"))));
    }

    public static BankAccess.BankAccessBuilder bankAccessBuilder() {
        return BankAccess.builder().id(new ObjectId().toHexString()).bankCode("bankCode").bankLogin("bankLogin").userId(new ObjectId().toString());
    }

    public static class TestConfiguration {

        private static OnlineBankingService mockedOnlineBankingService = mock(OnlineBankingService.class);
        private static BankAccessRepository mockedRepository = mock(BankAccessRepository.class);

        static {
            when(mockedRepository.findByUserId(any(String.class))).thenAnswer(invocationOnMock ->
            {
                List<BankAccess> bankAccessList = new ArrayList<>();
                bankAccessList.add(bankAccessBuilder().build());
                return Optional.of(bankAccessList);
            });

            when(mockedRepository.save(any(BankAccess.class))).thenAnswer(invocationOnMock ->
            {
                BankAccess bankAccess = (BankAccess) invocationOnMock.getArguments()[0];
                bankAccess.setId(new ObjectId().toString());
                return bankAccess;
            });

            when(mockedRepository.findById(any(String.class)))
                    .thenReturn(Optional.of(BankAccess.builder().id(new ObjectId().toString()).build()));

            when(mockedOnlineBankingService.loadBankAccounts(any(BankAccess.class), any(String.class))).thenAnswer(invocationOnMock ->
                    Optional.of(Collections.emptyList()));
        }

        @Bean
        @Primary
        public BankAccessRepository getBankAccessRepositoryMock() {
            return mockedRepository;
        }

        @Bean
        @Primary
        public OnlineBankingService getBankOnlineBankingServiceMock() {
            return mockedOnlineBankingService;
        }
    }
}
