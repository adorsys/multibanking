package de.adorsys.multibanking.web;

import de.adorsys.multibanking.Application;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.repository.BookingRepository;
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

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, BookingControllerDocumentation.TestConfiguration.class})
@WebAppConfiguration
@ActiveProfiles("ewu")
public class BookingControllerDocumentation extends AbstractControllerDocumentation {

    @Test
    public void testGetAll() throws Exception {
        mockMvc
                .perform(
                        get("/api/v1/users/{userId}/bankaccesses/{accessId}/accounts/{accountId}/bookings", new ObjectId().toString(), new ObjectId().toString(), new ObjectId().toString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_HAL_JSON_UTF8))
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("userId").description("Benutzer ID"),
                                parameterWithName("accessId").description("Bankzugang ID"),
                                parameterWithName("accountId").description("Bankkonto ID")),
                        responseFields(
                                fieldWithPath("_embedded.bookingEntityList").description("Die Liste der Umsätze")
                        )
                ));
    }

    @Test
    public void testGetSingle() throws Exception {
        mockMvc
                .perform(
                        get("/api/v1/users/{userId}/bankaccesses/{accessId}/accounts/{accountId}/bookings/{bookingId}", new ObjectId().toString(),
                                new ObjectId().toString(), new ObjectId().toString(), new ObjectId().toString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_HAL_JSON_UTF8))
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("userId").description("Benutzer ID"),
                                parameterWithName("accessId").description("Bankzugang ID"),
                                parameterWithName("accountId").description("Bankkonto ID"),
                                parameterWithName("bookingId").description("Umsatz ID")),
                        responseFields(
                                fieldWithPath("id").description("Umsatz ID"),
                                fieldWithPath("accountId").description("Bankkonto ID"),
                                fieldWithPath("externalId").description("Externe eindeutige Umsatz ID"),
                                fieldWithPath("otherAccount").description("Gegenkonto der Buchung (optional)").optional(),
                                fieldWithPath("valutaDate").description("Datum der Wertstellung"),
                                fieldWithPath("bookingDate").description("Buchungsdatum"),
                                fieldWithPath("amount").description("Gebuchter Betrag"),
                                fieldWithPath("balance").description("Der Saldo nach dem Buchen des Betrages"),
                                fieldWithPath("customerRef").description("Kundenreferenz"),
                                fieldWithPath("instRef").description("Kreditinstituts-Referenz"),
                                fieldWithPath("origValue").description("Ursprünglicher Betrag (optional)").optional(),
                                fieldWithPath("chargeValue").description("Betrag für Gebühren des Geldverkehrs (optional)").optional(),
                                fieldWithPath("additional").description("Zusatzinformationen im Rohformat"),
                                fieldWithPath("text").description("Beschreibung der Art der Buchung (optional)").optional(),
                                fieldWithPath("primanota").description("Primanotakennzeichen (optional)").optional(),
                                fieldWithPath("usage").description("Verwendungszweck"),
                                fieldWithPath("addkey").description("Erweiterte Informationen zur Art der Buchung (optional)").optional(),
                                fieldWithPath("sepa").description("Gibt an, ob ein Umsatz ein SEPA-Umsatz ist"),
                                fieldWithPath("reversal").description("Storno-Buchung")
                        )
                ));
    }

    public static BookingEntity booking() {
        return (BookingEntity)new BookingEntity()
                .id(new ObjectId().toString())
                .accountId(new ObjectId().toString())
                .externalId(UUID.randomUUID().toString())
                .valutaDate(new Date())
                .bookingDate(new Date())
                .amount(new BigDecimal(100))
                .reversal(false)
                .balance(new BigDecimal(200))
                .customerRef("NONREF")
                .instRef("")
                .origValue(new BigDecimal(100))
                .chargeValue(new BigDecimal(100))
                .sepa(false);
    }

    public static class TestConfiguration {

        private static BookingRepository mockedBookingRepository = mock(BookingRepository.class);

        static {
            when(mockedBookingRepository.findByAccountId(any(String.class))).thenAnswer(invocationOnMock ->
            {
                List<BookingEntity> bankAccountList = new ArrayList<>();
                bankAccountList.add(booking());
                return bankAccountList;
            });

            when(mockedBookingRepository.findById(any(String.class)))
                    .thenReturn(Optional.of(booking()));
        }

        @Bean
        @Primary
        public BookingRepository getMockedBookingRepositoryMock() {
            return mockedBookingRepository;
        }
    }
}
