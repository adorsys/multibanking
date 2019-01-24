package domain.request;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class AuthenticatePsuRequest extends AbstractHbciRequest {

    private String bankCode;
    private String customerId;
    private String login;
    private String pin;
}
