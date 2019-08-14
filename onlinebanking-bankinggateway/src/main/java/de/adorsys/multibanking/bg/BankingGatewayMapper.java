package de.adorsys.multibanking.bg;

import de.adorsys.multibanking.banking_gateway_b2c.model.*;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.xs2a.adapter.model.AccountDetailsTO;
import org.iban4j.Iban;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
    @Mapping(target = "externalIdMap", expression = "java(getExternalIdMap(accountDetailsTO.getResourceId()))")
    @Mapping(target = "blz", expression = "java(getBlz(accountDetailsTO))")
    @Mapping(target = "accountNumber", expression = "java(getAccountNumber(accountDetailsTO))")
    @Mapping(target = "type", expression = "java(getAccounType(accountDetailsTO.getCashAccountType()))")
    BankAccount toBankAccount(AccountDetailsTO accountDetailsTO);

    default Map<BankApi, String> getExternalIdMap(String accountResourceId) {
        Map<BankApi, String> externalIdMap = new EnumMap<>(BankApi.class);
        externalIdMap.put(BankApi.BANKING_GATEWAY, accountResourceId);
        return externalIdMap;
    }

    default String getBlz(AccountDetailsTO accountDetailsTO) {
        return Iban.valueOf(accountDetailsTO.getIban()).getBankCode();
    }

    default String getAccountNumber(AccountDetailsTO accountDetailsTO) {
        return Iban.valueOf(accountDetailsTO.getIban()).getAccountNumber();
    }

    default BankAccountType getAccounType(String cashAccountType) {
        return fromXS2AType(cashAccountType);
    }
}
