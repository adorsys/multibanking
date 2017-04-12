package de.adorsys.multibanking.banking;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.repository.BankAccessRepository;
import de.adorsys.multibanking.repository.BankAccountRepository;
import de.adorsys.multibanking.repository.BookingRepository;
import domain.BankAccount;
import domain.Booking;
import hbci4java.Hbci4JavaBanking;
import hbci4java.OnlineBankingService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by alexg on 25.02.17.
 */
@Service
public class BankingService {

    @Autowired
    BankAccountRepository bankAccountRepository;
    @Autowired
    BankAccessRepository bankAccessRepository;
    @Autowired
    BookingRepository bookingRepository;

    OnlineBankingService onlineBankingService = new Hbci4JavaBanking();

    public List<BankAccountEntity> loadBankAccounts(BankAccessEntity bankAccess, String pin) {
        List<BankAccount> bankAccounts = onlineBankingService.loadBankAccounts(bankAccess, pin);
        List<BankAccountEntity> bankAccountEntities = new ArrayList<>();

        bankAccounts.forEach(source -> {
            BankAccount target = new BankAccountEntity();
            BeanUtils.copyProperties(source, target);
            bankAccountEntities.add((BankAccountEntity) target);

        });

        return bankAccountEntities;
    }

    @Async
    public void loadBookingsAsync(BankAccessEntity bankAccess, BankAccountEntity bankAccount, String pin) {
        loadBookings(bankAccess, bankAccount, pin);
    }

    public void loadBookings(BankAccessEntity bankAccess, BankAccountEntity bankAccount, String pin) {
        List<Booking> bookings = onlineBankingService.loadBookings(bankAccess, bankAccount, pin);
        List<BookingEntity> bookingEntities = new ArrayList<>();

        bookings.forEach(source -> {
            BookingEntity target = new BookingEntity();
            BeanUtils.copyProperties(source, target);
            bookingEntities.add(target);
        });

        //update bankAccount, balance have changed
        bankAccountRepository.save(bankAccount);

        //update also bankaccess, passportstate have changed
        bankAccessRepository.save(bankAccess);

        bookingEntities.forEach(booking -> booking.accountId(bankAccount.getId()));
        try {
            bookingRepository.insert(bookingEntities);
        } catch (DuplicateKeyException e) {
            //ignore it
        }
    }

}
