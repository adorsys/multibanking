package domain;

import lombok.Data;

/**
 * Created by alexg on 07.02.17.
 */
@Data
public class BankAccess {

    private String bankName;
    private String bankLogin;
    private String bankCode;
    private String passportState;

    public BankAccess bankName(String bankName) {
        this.bankName = bankName;
        return this;
    }

    public BankAccess bankLogin(String bankLogin) {
        this.bankLogin = bankLogin;
        return this;
    }

    public BankAccess bankCode(String bankCode) {
        this.bankCode = bankCode;
        return this;
    }

    public BankAccess passportState(String passportState) {
        this.passportState = passportState;
        return this;
    }


}
