package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.web.model.ConsentTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", implementationName = "MultibankingConsentMapperImpl")
public interface ConsentMapper {

    @Mapping(target = "authorisationUrl", source = "authorisationUrl")
    ConsentTO toConsentTO(Consent consent, String authorisationUrl);

}
