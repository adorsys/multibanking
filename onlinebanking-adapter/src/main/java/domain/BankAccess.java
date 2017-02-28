package domain;

import lombok.Data;

/**
 * Created by alexg on 07.02.17.
 */
@Data
public class BankAccess {

    private String bankLogin;
    private String bankCode;
    private String passportState;

    public String getBankLogin() {
        return bankLogin;
    }

    public BankAccess bankLogin(String bankLogin) {
        this.bankLogin = bankLogin;
        return this;
    }

    public String getBankCode() {
        return bankCode;
    }

    public BankAccess bankCode(String bankCode) {
        this.bankCode = bankCode;
        return this;
    }

    public String passportState() {
        return passportState;
    }

    public BankAccess setPassportState(String passportState) {
        this.passportState = passportState;
        return this;
    }
}
