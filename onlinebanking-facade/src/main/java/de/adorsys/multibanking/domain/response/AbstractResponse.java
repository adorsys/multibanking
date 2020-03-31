package de.adorsys.multibanking.domain.response;

import de.adorsys.multibanking.domain.PsuMessage;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public abstract class AbstractResponse {

    private AuthorisationCodeResponse authorisationCodeResponse;
    private List<PsuMessage> messages;

    private Object bankApiConsentData;

    public boolean containsMessage(String messageCode) {
        return Optional.ofNullable(messages)
            .map(psuMessages -> psuMessages.stream()
                .anyMatch(psuMessage -> psuMessage.getCode() != null && messageCode.equals(psuMessage.getCode()))
            )
            .orElse(false);
    }

}
