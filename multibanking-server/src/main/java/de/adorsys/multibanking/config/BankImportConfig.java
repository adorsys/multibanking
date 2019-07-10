package de.adorsys.multibanking.config;

import de.adorsys.multibanking.service.BankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class BankImportConfig {

    private final BankService bankService;
    @Value("${bank.import.file:}")
    private String bankImportFile;

    @PostConstruct
    public void init() {
        if (StringUtils.isEmpty(bankImportFile)) {
            return;
        }

        File importFile = new File(bankImportFile);

        if (!importFile.exists()) {
            log.error("File for bank import does not exist: {}", importFile.getAbsolutePath());
            return;
        }

        bankService.importBanks(importFile);
    }

}
