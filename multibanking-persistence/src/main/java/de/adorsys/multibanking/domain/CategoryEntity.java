package de.adorsys.multibanking.domain;

import lombok.Data;

/**
 * Created by alexg on 05.05.17.
 */
@Data
public class CategoryEntity {

    private String mainCategory;
    private String subCategory;
    private String specification;
    private boolean variable;
    private ContractEntity contract;

    public CategoryEntity mainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
        return this;
    }

    public CategoryEntity subCategory(String subCategory) {
        this.subCategory = subCategory;
        return this;
    }

    public CategoryEntity specification(String specification) {
        this.specification = specification;
        return this;
    }

    public CategoryEntity variable(boolean variable) {
        this.variable = variable;
        return this;
    }

    public CategoryEntity contract(ContractEntity contract) {
        this.contract = contract;
        return this;
    }
}
