package de.adorsys.multibanking.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisationContainer;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(value = {"hbciPassportState"}, allowSetters = true)
public class BankAccessEntity extends BankAccess implements StrongCustomerAuthorisationContainer {

    private String id;
    private String userId;
    private boolean temporary;
    private boolean storeBookings;
    private boolean categorizeBookings;
    private boolean storeAnalytics;
    private boolean storeAnonymizedBookings;
    private boolean provideDataForMachineLearning;

    public BankAccessEntity id(String id) {
        this.id = id;
        return this;
    }
}
