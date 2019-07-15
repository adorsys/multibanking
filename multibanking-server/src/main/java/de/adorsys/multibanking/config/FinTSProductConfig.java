package de.adorsys.multibanking.config;

import de.adorsys.multibanking.domain.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class FinTSProductConfig {

    @Value("${fints.id:}")
    private String fintsProduct;
    @Value("${fints.version:}")
    private String fintsProductVersion;
    @Value("${info.project.version:null}")
    private String moduleVersion;

    private Product product;

    @PostConstruct
    public void init() {
        if (StringUtils.isEmpty(fintsProduct)) {
            log.warn("missing FinTS product configurations");
            return;
        }
        if (StringUtils.isEmpty(fintsProductVersion)) {
            product = new Product(fintsProduct, moduleVersion);
        } else {
            product = new Product(fintsProduct, fintsProductVersion);
        }
    }

    public Product getProduct() {
        return product;
    }
}
