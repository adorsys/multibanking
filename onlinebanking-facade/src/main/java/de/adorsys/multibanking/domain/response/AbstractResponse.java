package de.adorsys.multibanking.domain.response;

import de.adorsys.multibanking.domain.Message;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public abstract class AbstractResponse {

    private AuthorisationCodeResponse authorisationCodeResponse;
    private List<Message> messages;

    private Object bankApiConsentData;

    public boolean containsMessage(String messageCode) {
        return Optional.ofNullable(messages)
            .map(psuMessages -> psuMessages.stream()
                .anyMatch(psuMessage -> psuMessage.getKey() != null && messageCode.equals(psuMessage.getKey()))
            )
            .orElse(false);
    }

}
