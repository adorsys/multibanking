package de.adorsys.multibanking.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class RuleEntity extends Rule {

    private String id;
    private String userId;
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

        if (getSimilarityMatchType() != null && getExpression() != null) {
            searchIndex.add(getExpression());
        }
    }

}
