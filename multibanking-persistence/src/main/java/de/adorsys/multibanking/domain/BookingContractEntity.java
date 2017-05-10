package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.encrypt.Encrypted;
import lombok.Data;

/**
 * Created by alexg on 05.05.17.
 */
@Data
@Encrypted(fields = {"logo", "homepage", "hotline", "email", "mandatReference"})
public class BookingContractEntity {

    private String logo;
    private String homepage;
    private String hotline;
    private String email;
    private String mandatReference;

    public BookingContractEntity logo(String logo) {
        this.logo = logo;
        return this;
    }

    public BookingContractEntity homepage(String homepage) {
        this.homepage = homepage;
        return this;
    }

    public BookingContractEntity hotline(String hotline) {
        this.hotline = hotline;
        return this;
    }

    public BookingContractEntity email(String email) {
        this.email = email;
        return this;
    }

    public BookingContractEntity mandatReference(String mandatReference) {
        this.mandatReference = mandatReference;
        return this;
    }
}
