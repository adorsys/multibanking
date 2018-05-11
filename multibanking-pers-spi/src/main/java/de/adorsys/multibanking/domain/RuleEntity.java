package de.adorsys.multibanking.domain;

import domain.Rule;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexg on 05.09.17.
 */
@Data
@Document
public class RuleEntity extends Rule {

    @Id
    private String id;
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
    }

}
