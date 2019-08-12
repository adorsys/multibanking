package de.adorsys.multibanking.hbci;

import de.adorsys.multibanking.domain.request.AuthenticatePsuRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.hbci.model.HBCIConsent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
interface HbciMapper {

    @Mapping(target = "consentId", expression = "java( java.util.UUID.randomUUID().toString() )")
    @Mapping(target = "authorisationId", expression = "java( java.util.UUID.randomUUID().toString() )")
    @Mapping(target = "bankApiConsentData", expression = "java( bankApiConsentData )")
    @Mapping(target = "redirectUrl", ignore = true)
    CreateConsentResponse toCreateConsentResponse(HBCIConsent bankApiConsentData);

    @Mapping(target = "challenge", ignore = true)
    @Mapping(target = "psuMessage", ignore = true)
    @Mapping(target = "scaMethods", source = "tanMethodList")
    @Mapping(target = "scaStatus", source = "status")
    UpdateAuthResponse toUpdateAuthResponse(HBCIConsent hbciConsent);

    @Mapping(target = "login", source = "psuId")
    @Mapping(target = "customerId", source = "psuCustomerId")
    @Mapping(target = "pin", source = "password")
    @Mapping(target = "bankUrl", ignore = true)
    @Mapping(target = "hbciProduct", ignore = true)
    @Mapping(target = "hbciBPD", ignore = true)
    @Mapping(target = "hbciUPD", ignore = true)
    @Mapping(target = "hbciSysId", ignore = true)
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "paymentProduct", ignore = true)
    @Mapping(target = "paymentService", ignore = true)
    @Mapping(target = "bankCode", ignore = true)
    AuthenticatePsuRequest toAuthenticatePsuRequest(UpdatePsuAuthenticationRequest updatePsuAuthentication);
}