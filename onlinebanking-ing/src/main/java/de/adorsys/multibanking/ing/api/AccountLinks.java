package de.adorsys.multibanking.ing.api;

public class AccountLinks {
    private HrefType balances;

    private HrefType transactions;

    public HrefType getBalances() {
        return balances;
    }

    public void setBalances(HrefType balances) {
        this.balances = balances;
    }

    public HrefType getTransactions() {
        return transactions;
    }

    public void setTransactions(HrefType transactions) {
        this.transactions = transactions;
    }
}
