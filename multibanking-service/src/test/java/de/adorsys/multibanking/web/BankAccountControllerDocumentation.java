package de.adorsys.multibanking.web;

import de.adorsys.multibanking.Application;
import de.adorsys.multibanking.banking.OnlineBankingService;
import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.repository.BankAccessRepository;
import de.adorsys.multibanking.repository.BankAccountRepository;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, BankAccountControllerDocumentation.TestConfiguration.class})
@WebAppConfiguration
@ActiveProfiles("ewu")
public class BankAccountControllerDocumentation extends AbstractControllerDocumentation {

    @Test
    public void testGetAll() throws Exception {
        mockMvc
                .perform(
                        get("/api/v1/users/{userId}/bankaccesses/{accessId}/accounts", new ObjectId().toString(), new ObjectId().toString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_HAL_JSON_UTF8))
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("userId").description("Benutzer ID"),
                                parameterWithName("accessId").description("Bankzugang ID")),
                        responseFields(
                                fieldWithPath("_embedded.bankAccountList").description("Die Liste der Bankkonten")
                        )
                ));
    }

    @Test
    public void testGetSingle() throws Exception {
        mockMvc
                .perform(
                        get("/api/v1/users/{userId}/bankaccesses/{accessId}/accounts/{accountId}", new ObjectId().toString(), new ObjectId().toString(), new ObjectId().toString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_HAL_JSON_UTF8))
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("userId").description("Benutzer ID"),
                                parameterWithName("accessId").description("Bankzugang ID"),
                                parameterWithName("accountId").description("Bankkonto ID")),
                        responseFields(
                                fieldWithPath("id").description("Bankkonto ID"),
                                fieldWithPath("bankAccessId").description("Bankzugang ID"),
                                fieldWithPath("bankAccountBalance").description("Kontostand"),
                                fieldWithPath("countryHbciAccount").description("Länderkennung"),
                                fieldWithPath("blzHbciAccount").description("Bankleitzahl"),
                                fieldWithPath("numberHbciAccount").description("Kontonummer"),
                                fieldWithPath("typeHbciAccount").description("Kontotyp"),
                                fieldWithPath("currencyHbciAccount").description("Währung"),
                                fieldWithPath("nameHbciAccount").description("Kontoinhaber"),
                                fieldWithPath("bicHbciAccount").description("BIC"),
                                fieldWithPath("ibanHbciAccount").description("IBAN")
                        )
                ));
    }

    @Test
    public void testSyncBookings() throws Exception {
        mockMvc
                .perform(
                        put("/api/v1/users/{userId}/bankaccesses/{accessId}/accounts/{accountId}/sync", new ObjectId().toString(), new ObjectId().toString(), new ObjectId().toString())
                                .contentType(MediaType.APPLICATION_JSON).content("testPin"))
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("userId").description("Benutzer ID"),
                                parameterWithName("accessId").description("Bankzugang ID"),
                                parameterWithName("accountId").description("Account ID"))
                ));
    }

    public static BankAccount bankAccount() {
        return BankAccount.builder()
                .id(new ObjectId().toString())
                .bankAccessId(new ObjectId().toString())
                .countryHbciAccount("DE")
                .blzHbciAccount("12345678")
                .numberHbciAccount("987655433")
                .typeHbciAccount("Kontotyp")
                .currencyHbciAccount("EUR")
                .nameHbciAccount("Max Mustermann")
                .bicHbciAccount("GENODEF1S06")
                .ibanHbciAccount("DE12345600098765432")
                .build();
    }

    public static class TestConfiguration {

        private static OnlineBankingService mockedOnlineBankingService = mock(OnlineBankingService.class);
        private static BankAccessRepository mockedBankAccessRepository = mock(BankAccessRepository.class);
        private static BankAccountRepository mockedBankAccountRepository = mock(BankAccountRepository.class);

        static {
            when(mockedBankAccountRepository.findByBankAccessId(any(String.class))).thenAnswer(invocationOnMock ->
            {
                List<BankAccount> bankAccountList = new ArrayList<>();
                bankAccountList.add(bankAccount());
                return Optional.of(bankAccountList);
            });

            when(mockedBankAccessRepository.findById(any(String.class)))
                    .thenReturn(Optional.of(BankAccess.builder().id(new ObjectId().toString()).build()));

            when(mockedBankAccountRepository.findById(any(String.class)))
                    .thenReturn(Optional.of(BankAccount.builder().id(new ObjectId().toString()).build()));

            when(mockedOnlineBankingService.loadBookings(any(BankAccess.class), any(BankAccount.class), any(String.class))).thenAnswer(invocationOnMock ->
                    Optional.of(Collections.emptyList()));
        }

        @Bean
        @Primary
        public BankAccessRepository getBankAccountRepositoryMock() {
            return mockedBankAccessRepository;
        }

        @Bean
        @Primary
        public BankAccountRepository getBankAccessRepositoryMock() {
            return mockedBankAccountRepository;
        }

        @Bean
        @Primary
        public OnlineBankingService getBankOnlineBankingServiceMock() {
            return mockedOnlineBankingService;
        }
    }
}
