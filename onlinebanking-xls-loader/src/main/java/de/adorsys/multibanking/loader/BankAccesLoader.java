package de.adorsys.multibanking.loader;

import static de.adorsys.multibanking.utils.CellUtils.stringCell;
import static de.adorsys.multibanking.utils.CellUtils.stringFromNumCell;

import org.apache.poi.ss.usermodel.Row;

import de.adorsys.multibanking.exception.DuplicatedResourceException;
import de.adorsys.multibanking.service.XLSBankAccessService;
import domain.BankAccess;

public class BankAccesLoader {
	private XLSBankAccessService bankAccesService;
	public BankAccesLoader(XLSBankAccessService bankAccesService) {
		this.bankAccesService = bankAccesService;
	}

	public void update(Row row) {
		BankAccess bankAccess = new BankAccess();
//		String iban = stringFromNumCell(row, 0, false);
//		bankAccess.setBankCode(String.format("%.0f",iban));
		bankAccess.setBankCode(stringFromNumCell(row, 0, false));
		bankAccess.setBankLogin(stringCell(row, 1, false));
		String pin = stringFromNumCell(row, 3, false);
		bankAccess.setHbciPassportState(stringCell(row, 4, true));
		if(!bankAccesService.hasBankAccessForBankCode(bankAccess.getBankLogin(), bankAccess.getBankCode())){
			bankAccesService.addBankAccess(bankAccess.getBankLogin(), pin, bankAccess);
		}else {
			throw new DuplicatedResourceException("BankAccess with bank code already exist "+bankAccess.getBankCode()) ;
		}
	}
}
