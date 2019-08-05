package de.adorsys.multibanking.domain.spi;

/**
 * Marker Interface
 */
public interface StrongCustomerAuthorisation {
    default String toExceptionInfo() {
        return null;
    }
}
