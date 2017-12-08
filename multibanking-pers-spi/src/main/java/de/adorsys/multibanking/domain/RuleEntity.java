package de.adorsys.multibanking.domain;

import domain.Rule;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by alexg on 05.09.17.
 */
@Data
@Document
public class RuleEntity extends Rule {

    @Id
    private String id;

}
