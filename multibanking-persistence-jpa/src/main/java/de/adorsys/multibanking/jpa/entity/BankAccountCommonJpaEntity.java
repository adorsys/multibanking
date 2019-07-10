package de.adorsys.multibanking.jpa.entity;

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankAccountType;
import lombok.Data;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
public class BankAccountCommonJpaEntity {

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
