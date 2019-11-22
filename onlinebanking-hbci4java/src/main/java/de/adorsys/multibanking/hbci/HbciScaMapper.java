package de.adorsys.multibanking.hbci;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.hbci.model.HbciConsent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
interface HbciScaMapper {

    @Mapping(target = "consentId", expression = "java( java.util.UUID.randomUUID().toString() )")
    @Mapping(target = "authorisationId", expression = "java( java.util.UUID.randomUUID().toString() )")
    @Mapping(target = "bankApiConsentData", expression = "java( bankApiConsentData )")
    @Mapping(target = "redirectUrl", ignore = true)
    @Mapping(target = "redirectId", ignore = true)
    @Mapping(target = "oauthRedirectUrl", ignore = true)
    CreateConsentResponse toCreateConsentResponse(HbciConsent bankApiConsentData);

    @Mapping(target = "challenge", ignore = true)
    @Mapping(target = "psuMessage", ignore = true)
    @Mapping(target = "scaMethods", source = "hbciConsent.tanMethodList")
    @Mapping(target = "scaStatus", source = "hbciConsent.status")
    @Mapping(target = "scaApproach", constant = "EMBEDDED")
    UpdateAuthResponse toUpdateAuthResponse(HbciConsent hbciConsent, BankApi bankApi);

}
