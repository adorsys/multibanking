package domain;

import lombok.Data;

/**
 * Created by alexg on 04.12.17.
 */
@Data
public class Rule {

    private String ruleId;
    private String mainCategory;
    private String subCategory;
    private String specification;

    private String creditorId;
    private String expression;
    private String receiver;
    private String ruleType;

    private String logo;
    private String hotline;
    private String homepage;
    private String email;

    private boolean incoming;

}
