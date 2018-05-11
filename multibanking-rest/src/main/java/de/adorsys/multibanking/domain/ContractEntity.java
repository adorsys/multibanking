package de.adorsys.multibanking.domain;

import java.math.BigDecimal;

import de.adorsys.multibanking.domain.common.IdentityIf;
import domain.Contract;
import lombok.Data;

/**
 * Created by alexg on 07.02.17.
 * @author fpo 2018-04-06 08:03
 */
@Data
public class ContractEntity extends Contract implements IdentityIf {

    private String id;
    private String userId;
    private String accessId;
    private String accountId;
    private BigDecimal amount;
    private String mainCategory;
    private String mainCategoryName;
    private String subCategory;
    private String subCategoryName;
    private String specification;
    private String specificationName;
    private String provider;

}
