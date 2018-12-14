package domain;

import lombok.Data;

import java.util.Map;

/**
 * Created by alexg on 17.05.17.
 */
@Data
public class BankApiUser {

    private String apiUserId;
    private String apiPassword;
    private BankApi bankApi;
    private Map<String, String> properties;

}
