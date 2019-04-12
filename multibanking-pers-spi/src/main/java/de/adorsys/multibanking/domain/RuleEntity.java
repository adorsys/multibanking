package de.adorsys.multibanking.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Document
public class RuleEntity extends Rule {

    @Id
    private String id;
    @Indexed
    private String userId;
    @Indexed
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
