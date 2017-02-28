package hbci4java;


import domain.BankAccess;
import domain.BankAccount;
import domain.Booking;

import java.util.List;
import java.util.Optional;

public interface OnlineBankingService {

    Optional<List<BankAccount>> loadBankAccounts(BankAccess bankAccess, String pin);

    Optional<List<Booking>> loadBookings(BankAccess bankAccess, BankAccount bankAccount, String pin);

}