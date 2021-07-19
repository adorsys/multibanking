package de.adorsys.multibanking.config;

import de.adorsys.multibanking.bg.BankingGatewayAdapter;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.figo.FigoBanking;
import de.adorsys.multibanking.hbci.HbciBanking;
import de.adorsys.multibanking.ing.IngAdapter;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.manager.HBCIProduct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class AdapterConfig {

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
    @Value("${fints.id:}")
    private String fintsProduct;
    @Value("${fints.version:}")
    private String fintsProductVersion;
    @Value("${fints.sysIdCacheExpirationMs:0}")
    private long fintsSysIdCacheExpirationMs;
    @Value("${fints.sysUpdCacheExpirationMs:0}")
    private long fintsUpdCacheExpirationMs;
    @Value("${dump.download.files:false}")
    private boolean dumpDownloadFiles;

    private IngAdapter ingAdapter;
    private BankingGatewayAdapter bankingGatewayAdapter;
    private HbciBanking hbci4JavaBanking;
    private FigoBanking figoBanking = new FigoBanking(BankApi.FIGO);
    private FigoBanking figoBankingAlternative = new FigoBanking(BankApi.FIGO_ALTERNATIVE);

    @PostConstruct
    public void postConstruct() {
        ingAdapter = new IngAdapter(ingBaseUrl, keyStoreUrl, keyStorePassword, ingQwacAlias,
            ingQsealAlias);
        bankingGatewayAdapter = new BankingGatewayAdapter(bankingGatewayBaseUrl,
            bankingAdapterBaseUrl, dumpDownloadFiles);

        if (StringUtils.isEmpty(fintsProduct)) {
            log.warn("missing FinTS product configuration");
            hbci4JavaBanking = new HbciBanking(null, fintsSysIdCacheExpirationMs, fintsUpdCacheExpirationMs);
        } else {
            hbci4JavaBanking = new HbciBanking(new HBCIProduct(fintsProduct, fintsProductVersion), fintsSysIdCacheExpirationMs, fintsUpdCacheExpirationMs);
        }
    }

    @Bean
    public IngAdapter ingAdapter() {
        return ingAdapter;
    }

    @Bean
    public BankingGatewayAdapter bankingGatewayAdapter() {
        return bankingGatewayAdapter;
    }

    @Bean
    public HbciBanking hbci4JavaBanking() {
        return hbci4JavaBanking;
    }

    @Bean
    public FigoBanking figoBanking() {
        return figoBanking;
    }

    @Bean
    public FigoBanking figoBankingAlternative() {
        return figoBankingAlternative;
    }
}
