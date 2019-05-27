package de.adorsys.multibanking.jpa.entity;

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankAccountType;
import de.adorsys.multibanking.domain.BankApi;
import lombok.Data;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@Entity(name="bank_account")
@Data
public class BankAccountJpaEntity {

    @Id
    //@GeneratedValue
    private Long id;
    private String bankAccessId;
    private String userId;

    @Embedded
    private Map<BankApi, String> externalIdMap = new EnumMap<>(BankApi.class);
    @Embedded
    private ConsentEntity dedicatedConsent;
    @Embedded
    private BalancesReportEntity balances;
    private String owner;
    private String country;
    private String blz;
    private String bankName;
    private String accountNumber;
    private BankAccountType type;
    private String currency;
    private String name;
    private String bic;
    private String iban;
    private BankAccount.SyncStatus syncStatus;
    private LocalDateTime lastSync;

}
