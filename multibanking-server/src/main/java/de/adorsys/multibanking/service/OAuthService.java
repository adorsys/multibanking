package de.adorsys.multibanking.service;

import de.adorsys.multibanking.bg.BankingGatewayAdapter;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.ConsentEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.ConsentRepositoryIf;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OAuthService {
    private final ConsentRepositoryIf consentRepository;
    private final OnlineBankingServiceProducer bankingServiceProducer;

    public void submitAuthCode(String consentId, String authorisationId,  String oauthCode) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
                .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));

        BankingGatewayAdapter bankingGatewayAdapter = (BankingGatewayAdapter) bankingServiceProducer.getBankingService(BankApi.XS2A); // oauth only for xs2a
        Object bgSessionData = bankingGatewayAdapter.submitOAuthAuthorizationCode(authorisationId, oauthCode);

        internalConsent.setBankApiConsentData(bgSessionData);
        consentRepository.save(internalConsent);
    }
}
