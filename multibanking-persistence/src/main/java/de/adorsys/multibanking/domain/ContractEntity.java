package de.adorsys.multibanking.domain;

import lombok.Data;

/**
 * Created by alexg on 05.05.17.
 */
@Data
public class ContractEntity {

    private String logo;
    private String homepage;
    private String hotline;
    private String email;
    private String mandatReference;

    public ContractEntity logo(String logo) {
        this.logo = logo;
        return this;
    }

    public ContractEntity homepage(String homepage) {
        this.homepage = homepage;
        return this;
    }

    public ContractEntity hotline(String hotline) {
        this.hotline = hotline;
        return this;
    }

    public ContractEntity email(String email) {
        this.email = email;
        return this;
    }

    public ContractEntity mandatReference(String mandatReference) {
        this.mandatReference = mandatReference;
        return this;
    }
}
