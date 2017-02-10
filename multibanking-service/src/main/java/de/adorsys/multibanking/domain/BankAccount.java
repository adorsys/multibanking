package de.adorsys.multibanking.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kapott.hbci.structures.Konto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by alexg on 07.02.17.
 */
@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {

    @Id
    private String id;
    @Indexed
    private String bankAccessId;
    private BankAccountBalance bankAccountBalance;
    private String countryHbciAccount;
    private String blzHbciAccount;
    private String numberHbciAccount;
    private String typeHbciAccount;
    private String currencyHbciAccount;
    private String nameHbciAccount;
    private String bicHbciAccount;
    private String ibanHbciAccount;

    public BankAccount(Konto konto) {
        this.numberHbciAccount = konto.number;
        this.bicHbciAccount = konto.bic;
        this.blzHbciAccount = konto.blz;
        this.countryHbciAccount = konto.country;
        this.currencyHbciAccount = konto.curr;
        this.ibanHbciAccount = konto.iban;
        this.nameHbciAccount = (konto.name + " " + (konto.name2 != null ? konto.name2 : "")).trim();
        this.typeHbciAccount = konto.type;
    }

}
