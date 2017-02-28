package de.adorsys.multibanking.banking;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.repository.BankAccessRepository;
import de.adorsys.multibanking.repository.BankAccountRepository;
import domain.BankAccount;
import domain.Booking;
import hbci4java.Hbci4JavaBanking;
import hbci4java.OnlineBankingService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by alexg on 25.02.17.
 */
@Service
public class BankingService {

    @Autowired
    BankAccountRepository bankAccountRepository;
    @Autowired
    BankAccessRepository bankAccessRepository;

    OnlineBankingService onlineBankingService = new Hbci4JavaBanking();

    public Optional<List<BankAccountEntity>> loadBankAccounts(BankAccessEntity bankAccess, String pin) {
        Optional<List<BankAccount>> bankAccounts = onlineBankingService.loadBankAccounts(bankAccess, pin);

        List<BankAccountEntity> bankAccountEntities = null;
        if (bankAccounts.isPresent()) {
            bankAccountEntities = new ArrayList<>();
            for (BankAccount source : bankAccounts.get()) {
                BankAccount target = new BankAccountEntity();
                BeanUtils.copyProperties(source, target);
                bankAccountEntities.add((BankAccountEntity)target);
            }
        }

        return Optional.ofNullable(bankAccountEntities);
    }

    public Optional<List<BookingEntity>> loadBookings(BankAccessEntity bankAccess, BankAccountEntity bankAccount, String pin) {
        Optional<List<Booking>> bookings = onlineBankingService.loadBookings(bankAccess, bankAccount, pin);

        List<BookingEntity> bookingEntities = null;
        if (bookings.isPresent()) {
            bookingEntities = new ArrayList<>();
            for (Booking source : bookings.get()) {
                BookingEntity target = new BookingEntity();
                BeanUtils.copyProperties(source, target);
                bookingEntities.add(target);
            }
        }

        //update bankAccount, balance have changed
        bankAccountRepository.save(bankAccount);

        //update also bankaccess, passportstate have changed
        bankAccessRepository.save(bankAccess);

        return Optional.ofNullable(bookingEntities);
    }

}
