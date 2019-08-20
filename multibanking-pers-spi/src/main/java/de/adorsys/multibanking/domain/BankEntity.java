package de.adorsys.multibanking.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class BankEntity extends Bank {

    private String id;
    private String bankApiBankCode;
    private List<String> searchIndex;

}
