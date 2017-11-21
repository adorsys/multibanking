package domain;

import lombok.Builder;
import lombok.Data;

/**
 * Created by alexg on 10.11.17.
 */
@Data
@Builder
public class PaymentChallenge {

    private String title;
    private String label;
    private String format;
    private String data;

}
