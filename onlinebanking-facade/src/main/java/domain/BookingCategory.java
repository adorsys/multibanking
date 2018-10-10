package domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

/**
 * Created by alexg on 18.05.17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class BookingCategory extends Contract {

    private Set<String> rules;

    private String mainCategory;
    private String subCategory;
    private String specification;
    private String receiver;

    private Map<String, String> custom;

}
