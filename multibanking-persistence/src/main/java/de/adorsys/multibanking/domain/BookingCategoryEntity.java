package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.encrypt.Encrypted;
import lombok.Data;

/**
 * Created by alexg on 05.05.17.
 */
@Data
@Encrypted(fields = {"mainCategory", "subCategory", "specification"})
public class BookingCategoryEntity {

    private String mainCategory;
    private String subCategory;
    private String specification;
    private boolean variable;
    private BookingContractEntity contract;

    public BookingCategoryEntity mainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
        return this;
    }

    public BookingCategoryEntity subCategory(String subCategory) {
        this.subCategory = subCategory;
        return this;
    }

    public BookingCategoryEntity specification(String specification) {
        this.specification = specification;
        return this;
    }

    public BookingCategoryEntity variable(boolean variable) {
        this.variable = variable;
        return this;
    }

    public BookingCategoryEntity contract(BookingContractEntity contract) {
        this.contract = contract;
        return this;
    }
}
