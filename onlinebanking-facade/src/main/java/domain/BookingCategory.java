package domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * Created by alexg on 18.05.17.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCategory {

    private Set<String> rules;
    private String mainCategory;
    private String subCategory;
    private String specification;
    private boolean variable;
    private Contract contract;


}
