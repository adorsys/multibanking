package de.adorsys.multibanking.hbci;

import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.request.AuthenticatePsuRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.hbci.domain.HBCIConsentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
interface HbciMapper {

    HBCIConsentEntity toConsentEntity(Consent consentResponse);

    @Mapping(target = "consentId", source = "id")
    Consent toConsent(HBCIConsentEntity consentEntity);

    @Mapping(target = "scaMethods", source = "tanMethodList")
    @Mapping(target = "scaStatus", source = "status")
    UpdateAuthResponse toUpdateAuthRepsonse(HBCIConsentEntity entity);

    @Mapping(target = "login", source = "psuId")
    @Mapping(target = "customerId", source = "psuCustomerId")
    @Mapping(target = "pin", source = "password")
    AuthenticatePsuRequest toAuthenticatePsuRequest(UpdatePsuAuthenticationRequest updatePsuAuthentication);

    @Mapping(target = "consentId", source = "id")
    CreateConsentResponse toCreateConsentResponse(HBCIConsentEntity entity);
}
