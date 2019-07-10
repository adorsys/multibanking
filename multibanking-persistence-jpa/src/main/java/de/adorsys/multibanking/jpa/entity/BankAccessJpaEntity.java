package de.adorsys.multibanking.jpa.entity;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.jpa.converter.TanTransportTypesMapConverter;
import lombok.Data;

import javax.persistence.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity(name = "bank_access")
public class BankAccessJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String userId;
    private String pin;
    private String pin2;
    private boolean temporary;
    private boolean storePin;
    private boolean storeBookings;
    private boolean categorizeBookings;
    private boolean storeAnalytics;
    private boolean storeAnonymizedBookings;
    private boolean provideDataForMachineLearning;

    private String bankName;
    @Embedded
    private ConsentJpaEntity allAcountsConsent;
    private String bankLogin;
    private String bankLogin2;
    private String bankCode;
    private String iban;
    @Column(length = 10000)
    private String hbciPassportState;
    @Column(length = 10000)
    @Convert(converter = TanTransportTypesMapConverter.class)
    private HashMap<String, List<TanTransportTypeJpaEntity>> tanTransportTypes;
    @ElementCollection
    @CollectionTable(name = "bankaccess_external_id")
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "bank_api")
    @Column(name = "external_id")
    private Map<BankApi, String> externalIdMap = new EnumMap<>(BankApi.class);

}
