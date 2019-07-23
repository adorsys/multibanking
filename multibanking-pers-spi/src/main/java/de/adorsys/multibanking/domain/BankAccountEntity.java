package de.adorsys.multibanking.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(value = {"externalIdMap"}, allowSetters = true)
public class BankAccountEntity extends BankAccount {

    private String id;
    private String bankAccessId;
    private String userId;
    private String psd2ConsentId;
    private String psd2ConsentAuthorisationId;

}
