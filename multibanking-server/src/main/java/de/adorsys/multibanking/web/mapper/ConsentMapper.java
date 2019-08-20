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

    @Mapping(target = "psuAccountIban", ignore = true)
    @Mapping(target = "tppRedirectUri", ignore = true)
    ConsentTO toConsentTO(Consent consent);

    @Mapping(target = "consentId", ignore = true)
    Consent toConsent(ConsentTO consent);

    CreateConsentResponseTO toCreateConsentResponseTO(CreateConsentResponse createConsentResponseconsent);

    @Mapping(target = "id", source = "createConsentResponse.consentId")
    @Mapping(target = "authorisationId", source = "createConsentResponse.authorisationId")
    @Mapping(target = "bankApiConsentData", source = "createConsentResponse.bankApiConsentData")
    ConsentEntity toConsentEntity(CreateConsentResponse createConsentResponse, String psuAccountIban, BankApi bankApi);

}
