package de.adorsys.multibanking.web.base;

/**
 * Created by peter on 07.05.18 at 09:23.
 */
public class BankLoginTuple {
    private final String bankCode;
    private final String userID;
    private final String userPIN;

    public BankLoginTuple(String bankCode, String userID, String userPIN) {
        this.bankCode = bankCode;
        this.userID = userID;
        this.userPIN = userPIN;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserPIN() {
        return userPIN;
    }
}
