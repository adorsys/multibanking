package de.adorsys.multibanking.domain;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by alexg on 05.09.17.
 */
@Data
@Document
public class CustomRuleEntity extends RuleEntity {

    private String creator;

}
