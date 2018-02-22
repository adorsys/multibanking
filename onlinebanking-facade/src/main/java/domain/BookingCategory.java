package domain;

import java.util.Set;

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
	
	private Set<String> rules;

    private String mainCategory;

    private String mainCategoryName;

    private String subCategory;

    private String subCategoryName;

    private String specification;

    private String specificationName;

    private boolean variable;

    private Contract contract;

}
