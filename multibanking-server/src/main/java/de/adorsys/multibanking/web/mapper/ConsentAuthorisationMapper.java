package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.web.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConsentAuthorisationMapper {

    UpdateAuthResponseTO toUpdateAuthResponseTO(UpdateAuthResponse updateAuthResponse);

    UpdatePsuAuthenticationRequest toUpdatePsuAuthenticationRequest(UpdatePsuAuthenticationRequestTO updatePsuAuthenticationRequest, String consentId, String authorisationId);

    SelectPsuAuthenticationMethodRequest toSelectPsuAuthenticationMethodRequest(SelectPsuAuthenticationMethodRequestTO selectPsuAuthenticationMethodRequest, String consentId, String authorisationId);

    TransactionAuthorisationRequest toTransactionAuthorisationRequest(TransactionAuthorisationRequestTO transactionAuthorisationRequest, String consentId, String authorisationId);

}
