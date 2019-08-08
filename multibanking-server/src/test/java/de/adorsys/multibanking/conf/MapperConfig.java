package de.adorsys.multibanking.conf;

import de.adorsys.multibanking.web.mapper.*;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public AnalyticsMapper analyticsMapper() {
        return Mappers.getMapper(AnalyticsMapper.class);
    }

    @Bean
    public BalancesMapper balancesMapper() {
        return Mappers.getMapper(BalancesMapper.class);
    }

    @Bean
    public BankAccessMapper bankAccessMapper() {
        return Mappers.getMapper(BankAccessMapper.class);
    }

    @Bean
    public BankAccountMapper bankAccountMapper() {
        return Mappers.getMapper(BankAccountMapper.class);
    }

    @Bean
    public BankApiMapper bankApiMapper() {
        return Mappers.getMapper(BankApiMapper.class);
    }

    @Bean
    public BankMapper bankMapper() {
        return Mappers.getMapper(BankMapper.class);
    }

    @Bean
    public BookingMapper bookingMapper() {
        return Mappers.getMapper(BookingMapper.class);
    }

    @Bean
    public ContractMapper contractMapper() {
        return Mappers.getMapper(ContractMapper.class);
    }

    @Bean
    public RuleMapper ruleMapper() {
        return Mappers.getMapper(RuleMapper.class);
    }

    @Bean
    public ConsentMapper consentMapper() {
        return new MultibankingConsentMapperImpl();
    }
}
