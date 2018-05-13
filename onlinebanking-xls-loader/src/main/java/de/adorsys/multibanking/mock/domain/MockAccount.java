package de.adorsys.multibanking.mock.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.adorsys.multibanking.mock.domain.BankAccessData;
import de.adorsys.multibanking.mock.service.XLSBankAccessService;
import de.adorsys.multibanking.mock.service.XLSBankAccountService;
import de.adorsys.multibanking.mock.service.XLSBookingService;
import de.adorsys.multibanking.mock.service.XLSStandingOrderService;
import domain.BankAccess;
import domain.BankAccount;
import domain.Booking;
import domain.StandingOrder;
import exception.ResourceNotFoundException;

/**
 * This represents a mock account. The mock account holds mock data loaded for a 
 * user.
 * 
 * A mock account can have many bank accesses.
 * 
 * @author fpo
 *
 */
public class MockAccount implements XLSBankAccessService, XLSBankAccountService, XLSBookingService, XLSStandingOrderService {

	/* iban, BankAccess*/
	private final Map<String, BankAccessData> bankAccessMapByBankLogin = new HashMap<>();

	public void addBankAccess(BankAccessData bankAccess) {
		bankAccessMapByBankLogin.put(bankAccess.getBankAccess().getBankLogin(), bankAccess);
	}

	public Optional<BankAccessData> access(String bankLogin){
		return Optional.ofNullable(bankAccessMapByBankLogin.get(bankLogin));
	}

	public BankAccessData accessOrException(String bankLogin) {
		return access(bankLogin).orElseThrow(() -> notFound(bankLogin));
	}
	
	private ResourceNotFoundException notFound(String bankLogin){
		return new ResourceNotFoundException(String.format("BankLogin with id %s not found", bankLogin));		
	}
	
	@Override
	public void addStandingOrders(String bankLogin, String iban, StandingOrder standingOrder) {
		accessOrException(bankLogin)
			.accountDataOrException(iban).addStandingOrders(iban, standingOrder);
	}

	@Override
	public void addBooking(String bankLogin, String iban, Booking booking) {
		accessOrException(bankLogin)
			.accountDataOrException(iban).addBooking(booking);
	}

	@Override
	public void addBankAccount(String bankLogin, BankAccount bankAccount) {
		bankAccount.setIban(bankAccount.getIban());
		accessOrException(bankLogin).addBankAccount(bankAccount);
	}

	@Override
	public boolean hasBankAccessForBankCode(String bankLogin, String bankCode) {
		if(access(bankLogin).isPresent())
			return accessOrException(bankLogin).bankCode(bankCode).isPresent();
		return false;
	}

	@Override
	public void addBankAccess(String bankLogin, String pin, BankAccess bankAccess) {
		addBankAccess(new BankAccessData(bankAccess, pin));
	}

	public List<BankAccount> loadBankAccounts(BankAccess bankAccess, String bankCode, String pin) {
		BankAccessData accessData = accessOrException(bankAccess.getBankLogin());
		accessData.checkPin(pin);
		return accessData.loadBankAccounts(bankCode);
	}
	
}
