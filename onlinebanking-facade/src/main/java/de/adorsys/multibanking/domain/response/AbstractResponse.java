package de.adorsys.multibanking.domain.response;

import de.adorsys.multibanking.domain.PsuMessage;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public abstract class AbstractResponse {

    private AuthorisationCodeResponse authorisationCodeResponse;

    private List<PsuMessage> messages;

    private String hbciSysId;
    private Map<String, String> hbciUpd;

    public boolean containsMessage(String messageCode) {
        return Optional.ofNullable(messages)
            .filter(psuMessages -> psuMessages.stream()
                .anyMatch(psuMessage -> psuMessage.getCode() != null && messageCode.equals(psuMessage.getCode()))
            )
            .isPresent();

    }

}
