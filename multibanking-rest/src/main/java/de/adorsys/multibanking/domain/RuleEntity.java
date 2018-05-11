package de.adorsys.multibanking.domain;

import java.util.ArrayList;
import java.util.List;

import de.adorsys.multibanking.domain.common.IdentityIf;
import domain.Rule;
import lombok.Data;

/**
 * Created by alexg on 05.09.17.
 */
@Data
public class RuleEntity extends Rule implements IdentityIf {

    private String id;
    private List<String> searchIndex;

    public void updateSearchIndex() {
        searchIndex = new ArrayList<>();
        if (getCreditorId() != null) {
            searchIndex.add(getCreditorId());
        }

        if (getRuleId() != null) {
            searchIndex.add(getRuleId());
        }

        if (getReceiver() != null) {
            searchIndex.add(getReceiver());
        }
    }

}
