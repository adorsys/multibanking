package de.adorsys.multibanking.jpa.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@Entity(name="analytics_rule")
@Data
@EqualsAndHashCode(callSuper = false)
public class RuleJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String userId;
    @ElementCollection
    @CollectionTable(
            name = "rule_search",
            joinColumns = @JoinColumn(name = "rule_id")
    )
    private List<String> searchIndex;
    private String ruleId;
    private String mainCategory;
    private String subCategory;
    private String specification;
    private SIMILARITY_MATCH_TYPE similarityMatchType;
    private String creditorId;
    private String expression;
    private String receiver;
    private String ruleType;
    private String logo;
    private String hotline;
    private String homepage;
    private String email;
    private boolean incoming;

    public enum SIMILARITY_MATCH_TYPE {
        IBAN, REFERENCE_NAME, PURPOSE
    }

}
