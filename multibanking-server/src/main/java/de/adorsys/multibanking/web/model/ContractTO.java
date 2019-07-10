package de.adorsys.multibanking.web.model;

import lombok.Data;
import org.springframework.hateoas.core.Relation;

import java.math.BigDecimal;

@Relation(collectionRelation = "contractList")
@Data
public class ContractTO {

    private String id;
    private String logo;
    private String homepage;
    private String hotline;
    private String email;
    private String mandateReference;
    private CycleTO interval;
    private boolean cancelled;

    private BigDecimal amount;
    private String mainCategory;
    private String subCategory;
    private String specification;
    private String provider;
}
