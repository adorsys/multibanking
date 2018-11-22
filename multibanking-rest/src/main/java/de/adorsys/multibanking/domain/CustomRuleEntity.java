package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.domain.common.IdentityIf;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by alexg on 05.09.17.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CustomRuleEntity extends RuleEntity implements IdentityIf {
    private String userId;
    private boolean userRule;
    private boolean released;

}
