package de.adorsys.multibanking.domain;

import java.util.HashMap;
import java.util.Map;

import de.adorsys.multibanking.domain.common.IdentityIf;
import lombok.Data;

@Data
public class CategoryEntity implements IdentityIf {
    private String id;
    private String name;
    private String parent;
    // Map of language key and translation.
    private Map<String, String> translations = new HashMap<>();
}
