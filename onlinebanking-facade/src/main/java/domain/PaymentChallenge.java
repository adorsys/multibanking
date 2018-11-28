package domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by alexg on 10.11.17.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentChallenge {

    private String title;
    private String label;
    private String format;
    private String data;

}
