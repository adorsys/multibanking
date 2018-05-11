package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.domain.common.IdentityIf;
import lombok.Data;

@Data
public class CustomCategoryEntity extends CategoryEntity implements IdentityIf {
    private String userId;
    private boolean released;
}
