package de.adorsys.multibanking.domain.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public abstract class AbstractResponse {

    private String hbciSysId;
    private Map<String, String> hbciUpd;
    private List<String> messages;
}
