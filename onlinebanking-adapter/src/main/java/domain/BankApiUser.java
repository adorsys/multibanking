package domain;

import lombok.Data;

/**
 * Created by alexg on 17.05.17.
 */
@Data
public class BankApiUser {

    private String apiUserId;
    private String apiPassword;
    private BankApi bankApi;


}
