package domain;

import lombok.Data;

/**
 * Created by alexg on 04.12.17.
 */
@Data
public class Rule {

    String ruleId;
    String mainCategory;
    String subCategory;
    String specification;

    String creditorId;
    String expression;
    String receiver;
    String ruleType;

    String logo;
    String hotline;
    String homepage;
    String email;

    boolean incoming;
    boolean taxRelevant;
}
