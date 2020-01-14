package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.hateoas.core.Relation;

@Schema(name = "Bank")
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
