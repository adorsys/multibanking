package domain.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CreateConsentResponse {
    private String consentId;
    private LocalDate validUntil;
}
