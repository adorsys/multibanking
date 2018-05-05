package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.exception.ResourceNotFoundException;
import domain.BankAccess;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is the user data object. It is the unit of storage.
 *
 * @author fpo 2018-04-02 03:55
 */
@Data
public class UserData {
    private UserEntity userEntity;
    private List<BankAccessData> bankAccesses = new ArrayList<>();
    private List<ContractEntity> contracts = new ArrayList<>();

    private AccountSynchPref accountSynchPref;

    public Optional<BankAccessData> getBankAccess(String accessId) {
        for (BankAccessData d : bankAccesses) {
            if (d.getBankAccess().getId().equals(accessId)) {
                return Optional.of(d);
            }
        }
        return Optional.empty();
    }

    public BankAccessData bankAccessData(String accessId) {
        return getBankAccess(accessId).orElseThrow(() -> new ResourceNotFoundException(BankAccessData.class, accessId));
    }

    public BankAccountData bankAccountData(String accessId, String accountId) {
        return bankAccessData(accessId)
        .getBankAccount(accountId).orElseThrow(() -> new ResourceNotFoundException(BankAccountData.class, accessId));
    }

    public BankAccessData remove(String accessId) {
        for (BankAccessData d : bankAccesses) {
            if (d.getBankAccess().getId().equals(accessId)) {
                bankAccesses.remove(d);
                return d;
            }
        }
        return null;
    }

    public boolean containsKey(String accessId) {
        for (BankAccessData d : bankAccesses) {
            if (d.getBankAccess().getId().equals(accessId)) {
                return true;
            }
        }
        return false;
    }

    public void put(String id, BankAccessData b) {
        remove(id);
        bankAccesses.add(b);
    }

    public BankAccessData get(String accessId) {
        return getBankAccess(accessId).get();
    }
}
