package domain;

import lombok.Data;

/**
 * Created by cbr on 24.08.18.
 */
@Data
public abstract class AbstractScaType {
    private TanTransportType tanMedia;
    private PaymentChallenge paymentChallenge;
}
