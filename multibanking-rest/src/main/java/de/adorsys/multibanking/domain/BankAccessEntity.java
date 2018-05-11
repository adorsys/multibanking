package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.domain.common.IdentityIf;
import domain.BankAccess;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by alexg on 07.02.17.
 */
@Data
public class BankAccessEntity extends BankAccess implements IdentityIf {

    private String id;
    @ApiModelProperty(value = "Unique identifier of the user owner of this bank access", example = "3c149076-13c4-4190-ace3-e30bf8f61526")
    private String userId;
    @ApiModelProperty(value = "The personal identification number or password used to login the user with the online banking account.", example = "12345")
    private String pin;
    private String pin2;
    @ApiModelProperty(value = "Indicates that this is a temporary account. Will be deleted upon expiration", example = "false")
    private boolean temporary;
    @ApiModelProperty(value = "States whether the PIN shall be stored for asynchronous access to the user's online banking account. Will be stored separately and not returned with user data", example = "true")
    private boolean storePin;
    @ApiModelProperty(value = "States whether bookings loaded from the user's online banking account shall be stored.", example = "true")
    private boolean storeBookings = true;
    @ApiModelProperty(value = "States whether bookings loaded from the user's online banking account shall be sent to category service.", example = "true")
    private boolean categorizeBookings;
    @ApiModelProperty(value = "States whether analytics result shall be stored.", example = "true")
    private boolean storeAnalytics;
    @ApiModelProperty(value = "States whether anonymized booking shall be stored.", example = "true")
    private boolean storeAnonymizedBookings;

    public BankAccessEntity id(String id) {
        this.id = id;
        return this;
    }
}
