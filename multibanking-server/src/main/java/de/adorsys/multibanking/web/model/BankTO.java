package de.adorsys.multibanking.web.model;

import lombok.Data;
import org.springframework.hateoas.core.Relation;

@Relation(collectionRelation = "bankList")
@Data
public class BankTO {

    private String bankingUrl;
    private String bankCode;
    private String bic;
    private String name;
    private BankLoginSettingsTO loginSettings;
    private BankApiTO bankApi;

}
