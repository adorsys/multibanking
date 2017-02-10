package de.adorsys.multibanking.banking;

import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.Booking;

import java.util.List;
import java.util.Optional;

public interface OnlineBankingService {

    Optional<List<BankAccount>> loadBankAccounts(BankAccess bankAccess, String pin);

    Optional<List<Booking>> loadBookings(BankAccess bankAccess, BankAccount bankAccount, String pin);

}