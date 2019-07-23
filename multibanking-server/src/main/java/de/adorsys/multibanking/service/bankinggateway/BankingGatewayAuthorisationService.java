package de.adorsys.multibanking.service.bankinggateway;

import de.adorsys.banking.repository.model.ConsentEntity;
import de.adorsys.banking.repository.spi.ConsentRepository;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.request.CreateConsentRequest;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;

@AllArgsConstructor
@Service
public class BankingGatewayAuthorisationService {

    private final Principal principal;
    private final BankingGatewayMapper bankingGatewayMapper;
    private final ConsentRepository consentRepository;

    public ScaStatus getConsentStatus(String consentId) {
        return consentRepository.getConsentStatus(consentId)
            .map(bankingGatewayMapper::toScaStatus)
            .orElseThrow(() -> new ResourceNotFoundException(ScaStatus.class, consentId));
    }

    public Consent getConsent(String consentId) {
        return consentRepository.getConsent(consentId)
            .map(bankingGatewayMapper::toConsent)
            .orElseThrow(() -> new ResourceNotFoundException(Consent.class, consentId));
    }

    public Consent createAvailableAccountsConsent(OnlineBankingService onlineBankingService,
                                                  BankApiUser bankApiUser,
                                                  BankAccessEntity bankAccessEntity,
                                                  BankEntity bankEntity) {
        //we assume the user belongs to same identity provider
        bankApiUser.setApiUserId(principal.getName());

        CreateConsentRequest createConsentRequest = CreateConsentRequest.builder()
            .bankAccess(bankAccessEntity)
            .bankApiUser(bankApiUser)
            .availableAccountsConsent(true)
            .frequencyPerDay(1)
            .validUntil(LocalDate.now().plusDays(1))
            .recurringIndicator(false)
            .build();

        CreateConsentResponse consentResponse =
            onlineBankingService.createAccountInformationConsent(bankEntity.getBankingUrl(),
                createConsentRequest);

        ConsentEntity consentEntity = saveConsent(bankAccessEntity.getIban(), createConsentRequest, consentResponse);

        return bankingGatewayMapper.toConsent(consentEntity);
    }

    public Consent createDedicatedConsent(OnlineBankingService onlineBankingService,
                                          BankApiUser bankApiUser,
                                          BankAccessEntity bankAccessEntity,
                                          BankAccountEntity bankAccountEntity,
                                          BankEntity bankEntity) {
        //we assume the user belongs to same identity provider
        bankApiUser.setApiUserId(principal.getName());

        CreateConsentRequest createConsentRequest = CreateConsentRequest.builder()
            .bankAccess(bankAccessEntity)
            .accounts(Collections.singletonList(bankAccountEntity))
            .balances(Collections.singletonList(bankAccountEntity))
            .transactions(Collections.singletonList(bankAccountEntity))
            .bankApiUser(bankApiUser)
            .frequencyPerDay(5)
            .validUntil(LocalDate.now().plusYears(1))
            .recurringIndicator(true)
            .build();

        CreateConsentResponse consentResponse =
            onlineBankingService.createAccountInformationConsent(bankEntity.getBankingUrl(),
                createConsentRequest);

        ConsentEntity consentEntity = saveConsent(bankAccountEntity.getIban(), createConsentRequest, consentResponse);

        return bankingGatewayMapper.toConsent(consentEntity);
    }

    private ConsentEntity saveConsent(String iban, CreateConsentRequest createConsentRequest,
                                      CreateConsentResponse consentResponse) {
        ConsentEntity consentEntity = bankingGatewayMapper.toConsentEntity(iban, createConsentRequest,
            consentResponse);

        consentRepository.saveConsent(consentEntity);

        return consentEntity;
    }
}
