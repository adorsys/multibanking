package domain;

import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by alexg on 18.05.17.
 */
@Data
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
