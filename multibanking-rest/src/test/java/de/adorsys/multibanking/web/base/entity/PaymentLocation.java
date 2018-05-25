package de.adorsys.multibanking.web.base.entity;

import java.net.URI;

/**
 * Created by peter on 25.05.18 at 13:45.
 */
public class PaymentLocation {
    URI location;
    public PaymentLocation(URI location) {
        this.location = location;
    }

    public URI getLocation() {
        return location;
    }

    public PaymentID getPaymentID() {
        String rawPath = location.getRawPath();
        int beginID = rawPath.lastIndexOf("/");
        String id = rawPath.substring(beginID+1);
        return new PaymentID(id);
    }
}
