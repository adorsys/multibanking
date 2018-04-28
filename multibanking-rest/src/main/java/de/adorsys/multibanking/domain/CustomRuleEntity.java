package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.domain.common.IdentityIf;
import lombok.Data;

/**
 * Created by alexg on 05.09.17.
 */
@Data
public class CustomRuleEntity extends RuleEntity implements IdentityIf {
    private String userId;
    private boolean userRule;
    private boolean released;

}
