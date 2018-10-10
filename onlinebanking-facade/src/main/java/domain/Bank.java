package domain;

import lombok.Data;

/**
 * Created by alexg on 10.07.17.
 */
@Data
public class Bank {

    private String bankingUrl;
    private String bankCode;
    private String bic;
    private String name;
    private BankLoginSettings loginSettings;
    private BankApi bankApi;

}
