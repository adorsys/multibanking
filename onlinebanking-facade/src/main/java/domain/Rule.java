package domain;

import lombok.Data;

/**
 * Created by alexg on 04.12.17.
 */
@Data
public class Rule {

    public enum SIMILARITY_MATCH_TYPE {
        MANDATE_REFERENCE, REFERENCE_NAME, PURPOSE
    }

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

}
