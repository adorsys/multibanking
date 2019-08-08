package de.adorsys.multibanking.domain.spi;

import java.util.List;

public interface StrongCustomerAuthorisable<T extends StrongCustomerAuthorisation> {
    void containsValidAuthorisation(StrongCustomerAuthorisationContainer authorisationContainer);
    T createAuthorisation(T authorisation);
    T getAuthorisation(String authorisationId);
    List<T> getAuthorisationList();
    void revokeAuthorisation(String authorisationId);

}
