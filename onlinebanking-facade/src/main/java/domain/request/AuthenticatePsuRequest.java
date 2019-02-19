package domain.request;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class AuthenticatePsuRequest extends AbstractRequest {

    private String paymentId;
    private String bankCode;
    private String customerId;
    private String login;
    private String pin;
}
