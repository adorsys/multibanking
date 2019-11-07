package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.BankEntity;
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

    @Mapping(target = "consentId", source = "consentEntity.id")
    @Mapping(target = "authorisationId", source = "consentEntity.authorisationId")
    @Mapping(target = "psuAccountIban", source = "consentEntity.psuAccountIban")
    @Mapping(target = "bankApiConsentData", source = "consentEntity.bankApiConsentData")
    @Mapping(target = "credentials.customerId", source = "updatePsuAuthenticationRequest.psuId")
    @Mapping(target = "credentials.userId", source = "updatePsuAuthenticationRequest.psuCorporateId")
    @Mapping(target = "credentials.pin", source = "updatePsuAuthenticationRequest.password")
    @Mapping(target = "hbciProduct", ignore = true)
    @Mapping(target = "hbciBPD", ignore = true)
    @Mapping(target = "hbciUPD", ignore = true)
    @Mapping(target = "hbciSysId", ignore = true)
    @Mapping(target = "bankApiUser", ignore = true)
    @Mapping(target = "bankAccess", ignore = true)
    @Mapping(target = "authorisationCode", ignore = true)
    UpdatePsuAuthenticationRequest toUpdatePsuAuthenticationRequest(UpdatePsuAuthenticationRequestTO updatePsuAuthenticationRequest, ConsentEntity consentEntity, BankEntity bank);

    UpdateAuthResponseTO toUpdateAuthResponseTO(UpdateAuthResponse updateAuthResponse);

    @Mapping(target = "consentId", source = "consentEntity.id")
    @Mapping(target = "hbciProduct", ignore = true)
    @Mapping(target = "hbciBPD", ignore = true)
    @Mapping(target = "hbciUPD", ignore = true)
    @Mapping(target = "hbciSysId", ignore = true)
    @Mapping(target = "bank", ignore = true)
    @Mapping(target = "bankApiUser", ignore = true)
    @Mapping(target = "bankAccess", ignore = true)
    @Mapping(target = "authorisationCode", ignore = true)
    SelectPsuAuthenticationMethodRequest toSelectPsuAuthenticationMethodRequest(SelectPsuAuthenticationMethodRequestTO selectPsuAuthenticationMethodRequest, ConsentEntity consentEntity);

    @Mapping(target = "consentId", source = "consentEntity.id")
    @Mapping(target = "hbciProduct", ignore = true)
    @Mapping(target = "hbciBPD", ignore = true)
    @Mapping(target = "hbciUPD", ignore = true)
    @Mapping(target = "hbciSysId", ignore = true)
    @Mapping(target = "bank", ignore = true)
    @Mapping(target = "bankApiUser", ignore = true)
    @Mapping(target = "bankAccess", ignore = true)
    @Mapping(target = "authorisationCode", ignore = true)
    TransactionAuthorisationRequest toTransactionAuthorisationRequest(TransactionAuthorisationRequestTO transactionAuthorisationRequest, ConsentEntity consentEntity);

}
