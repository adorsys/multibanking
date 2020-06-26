package de.adorsys.multibanking.web.model;

public enum ConsentStatusTO {
    /* @formatter:off */
    RECEIVED,
    REJECTED,
    VALID,
    REVOKEDBYPSU,
    EXPIRED,
    TERMINATEDBYTPP,
    TERMINATED_BY_ASPSP,
    PARTIALLY_AUTHORISED
}
