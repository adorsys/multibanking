package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.bg.domain.Consent;
import de.adorsys.multibanking.web.model.ConsentTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", implementationName = "MultibankingConsentMapperImpl")
public interface ConsentMapper {

    @Mapping(target = "psuAccountIban", ignore = true)
    ConsentTO toConsentTO(Consent consent);

    @Mapping(target = "authUrl", ignore = true)
    Consent toConsent(ConsentTO consent);

}
