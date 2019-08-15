package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.Credentials;
import de.adorsys.multibanking.web.model.CredentialsTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CredentialsMapper {

    Credentials toCredentials(CredentialsTO credentialsTO);


}
