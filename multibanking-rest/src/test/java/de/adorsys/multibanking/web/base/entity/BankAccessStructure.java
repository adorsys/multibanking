package de.adorsys.multibanking.web.base.entity;

/**
 * Created by peter on 07.05.18 at 09:23.
 */
public class BankAccessStructure {
    private final String bankCode;
    private final String bankLogin;
    private final String pin;

    public BankAccessStructure(String bankCode, String bankLogin, String pin) {
        this.bankCode = bankCode;
        this.bankLogin = bankLogin;
        this.pin = pin;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getBankLogin() {
        return bankLogin;
    }

    public String getPin() {
        return pin;
    }
}
