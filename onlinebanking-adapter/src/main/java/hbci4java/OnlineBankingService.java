package hbci4java;


import domain.BankAccess;
import domain.BankAccount;
import domain.Booking;

import java.util.List;

public interface OnlineBankingService {

    List<BankAccount> loadBankAccounts(BankAccess bankAccess, String pin);

    List<Booking> loadBookings(BankAccess bankAccess, BankAccount bankAccount, String pin);

}