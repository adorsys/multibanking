package de.adorsys.multibanking.hbci;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.exception.Message;
import de.adorsys.multibanking.domain.request.AuthenticatePsuRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.hbci.model.HBCIConsent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
interface HbciMapper {

    @Mapping(target = "consentId", expression = "java( java.util.UUID.randomUUID().toString() )")
    @Mapping(target = "authorisationId", expression = "java( java.util.UUID.randomUUID().toString() )")
    @Mapping(target = "bankApiConsentData", expression = "java( bankApiConsentData )")
    @Mapping(target = "redirectUrl", ignore = true)
    CreateConsentResponse toCreateConsentResponse(HBCIConsent bankApiConsentData);

    @Mapping(target = "challenge", ignore = true)
    @Mapping(target = "psuMessage", ignore = true)
    @Mapping(target = "scaMethods", source = "hbciConsent.tanMethodList")
    @Mapping(target = "scaStatus", source = "hbciConsent.status")
    @Mapping(target = "scaApproach", constant = "EMBEDDED")
    UpdateAuthResponse toUpdateAuthResponse(HBCIConsent hbciConsent, BankApi bankApi);

    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "paymentProduct", ignore = true)
    @Mapping(target = "paymentService", ignore = true)
    AuthenticatePsuRequest toAuthenticatePsuRequest(UpdatePsuAuthenticationRequest updatePsuAuthentication);

    default List<Message> toMessages(List<String> messages) {
        return messages.stream()
            .map(messageString -> Message.builder().renderedMessage(messageString).build())
            .collect(Collectors.toList());

    }
}
