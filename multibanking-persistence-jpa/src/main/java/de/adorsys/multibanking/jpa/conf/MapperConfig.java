package de.adorsys.multibanking.jpa.conf;

import de.adorsys.multibanking.jpa.mapper.JpaEntityMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public JpaEntityMapper entityMapper() {
        return Mappers.getMapper(JpaEntityMapper.class);
    }

}

