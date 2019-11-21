package de.adorsys.multibanking.bg;

import de.adorsys.multibanking.banking_gateway_b2c.model.*;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.Message;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.xs2a.adapter.service.model.AccountDetails;
import de.adorsys.xs2a.adapter.service.model.CashAccountType;
import de.adorsys.xs2a.adapter.service.model.TppMessage;
import de.adorsys.xs2a.adapter.service.model.Transactions;
import org.iban4j.Iban;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static de.adorsys.multibanking.domain.BankAccountType.fromXS2AType;
import static de.adorsys.multibanking.domain.BankApi.XS2A;

@Mapper
interface BankingGatewayMapper {

    @Mapping(source = "psuAccountIban", target = "psuAccount")
    @Mapping(source = "accounts", target = "access.accounts")
    @Mapping(source = "balances", target = "access.balances")
    @Mapping(source = "transactions", target = "access.transactions")
    @Mapping(target = "consentStatus", ignore = true)
    ConsentTO toConsentTO(Consent consentTemplate);

    @Mapping(target = "redirectId", ignore = true)
    @InheritInverseConfiguration
    Consent toConsent(ConsentTO consentTO);

    @Mapping(target = "bankApiConsentData", ignore = true)
    @Mapping(target = "redirectId", ignore = true)
    CreateConsentResponse toCreateConsentResponse(CreateConsentResponseTO consentResponse);

    @Mapping(target = "psuId", source = "customerId")
    @Mapping(target = "psuCorporateId", source = "userId")
    @Mapping(target = "password", source = "pin")
    UpdatePsuAuthenticationRequestTO toUpdatePsuAuthenticationRequestTO(Credentials credentials);

    UpdateAuthResponse toUpdateAuthResponseTO(ResourceOfUpdateAuthResponseTO resourceUpdateAuthResponseTO,
                                              BankApi bankApi);

    SelectPsuAuthenticationMethodRequestTO toSelectPsuAuthenticationMethodRequestTO(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod);

    TransactionAuthorisationRequestTO toTransactionAuthorisationRequestTO(TransactionAuthorisationRequest transactionAuthorisation);

    @Mapping(target = "id", source = "authenticationMethodId")
    @Mapping(target = "inputInfo", source = "explanation")
    @Mapping(target = "medium", source = "name")
    @Mapping(target = "type", source = "authenticationType")
    @Mapping(target = "needTanMedia", ignore = true)
    TanTransportType toTanTransportType(ScaMethodTO scaMethodTO);

    List<BankAccount> toBankAccounts(List<AccountDetails> accountDetails);

    @Mapping(target = "country", ignore = true)
    @Mapping(target = "bankName", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "syncStatus", ignore = true)
    @Mapping(target = "lastSync", ignore = true)
    @Mapping(target = "balances", ignore = true)
    @Mapping(target = "bic", ignore = true)
    @Mapping(target = "externalIdMap", expression = "java(getExternalIdMap(accountDetails.getResourceId()))")
    @Mapping(target = "blz", expression = "java(getBlz(accountDetails))")
    @Mapping(target = "accountNumber", expression = "java(getAccountNumber(accountDetails))")
    @Mapping(target = "type", expression = "java(getAccounType(accountDetails.getCashAccountType()))")
    BankAccount toBankAccount(AccountDetails accountDetails);

    default Map<BankApi, String> getExternalIdMap(String accountResourceId) {
        Map<BankApi, String> externalIdMap = new EnumMap<>(BankApi.class);
        externalIdMap.put(XS2A, accountResourceId);
        return externalIdMap;
    }

    default String getBlz(AccountDetails accountDetails) {
        return Iban.valueOf(accountDetails.getIban()).getBankCode();
    }

    default String getAccountNumber(AccountDetails accountDetails) {
        return Iban.valueOf(accountDetails.getIban()).getAccountNumber();
    }

    default BankAccountType getAccounType(CashAccountType cashAccountType) {
        return fromXS2AType(cashAccountType.toString());
    }

    List<Booking> toBookings(List<Transactions> transactionDetails);

    @Mapping(source = "valueDate", target = "valutaDate")
    @Mapping(source = "transactionAmount.amount", target = "amount")
    @Mapping(source = "transactionAmount.currency", target = "currency")
    @Mapping(source = "endToEndId", target = "externalId")
    @Mapping(source = "remittanceInformationUnstructured", target = "usage")
    default Booking toBooking(Transactions transactionDetails) {
        Booking booking = new Booking();
        booking.setBankApi(XS2A);
        booking.setBookingDate(transactionDetails.getBookingDate());
        booking.setValutaDate(transactionDetails.getValueDate());
        booking.setAmount(new BigDecimal(transactionDetails.getTransactionAmount().getAmount()));
        booking.setCurrency(transactionDetails.getTransactionAmount().getCurrency());
        booking.setExternalId(transactionDetails.getEndToEndId());
        booking.setUsage(transactionDetails.getRemittanceInformationUnstructured());
        booking.setTransactionCode(transactionDetails.getPurposeCode() == null ? null : transactionDetails.getPurposeCode().getCode());

        BankAccount bankAccount = new BankAccount();
        if (transactionDetails.getCreditorName() != null || transactionDetails.getCreditorAccount() !=null) {
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
    Balance toBalance(de.adorsys.xs2a.adapter.service.model.Balance balance);

    List<Message> toMessages(List<MessageTO> messagesTO);

    @Mapping(target = "severity", source = "category")
    @Mapping(target = "key", source = "code")
    @Mapping(target = "renderedMessage", source = "text")
    Message toMessage(TppMessage tppMessage);
}
