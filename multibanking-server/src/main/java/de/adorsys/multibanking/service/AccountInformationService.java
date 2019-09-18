package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.ConsentEntity;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.response.AbstractResponse;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.exception.InvalidConsentException;
import de.adorsys.multibanking.exception.InvalidPinException;
import de.adorsys.multibanking.exception.TransactionAuthorisationRequiredException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.ConsentRepositoryIf;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_CONSENT;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_PIN;

abstract class AccountInformationService {

    @Autowired
    private BankAccessRepositoryIf bankAccessRepository;
    @Autowired
    private ConsentRepositoryIf consentRepository;

    RuntimeException handleMultibankingException(BankAccessEntity bankAccess, MultibankingException e) {
        if (e.getMultibankingError() == INVALID_PIN) {
            return new InvalidPinException(bankAccess.getId());
        } else if (e.getMultibankingError() == INVALID_CONSENT) {
            bankAccess.setConsentId(null);
            bankAccessRepository.save(bankAccess);
            return new InvalidConsentException();
        }
        return e;
    }

    void checkSca(AbstractResponse response, ConsentEntity consentEntity, OnlineBankingService onlineBankingService) {
        Optional.ofNullable(response.getAuthorisationCodeResponse())
            .ifPresent(authorisationCodeResponse -> {
                onlineBankingService.getStrongCustomerAuthorisation().afterExecute(consentEntity.getBankApiConsentData(),
                    authorisationCodeResponse);
                consentRepository.save(consentEntity);

                throw new TransactionAuthorisationRequiredException(authorisationCodeResponse.getUpdateAuthResponse(),
                    consentEntity.getId(), consentEntity.getAuthorisationId());
            });

        consentRepository.save(consentEntity);

    }

}
