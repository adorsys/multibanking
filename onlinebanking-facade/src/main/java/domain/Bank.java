package domain;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * Created by alexg on 10.07.17.
 */
@Data
public class Bank {

    private String bankCode;
    private String bic;
    private String name;
    private BankLoginSettings loginSettings;


}
