package de.adorsys.multibanking.service;

import de.adorsys.multibanking.bg.BankingGatewayAdapter;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.figo.FigoBanking;
import de.adorsys.multibanking.hbci.HbciBanking;
import de.adorsys.multibanking.ing.IngAdapter;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OnlineBankingServiceProducer {

    private final IngAdapter ingAdapter;
    private final BankingGatewayAdapter bankingGatewayAdapter;
    private final HbciBanking hbci4JavaBanking;
    private final FigoBanking figoBanking;
    private final FigoBanking figoBankingAlternative;
    private final BankRepositoryIf bankRepository;
    @Value("${defaultBankApi:HBCI}")
    private String defaultBankApi;

    private BankApi getBankApiForBlz(String blz) {
        BankEntity bankInfoEntity = bankRepository.findByBankCode(blz).orElse(null);

        if (bankInfoEntity != null && bankInfoEntity.getBankApi() != null) {
            return bankInfoEntity.getBankApi();
        }
        return BankApi.valueOf(defaultBankApi);
    }

    public OnlineBankingService getBankingService(String bankCode) {
        BankApi bankApi = getBankApiForBlz(bankCode);

        return getBankingService(bankApi);
    }

    public OnlineBankingService getBankingService(BankApi bankApi) {
        switch (bankApi) {
            case HBCI:
                return hbci4JavaBanking;
            case FIGO:
                return figoBanking;
            case ING:
                return ingAdapter;
            case FIGO_ALTERNATIVE:
                return figoBankingAlternative;
            case XS2A:
                return bankingGatewayAdapter;
        }
        throw new IllegalStateException("unsupported bank api");
    }
}
