package de.adorsys.multibanking.service;

import de.adorsys.multibanking.bg.BankingGatewayAdapter;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.figo.FigoBanking;
import de.adorsys.multibanking.finapi.FinapiBanking;
import de.adorsys.multibanking.hbci.Hbci4JavaBanking;
import de.adorsys.multibanking.ing.IngAdapter;
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

    @Value("${ing.url}")
    private String ingBaseUrl;
    @Value("${pkcs12.keyStore.url}")
    private String keyStoreUrl;
    @Value("${pkcs12.keyStore.password}")
    private String keyStorePassword;
    @Value("${ing.qwac.alias}")
    private String ingQwacAlias;
    @Value("${ing.qseal.alias}")
    private String ingQsealAlias;

    @Getter(lazy = true)
    private final BankingGatewayAdapter xs2ABanking = new BankingGatewayAdapter(bankingGatewayBaseUrl,
        bankingAdapterBaseUrl);
    private Hbci4JavaBanking hbci4JavaBanking = new Hbci4JavaBanking(true);
    private FigoBanking figoBanking = new FigoBanking(BankApi.FIGO);
    private FigoBanking figoBankingAlternative = new FigoBanking(BankApi.FIGO_ALTERNATIVE);
    private FinapiBanking finapiBanking = new FinapiBanking();
    private IngAdapter ingAdapter = new IngAdapter(ingBaseUrl, keyStoreUrl, keyStorePassword, ingQwacAlias, ingQsealAlias);

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
            case FINAPI:
                return finapiBanking;
            case XS2A:
                return getXs2ABanking();
            case SCREEN_SCRAPPING:
                break;
        }
        throw new IllegalStateException("unsupported bank api");
    }
}
