package de.adorsys.multibanking.service;

import de.adorsys.multibanking.bg.BankingGatewayAdapter;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.figo.FigoBanking;
import de.adorsys.multibanking.finapi.FinapiBanking;
import de.adorsys.multibanking.hbci.Hbci4JavaBanking;
import de.adorsys.multibanking.mock.MockBanking;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OnlineBankingServiceProducer {

    private final BankRepositoryIf bankRepository;

    @Value("${defaultBankApi:HBCI}")
    private String defaultBankApi;
    @Value("${bankinggateway.b2c.url}")
    private String bankingGatewayBaseUrl;
    @Value("${bankinggateway.adapter.url}")
    private String bankingAdapterBaseUrl;

    @Getter(lazy = true)
    private final BankingGatewayAdapter xs2ABanking = new BankingGatewayAdapter(bankingGatewayBaseUrl, bankingAdapterBaseUrl);
    private Hbci4JavaBanking hbci4JavaBanking = new Hbci4JavaBanking(true);
    private FigoBanking figoBanking = new FigoBanking(BankApi.FIGO);
    private FigoBanking figoBankingAlternative = new FigoBanking(BankApi.FIGO_ALTERNATIVE);
    private FinapiBanking finapiBanking = new FinapiBanking();
    private MockBanking mockBanking = new MockBanking();

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
            case FIGO_ALTERNATIVE:
                return figoBankingAlternative;
            case FINAPI:
                return finapiBanking;
            case XS2A:
            case BANKING_GATEWAY:
                return getXs2ABanking();
            case MOCK:
                return mockBanking;
            case SCREEN_SCRAPPING:
                break;
        }
        throw new IllegalStateException("unsupported bank api");
    }
}
