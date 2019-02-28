package domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountReference {
    private String iban;
    private String currency;
}
