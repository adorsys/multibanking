package de.adorsys.multibanking.service.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.manager.HBCIUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.adorsys.multibanking.domain.BankEntity;
import domain.BankApi;
import domain.BankLoginCredentialInfo;
import domain.BankLoginSettings;

public class ImportBanks {

    static HashMap<String, List<String>> instituteMap = new HashMap<>();
    static HashMap<String, List<String>> bank_catalogueMap = new HashMap<>();
    static HashMap<String, List<String>> mapping_sucheMap = new HashMap<>();
    static HashMap<String, String> blz_propertiesMap = new HashMap<>();

    public static void main(String... args) throws Exception {
        read_fints_institute_csv();
        read_bank_catalogue_csv();
        read_mapping_suche_csv();
        read_blz_properties();
        merge();
    }

    private static void read_fints_institute_csv() throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(HBCIUtils.class.getClassLoader().getResource("bank-import/fints_institute.csv").openStream()))) {
            reader.readLine(); //skip first line
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";");
                if (values.length > 1) {
                    instituteMap.put(values[1], new ArrayList<>(Arrays.asList(values)));
                }
            }
        }
    }

    private static void read_bank_catalogue_csv() throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(HBCIUtils.class.getClassLoader().getResource("bank-import/bank-catalogue.csv").openStream()))) {
            reader.readLine(); //skip first line
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";");
                if (values.length > 1) {
                    bank_catalogueMap.put(values[0], new ArrayList<>(Arrays.asList(values)));
                }
            }
        }
    }

    private static void read_mapping_suche_csv() throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(HBCIUtils.class.getClassLoader().getResource("bank-import/mapping-suche.csv").openStream()))) {
            reader.readLine(); //skip first line
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";");
                if (values.length == 6) {
                    mapping_sucheMap.put(values[3], new ArrayList<>(Arrays.asList(values)));
                }
            }
        }
    }

    private static void read_blz_properties() throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(HBCIUtils.class.getClassLoader().getResource("blz.properties").openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("=");
                blz_propertiesMap.put(values[0], values[1]);
            }
        }

    }

    private static void merge() throws IOException {
        HashMap<String, BankEntity> mergedMap = new HashMap<>();

        instituteMap.forEach((s, strings) -> {
            BankEntity bankEntity = new BankEntity();
            bankEntity.setBankCode(strings.get(1));
            if (strings.size() > 2) {
                bankEntity.setName(strings.get(2));
                if (strings.size() > 3 && StringUtils.isNotBlank(strings.get(3))) {
                    bankEntity.setName(bankEntity.getName()+" "+strings.get(3));
                }
            }
            mergedMap.put(s, bankEntity);
        });

        bank_catalogueMap.forEach((s, strings) -> {
            BankEntity bankEntity = mergedMap.get(strings.get(0));
            if (bankEntity == null) {
                bankEntity = new BankEntity();
                bankEntity.setBankCode(strings.get(0));
                mergedMap.put(s, bankEntity);
            }

            bankEntity.setName(strings.get(1));

            BankLoginSettings loginSettings = new BankLoginSettings();
            bankEntity.setLoginSettings(loginSettings);
            loginSettings.setAdvice(strings.get(2));

            List<BankLoginCredentialInfo> bankLoginCredentials = new ArrayList<>();
            loginSettings.setCredentials(bankLoginCredentials);

            BankLoginCredentialInfo bankLoginCredential = new BankLoginCredentialInfo();
            if (strings.size() > 4) {
                bankLoginCredential.setLabel(strings.get(3));
                bankLoginCredential.setMasked(Boolean.parseBoolean(strings.get(4)));
                bankLoginCredentials.add(bankLoginCredential);

                bankLoginCredential = new BankLoginCredentialInfo();
                bankLoginCredential.setLabel(strings.get(5));
                bankLoginCredential.setMasked(Boolean.parseBoolean(strings.get(6)));
                bankLoginCredentials.add(bankLoginCredential);

                if (strings.size() > 7) {
                    bankLoginCredential = new BankLoginCredentialInfo();
                    bankLoginCredential.setLabel(strings.get(7));
                    bankLoginCredential.setMasked(Boolean.parseBoolean(strings.get(8)));
                    bankLoginCredentials.add(bankLoginCredential);
                }
            }
        });

        mapping_sucheMap.forEach((s, strings) -> {
            BankEntity bankEntity = mergedMap.get(strings.get(3));
            if (bankEntity == null) {
                bankEntity = new BankEntity();
                bankEntity.setBankCode(strings.get(3));
                mergedMap.put(s, bankEntity);
            }

            if (StringUtils.isBlank(bankEntity.getName())) {
                bankEntity.setName(strings.get(4));
            }

            bankEntity.setSearchIndex(new ArrayList<>());
            bankEntity.getSearchIndex().add(strings.get(3).toLowerCase());
            bankEntity.getSearchIndex().add(strings.get(4).toLowerCase());

            bankEntity.setBlzHbci(strings.get(0));
        });

        try (InputStream inputStream = HBCIUtils.class.getClassLoader().getResource("blz.properties").openStream()) {
            HBCIUtils.refreshBLZList(inputStream);
            HBCIUtils.banks.values().forEach(bankInfo -> {
                BankEntity bankEntity = mergedMap.get(bankInfo.getBlz());
                if (bankEntity == null) {
                    bankEntity = new BankEntity();
                    bankEntity.setBankCode(bankInfo.getBlz());
                    mergedMap.put(bankInfo.getBlz(), bankEntity);
                }

                if (StringUtils.isBlank(bankEntity.getName())) {
                    bankEntity.setName(parseToUT8(bankInfo.getName()));
                }

                bankEntity.setBic(bankInfo.getBic());
                if (bankInfo.getPinTanVersion() != null) {
                    bankEntity.setBankApi(BankApi.HBCI);
                } else {
                    bankEntity.setBankApi(BankApi.FIGO);
                }
            });
        }

        mergedMap.values().forEach(bankEntity -> {
            if (bankEntity.getSearchIndex() == null || bankEntity.getSearchIndex().size() == 0) {
                bankEntity.setSearchIndex(new ArrayList<>());
                if (bankEntity.getName() != null) {
                    bankEntity.getSearchIndex().add(bankEntity.getName().toLowerCase());
                }
            }

            if (!bankEntity.getSearchIndex().contains(bankEntity.getBankCode())) {
                bankEntity.getSearchIndex().add(bankEntity.getBankCode());
            }
        });


        final YAMLFactory ymlFactory = new YAMLFactory();
        ObjectMapper objectMapper = new ObjectMapper(ymlFactory);

        try (InputStream inputStream = HBCIUtils.class.getClassLoader().getResource("bank-import/custom.yml").openStream()) {
            List<BankEntity> banks = objectMapper.readValue(inputStream, new TypeReference<List<BankEntity>>() {
            });
            banks.forEach(bankEntity -> {
                mergedMap.put(bankEntity.getBankCode(), bankEntity);
            });

            List<BankEntity> bankEntities = mergedMap.values()
                    .stream()
                    .sorted((e1, e2) -> e1.getBankCode().compareTo(e2.getBankCode()))
                    .collect(Collectors.toList());

            objectMapper.writeValue(new File("/Users/alexg/Downloads/bank-catalogue.yml"), bankEntities);
        }


    }

    // fix the encoding issue
    private static String parseToUT8(String source) {
        if (StringUtils.isNotBlank(source)) {
            return new String(source.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        } else {
            return source;
        }
    }

}
