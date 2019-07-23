package de.adorsys.multibanking.service.bankinggateway;

import de.adorsys.banking.repository.model.ConsentEntity;
import de.adorsys.banking.repository.model.ConsentStatusEntity;
import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.ScaStatus;
import de.adorsys.multibanking.domain.request.CreateConsentRequest;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankingGatewayMapper {

    @Mapping(target = "access.accounts", source = "createConsentRequest.accounts")
    @Mapping(target = "access.balances", source = "createConsentRequest.balances")
    @Mapping(target = "access.transactions", source = "createConsentRequest.transactions")
    @Mapping(target = "consentStatus", source = "consentResponse.consentStatus")
    @Mapping(target = "psuAccount", source = "iban")
    @Mapping(target = "validUntil", source = "consentResponse.validUntil")
    ConsentEntity toConsentEntity(String iban, CreateConsentRequest createConsentRequest,
                                  CreateConsentResponse consentResponse);

    @Mapping(target = "accounts", source = "access.accounts")
    @Mapping(target = "balances", source = "access.balances")
    @Mapping(target = "transactions", source = "access.transactions")
    @Mapping(target = "scaStatus", source = "consentStatus")
    Consent toConsent(ConsentEntity consentEntity);

    ScaStatus toScaStatus(ConsentStatusEntity consentStatusEntity);
}
