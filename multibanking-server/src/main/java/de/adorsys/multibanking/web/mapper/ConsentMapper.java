package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.web.model.ConsentTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConsentMapper {

    ConsentTO toConsentTO(Consent consent);

}
