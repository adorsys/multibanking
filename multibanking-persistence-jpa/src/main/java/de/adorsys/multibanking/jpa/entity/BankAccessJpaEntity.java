package de.adorsys.multibanking.jpa.entity;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.TanTransportType;
import lombok.Data;

import javax.persistence.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity(name="bank_access")
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
    private ConsentEntity allAcountsConsent;
    private String bankLogin;
    private String bankLogin2;
    private String bankCode;
    private String iban;
    @ElementCollection
    @CollectionTable(name="tan_method")
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name="bank_api")
    @Column(name="tan_method")
    private Map<BankApi, List<TanTransportType>> tanTransportTypes =  new EnumMap<>(BankApi.class);
    private String hbciPassportState;
//    @Embedded
//    private Map<BankApi, String> externalIdMap = new EnumMap<>(BankApi.class);

}
