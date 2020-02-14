package de.adorsys.multibanking.ing;

import de.adorsys.multibanking.domain.Balance;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.ing.api.Account;
import de.adorsys.multibanking.ing.api.Transaction;
import org.iban4j.Iban;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static de.adorsys.multibanking.domain.BankApi.ING;
import static de.adorsys.multibanking.domain.BankApi.XS2A;

@Mapper
interface IngMapper {

    @Mapping(target = "consentId", expression = "java( java.util.UUID.randomUUID().toString() )")
    @Mapping(target = "authorisationId", expression = "java( java.util.UUID.randomUUID().toString() )")
    @Mapping(target = "bankApiConsentData", expression = "java( bankApiConsentData )")
    @Mapping(target = "redirectId", ignore = true)
    @Mapping(target = "authorisationCodeResponse", ignore = true)
    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "oauthRedirectUrl", ignore = true)
    @Mapping(target = "scaApproach", constant = "OAUTH")
    CreateConsentResponse toCreateConsentResponse(IngSessionData bankApiConsentData, String redirectUrl);

    @Mapping(target = "challenge", ignore = true)
    @Mapping(target = "scaMethods", ignore = true)
    @Mapping(target = "authorisationCodeResponse", ignore = true)
    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "bankApiConsentData", ignore = true)
    UpdateAuthResponse toUpdateAuthResponse(IngSessionData ingSessionData,
                                            @MappingTarget UpdateAuthResponse updateAuthResponse);

    List<Booking> mapToBookings(List<Transaction> transactions);

    @Mapping(source = "valueDate", target = "valutaDate")
    @Mapping(source = "transactionAmount.amount", target = "amount")
    @Mapping(source = "transactionAmount.currency", target = "currency")
    @Mapping(source = "endToEndId", target = "externalId")
    @Mapping(source = "remittanceInformationUnstructured.reference", target = "usage")
    default Booking toBooking(Transaction transactionDetails) {
        Booking booking = new Booking();
        booking.setBankApi(ING);
        booking.setBookingDate(transactionDetails.getBookingDate());
        booking.setValutaDate(transactionDetails.getValueDate());
        booking.setAmount(new BigDecimal(transactionDetails.getTransactionAmount().getAmount()));
        booking.setCurrency(transactionDetails.getTransactionAmount().getCurrency());
        booking.setExternalId(transactionDetails.getEndToEndId());
        booking.setUsage(transactionDetails.getRemittanceInformationUnstructured());
        booking.setTransactionCode(transactionDetails.getRemittanceInformationStructured() == null ? null :
            transactionDetails.getRemittanceInformationStructured().getReferenceType());

        BankAccount bankAccount = new BankAccount();
        if (transactionDetails.getCreditorName() != null || transactionDetails.getCreditorAccount() != null) {
            bankAccount.setOwner(transactionDetails.getCreditorName());
            bankAccount.setIban(transactionDetails.getCreditorAccount().getIban());
        } else if (transactionDetails.getDebtorName() != null || transactionDetails.getDebtorAccount() != null) {
            bankAccount.setOwner(transactionDetails.getDebtorName());
            bankAccount.setIban(transactionDetails.getDebtorAccount().getIban());
        }
        booking.setOtherAccount(bankAccount);

        return booking;
    }

    @Mapping(target = "amount", source = "balanceAmount.amount")
    @Mapping(target = "date", source = "referenceDate")
    @Mapping(target = "currency", source = "balanceAmount.currency")
    Balance toBalance(de.adorsys.multibanking.ing.api.Balance balance);

    List<BankAccount> toBankAccounts(List<Account> accountDetails);

    @Mapping(target = "country", ignore = true)
    @Mapping(target = "bankName", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "syncStatus", ignore = true)
    @Mapping(target = "lastSync", ignore = true)
    @Mapping(target = "balances", ignore = true)
    @Mapping(target = "externalIdMap", expression = "java(getExternalIdMap(accountDetails.getResourceId().toString()))")
    @Mapping(target = "blz", expression = "java(getBlz(accountDetails))")
    @Mapping(target = "accountNumber", expression = "java(getAccountNumber(accountDetails))")
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "bic", ignore = true)
    BankAccount toBankAccount(Account accountDetails);

    default Map<BankApi, String> getExternalIdMap(String accountResourceId) {
        Map<BankApi, String> externalIdMap = new EnumMap<>(BankApi.class);
        externalIdMap.put(XS2A, accountResourceId);
        return externalIdMap;
    }

    default String getBlz(Account accountDetails) {
        return Iban.valueOf(accountDetails.getIban()).getBankCode();
    }

    default String getAccountNumber(Account accountDetails) {
        return Iban.valueOf(accountDetails.getIban()).getAccountNumber();
    }

}

