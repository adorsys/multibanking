package domain;

import java.util.Set;

import io.swagger.annotations.ApiModelProperty;
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
  @ApiModelProperty(value = "Main Category")
  private String mainCategory;
  private String mainCategoryName;
  @ApiModelProperty(value = "Sub Category")
  private String subCategory;
  private String subCategoryName;
  @ApiModelProperty(value = "Specification")
  private String specification;
  private String specificationName;
  @ApiModelProperty(value = "Variable", example="true")
  private boolean variable;
  @ApiModelProperty(value = "Contract", example="false")
  private Contract contract;

}
