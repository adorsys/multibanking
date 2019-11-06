package de.adorsys.multibanking.config;

import de.adorsys.multibanking.bg.BankingGatewayAdapter;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.figo.FigoBanking;
import de.adorsys.multibanking.finapi.FinapiBanking;
import de.adorsys.multibanking.hbci.Hbci4JavaBanking;
import de.adorsys.multibanking.ing.IngAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

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

    private IngAdapter ingAdapter;
    private BankingGatewayAdapter bankingGatewayAdapter;
    private Hbci4JavaBanking hbci4JavaBanking = new Hbci4JavaBanking(true);
    private FigoBanking figoBanking = new FigoBanking(BankApi.FIGO);
    private FigoBanking figoBankingAlternative = new FigoBanking(BankApi.FIGO_ALTERNATIVE);
    private FinapiBanking finapiBanking = new FinapiBanking();

    @PostConstruct
    public void postConstruct() {
        ingAdapter = new IngAdapter(ingBaseUrl, keyStoreUrl, keyStorePassword, ingQwacAlias,
            ingQsealAlias);
        bankingGatewayAdapter = new BankingGatewayAdapter(bankingGatewayBaseUrl,
            bankingAdapterBaseUrl);
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
    public Hbci4JavaBanking hbci4JavaBanking() {
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

    @Bean
    public FinapiBanking finapiBanking() {
        return finapiBanking;
    }
}
