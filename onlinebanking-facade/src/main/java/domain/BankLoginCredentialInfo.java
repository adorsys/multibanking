package domain;

import lombok.Data;

/**
 * Created by alexg on 10.07.17.
 */
@Data
public class BankLoginCredentialInfo {

    private String label;
    private boolean masked;
    private boolean optional;

}
