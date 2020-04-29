package de.adorsys.multibanking.domain;

public enum ConsentStatus {
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
