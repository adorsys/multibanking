package de.adorsys.multibanking.domain.response;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class SubmitAuthorizationCodeResponse extends AbstractResponse {

    private String status;
    private String transactionId;
    private String hbciSysId;
    private Map<String, String> hbciUpd;
    private List<String> messages;

}
