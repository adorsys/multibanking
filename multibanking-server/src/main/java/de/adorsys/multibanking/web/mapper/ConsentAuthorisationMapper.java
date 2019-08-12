package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.ConsentEntity;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.web.model.SelectPsuAuthenticationMethodRequestTO;
import de.adorsys.multibanking.web.model.TransactionAuthorisationRequestTO;
import de.adorsys.multibanking.web.model.UpdateAuthResponseTO;
import de.adorsys.multibanking.web.model.UpdatePsuAuthenticationRequestTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConsentAuthorisationMapper {

    UpdateAuthResponseTO toUpdateAuthResponseTO(UpdateAuthResponse updateAuthResponse);

    @Mapping(target = "consentId", source = "consentEntity.id")
    UpdatePsuAuthenticationRequest toUpdatePsuAuthenticationRequest(UpdatePsuAuthenticationRequestTO updatePsuAuthenticationRequest, ConsentEntity consentEntity);

    @Mapping(target = "consentId", source = "consentEntity.id")
    SelectPsuAuthenticationMethodRequest toSelectPsuAuthenticationMethodRequest(SelectPsuAuthenticationMethodRequestTO selectPsuAuthenticationMethodRequest, ConsentEntity consentEntity);

    @Mapping(target = "consentId", source = "consentEntity.id")
    TransactionAuthorisationRequest toTransactionAuthorisationRequest(TransactionAuthorisationRequestTO transactionAuthorisationRequest, ConsentEntity consentEntity);

}
