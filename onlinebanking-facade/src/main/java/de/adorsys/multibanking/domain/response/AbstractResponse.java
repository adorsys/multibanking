package de.adorsys.multibanking.domain.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public abstract class AbstractResponse {

    private AuthorisationCodeResponse authorisationCodeResponse;

    private List<String> messages;
    private List<String> warnings;

    private String hbciSysId;
    private Map<String, String> hbciUpd;

}
