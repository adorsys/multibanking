package de.adorsys.multibanking.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.domain.BankAccessData;
import de.adorsys.multibanking.domain.BankAccountData;
import lombok.Data;

/**
 * This is the user data object. It is the unit of storage.
 *
 * @author fpo 2018-04-02 03:55
 */
@Data
public class UserData {
    private UserEntity userEntity;
    private List<BankAccessData> bankAccesses = new ArrayList<>();

    private AccountSynchPref accountSynchPref;

    public Optional<BankAccessData> getBankAccess(String accessId) {
    	return bankAccesses.stream().filter(b -> StringUtils.equals(accessId, b.getBankAccess().getId())).findFirst();
    }

    public BankAccessData bankAccessDataOrException(String accessId) {
        return getBankAccess(accessId).orElseThrow(() -> new ResourceNotFoundException(BankAccessData.class, accessId));
    }

    public BankAccountData bankAccountDataOrException(String accessId, String accountId) {
        return bankAccessDataOrException(accessId)
        .getBankAccount(accountId).orElseThrow(() -> new ResourceNotFoundException(BankAccountData.class, accessId));
    }

    public BankAccessData remove(String accessId) {
    	List<BankAccessData> candidates = bankAccesses.stream()
    			.filter(b -> StringUtils.equals(accessId, b.getBankAccess().getId()))
    			.collect(Collectors.<BankAccessData> toList());
    	if(candidates.isEmpty()) return null;
    	bankAccesses.removeAll(candidates);
    	return candidates.iterator().next();
    }

    public boolean containsKey(String accessId) {
    	return getBankAccess(accessId).isPresent();
    }

    public void put(String id, BankAccessData b) {
        remove(id);
        bankAccesses.add(b);
    }

    public BankAccessData get(String accessId) {
        return getBankAccess(accessId).orElse(null);
    }
}
