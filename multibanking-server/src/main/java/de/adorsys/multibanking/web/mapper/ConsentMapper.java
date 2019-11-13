package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.ConsentEntity;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.web.model.ConsentTO;
import de.adorsys.multibanking.web.model.CreateConsentResponseTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", implementationName = "MultibankingConsentMapperImpl")
public interface ConsentMapper {

    @Mapping(target = "id", source = "consentId")
    @Mapping(target = "tppRedirectUri", ignore = true)
    ConsentTO toConsentTO(Consent consent);

    @Mapping(target = "consentId", source = "id")
    Consent toConsent(ConsentTO consent);

    @Mapping(target = "consentId", source = "id")
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "balances", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "recurringIndicator", ignore = true)
    @Mapping(target = "validUntil", ignore = true)
    @Mapping(target = "frequencyPerDay", ignore = true)
    Consent toConsent(ConsentEntity consent);

    CreateConsentResponseTO toCreateConsentResponseTO(CreateConsentResponse createConsentResponse);

    @Mapping(target = "id", source = "createConsentResponse.consentId")
    @Mapping(target = "authorisationId", source = "createConsentResponse.authorisationId")
    @Mapping(target = "bankApiConsentData", source = "createConsentResponse.bankApiConsentData")
    ConsentEntity toConsentEntity(CreateConsentResponse createConsentResponse, String redirectId, String psuAccountIban, BankApi bankApi);

}
