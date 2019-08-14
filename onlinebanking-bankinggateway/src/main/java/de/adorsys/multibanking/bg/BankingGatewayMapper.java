package de.adorsys.multibanking.bg;

import de.adorsys.multibanking.banking_gateway_b2c.model.*;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.xs2a.adapter.model.AccountDetailsTO;
import de.adorsys.xs2a.adapter.service.account.AccountDetails;
import de.adorsys.xs2a.adapter.service.account.CashAccountType;
import de.adorsys.xs2a.adapter.service.account.Transactions;
import org.iban4j.Iban;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static de.adorsys.multibanking.domain.BankAccountType.fromXS2AType;

@Mapper
interface BankingGatewayMapper {

    @Mapping(source = "psuAccountIban", target = "psuAccount")
    @Mapping(source = "accounts", target = "access.accounts")
    @Mapping(source = "balances", target = "access.balances")
    @Mapping(source = "transactions", target = "access.transactions")
    @Mapping(target = "consentStatus", ignore = true)
    ConsentTO toConsentTO(Consent consentTemplate);

    @InheritInverseConfiguration
    Consent toConsent(ConsentTO consentTO);

    @Mapping(target = "bankApiConsentData", ignore = true)
    CreateConsentResponse toCreateConsentResponse(CreateConsentResponseTO consentResponse);

    UpdatePsuAuthenticationRequestTO toUpdatePsuAuthenticationRequestTO(UpdatePsuAuthenticationRequest updatePsuAuthentication);

    UpdateAuthResponse toUpdateAuthResponseTO(ResourceUpdateAuthResponseTO resourceUpdateAuthResponseTO,
                                              BankApi bankApi);

    SelectPsuAuthenticationMethodRequestTO toSelectPsuAuthenticationMethodRequestTO(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod);

    TransactionAuthorisationRequestTO toTransactionAuthorisationRequestTO(TransactionAuthorisationRequest transactionAuthorisation);

    @Mapping(target = "id", source = "authenticationMethodId")
    @Mapping(target = "inputInfo", source = "explanation")
    @Mapping(target = "medium", source = "name")
    TanTransportType toTanTransportType(ScaMethodTO scaMethodTO);

    @Mapping(target = "country", ignore = true)
    @Mapping(target = "bankName", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "syncStatus", ignore = true)
    @Mapping(target = "lastSync", ignore = true)
    @Mapping(target = "balances", ignore = true)
    @Mapping(target = "externalIdMap", expression = "java(getExternalIdMap(accountDetails.getResourceId()))")
    @Mapping(target = "blz", expression = "java(getBlz(accountDetails))")
    @Mapping(target = "accountNumber", expression = "java(getAccountNumber(accountDetails))")
    @Mapping(target = "type", expression = "java(getAccounType(accountDetails.getCashAccountType()))")
    BankAccount toBankAccount(AccountDetails accountDetails);

    default Map<BankApi, String> getExternalIdMap(String accountResourceId) {
        Map<BankApi, String> externalIdMap = new EnumMap<>(BankApi.class);
        externalIdMap.put(BankApi.BANKING_GATEWAY, accountResourceId);
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

    @Mapping(source = "valueDate", target = "valutaDate")
    @Mapping(source = "transactionAmount.amount", target = "amount")
    @Mapping(source = "transactionAmount.currency", target = "currency")
    @Mapping(source = "endToEndId", target = "externalId")
    @Mapping(source = "remittanceInformationUnstructured", target = "usage")
    default Booking toBooking(Transactions transactionDetails) {
        Booking booking = new Booking();
        booking.setBankApi(BankApi.BANKING_GATEWAY);
        booking.setBookingDate(transactionDetails.getBookingDate());
        booking.setValutaDate(transactionDetails.getValueDate());
        booking.setAmount(new BigDecimal(transactionDetails.getTransactionAmount().getAmount()));
        booking.setCurrency(transactionDetails.getTransactionAmount().getCurrency());
        booking.setExternalId(transactionDetails.getEndToEndId());
        booking.setUsage(transactionDetails.getRemittanceInformationUnstructured());

        if (transactionDetails.getCreditorName() != null || transactionDetails.getDebtorName() != null) {
            BankAccount bankAccount = new BankAccount();
            bankAccount.setOwner(transactionDetails.getCreditorName() != null ? transactionDetails.getCreditorName()
                : transactionDetails.getDebtorName());
            booking.setOtherAccount(bankAccount);
        }

        return booking;
    }
}
