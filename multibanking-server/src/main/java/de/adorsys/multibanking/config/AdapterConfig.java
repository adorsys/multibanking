package de.adorsys.multibanking.config;

import de.adorsys.multibanking.bg.BankingGatewayAdapter;
import de.adorsys.multibanking.hbci.HbciBanking;
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
    @Value("${fints.updCacheExpirationMs:0}")
    private long fintsUpdCacheExpirationMs;
    @Value("${dump.download.files:false}")
    private boolean dumpDownloadFiles;
    @Value("${fints.bpdCacheExpirationMs:86400000}") //one day
    private long fintsBpdCacheExpirationMs;

    private BankingGatewayAdapter bankingGatewayAdapter;
    private HbciBanking hbci4JavaBanking;

    @PostConstruct
    public void postConstruct() {
        bankingGatewayAdapter = new BankingGatewayAdapter(bankingGatewayBaseUrl,
            bankingAdapterBaseUrl, dumpDownloadFiles);

        if (StringUtils.isEmpty(fintsProduct)) {
            log.warn("missing FinTS product configuration");
            hbci4JavaBanking = new HbciBanking(null, fintsSysIdCacheExpirationMs, fintsUpdCacheExpirationMs, fintsBpdCacheExpirationMs);
        } else {
            hbci4JavaBanking = new HbciBanking(new HBCIProduct(fintsProduct, fintsProductVersion), fintsSysIdCacheExpirationMs, fintsUpdCacheExpirationMs, fintsBpdCacheExpirationMs);
        }
    }

    @Bean
    public BankingGatewayAdapter bankingGatewayAdapter() {
        return bankingGatewayAdapter;
    }

    @Bean
    public HbciBanking hbci4JavaBanking() {
        return hbci4JavaBanking;
    }

}
