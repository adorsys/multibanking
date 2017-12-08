package domain;

import lombok.Data;

import java.util.List;

/**
 * Created by alexg on 04.12.17.
 */
@Data
public class RuleCategory {

    String id;
    String name;
    List<RuleCategory> subcategories;
    List<RuleCategory> specifications;

}
