package de.adorsys.multibanking.bg;

import de.adorsys.multibanking.banking_gateway_b2c.model.*;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.TanTransportType;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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

    UpdateAuthResponse toUpdateAuthResponseTO(ResourceUpdateAuthResponseTO resourceUpdateAuthResponseTO);

    SelectPsuAuthenticationMethodRequestTO toSelectPsuAuthenticationMethodRequestTO(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod);

    TransactionAuthorisationRequestTO toTransactionAuthorisationRequestTO(TransactionAuthorisationRequest transactionAuthorisation);

    @Mapping(target = "id", source = "authenticationMethodId")
    @Mapping(target = "inputInfo", source = "explanation")
    @Mapping(target = "medium", source = "name")
    TanTransportType toTanTransportType(ScaMethodTO scaMethodTO);
}
