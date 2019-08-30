package de.adorsys.multibanking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@AllArgsConstructor
@Slf4j
@Service
public class BankService {

    private final BankRepositoryIf bankRepository;

    public void importBanks(MultipartFile file) {
        try {
            importBanks(file.getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void importBanks(File file) {
        try {
            importBanks(new FileInputStream(file));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void importBanks(InputStream inputStream) throws IOException {
        log.info("start import banks file");

        final YAMLFactory ymlFactory = new YAMLFactory();
        ObjectMapper objectMapper = new ObjectMapper(ymlFactory)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<BankEntity> banks = objectMapper.readValue(inputStream, new TypeReference<List<BankEntity>>() {
        });

        bankRepository.deleteAll();
        bankRepository.save(banks);

        log.info("successfully imported [{}]", banks.size());
    }

    public BankEntity findBank(String bankCode) {
        return bankRepository.findByBankCode(bankCode)
            .orElseThrow(() -> new ResourceNotFoundException(BankEntity.class, bankCode));
    }

    public List<BankEntity> search(String terms) {
        return bankRepository.search(terms);
    }

}
