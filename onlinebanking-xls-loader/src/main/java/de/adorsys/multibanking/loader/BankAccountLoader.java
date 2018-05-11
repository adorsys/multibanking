package de.adorsys.multibanking.loader;

import static de.adorsys.multibanking.utils.CellUtils.bigDecimalCell;
import static de.adorsys.multibanking.utils.CellUtils.stringCell;

import java.util.Random;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Row;
import org.iban4j.IbanUtil;

import de.adorsys.multibanking.service.XLSBankAccountService;
import domain.Bank;
import domain.BankAccount;
import domain.BankAccountBalance;
import domain.BankAccountType;
import domain.BankApi;
import exception.ResourceNotFoundException;

public class BankAccountLoader {
	private static final Logger LOG = Logger.getLogger(BankAccountLoader.class.getName());
	private XLSBankAccountService bankAccountServiceService;
	private MockBankCatalogue  bankCatalogue ;

	public BankAccountLoader(XLSBankAccountService bankAccountServiceService, MockBankCatalogue bankCatalogue) {
		this.bankAccountServiceService = bankAccountServiceService;
		this.bankCatalogue = bankCatalogue;
	}

	public void update(Row row) {
		String bankLogin=stringCell(row, 0, false);		

		BankAccount bankAccount = new BankAccount();
		bankAccount.setIban(stringCell(row, 1, false));

		String iban = bankAccount.getIban();
		try {
			bankAccount.setAccountNumber(IbanUtil.getAccountNumber(iban));
			bankAccount.setCountry(IbanUtil.getCountryCode(iban));
			bankAccount.setBlz(IbanUtil.getBankCode(iban));
			Bank bankEntity = bankCatalogue.getBank(bankAccount.getBlz()).orElseThrow(()-> new ResourceNotFoundException("Bank not Found "+bankAccount.getBlz()));
			bankAccount.setBankName(bankEntity.getName());
			bankAccount.setBic(bankEntity.getBic());
		} catch (Exception e) {
			LOG.fine("The IBAN: "+ iban +" is not well formatted  eg:DE81100000004076397393 ");
		}

		bankAccount.setType(BankAccountType.valueOf(stringCell(row, 2, false)));
		bankAccount.setCurrency(stringCell(row, 3, false));
		bankAccount.owner(stringCell(row, 4, false));
		bankAccount.bankAccountBalance(new BankAccountBalance());

		bankAccount.getBankAccountBalance().readyHbciBalance(bigDecimalCell(row, 5, true));
		bankAccount.getBankAccountBalance().unreadyHbciBalance(bigDecimalCell(row, 6, true));
		bankAccount.getBankAccountBalance().creditHbciBalance(bigDecimalCell(row, 7, true));
		bankAccount.getBankAccountBalance().availableHbciBalance(bigDecimalCell(row, 8, true));
		bankAccount.getBankAccountBalance().usedHbciBalance(bigDecimalCell(row, 9, true));
		bankAccount.externalId(BankApi.MOCK, new Random().nextInt(1000)+"");
		bankAccountServiceService.addBankAccount(bankLogin, bankAccount);
	}

}
