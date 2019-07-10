package de.adorsys.multibanking.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class BookingCategoryTO extends ContractTO {

    private Set<String> rules;
    private String receiver;
    private Map<String, String> custom;

}
