package de.adorsys.multibanking.config;

import de.adorsys.xs2a.gateway.service.PaymentInitiationService;
import de.adorsys.xs2a.gateway.service.ais.AccountInformationService;
import de.adorsys.xs2a.gateway.service.impl.AccountInformationServiceImpl;
import de.adorsys.xs2a.gateway.service.impl.PaymentInitiationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BankingGatewayAdapterConfig {

    @Bean
    AccountInformationService accountInformationService() {
        return new AccountInformationServiceImpl();
    }

    @Bean
    PaymentInitiationService paymentInitiationService() {
        return new PaymentInitiationServiceImpl();
    }
}


