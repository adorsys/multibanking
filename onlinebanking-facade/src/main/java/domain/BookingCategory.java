package domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by alexg on 18.05.17.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCategory {

    private String mainCategory;
    private String subCategory;
    private String specification;
    private boolean variable;
    private Contract contract;


}
