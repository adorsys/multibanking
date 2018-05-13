package de.adorsys.multibanking.mock.utils;

import org.apache.commons.lang3.StringUtils;
import org.iban4j.IbanUtil;

import domain.BankAccount;
import exception.InvalidIbanException;
/**
 * @author cga
 *
 */
public class IbanUtils {

	public static  void extractDetailFromIban(BankAccount bankAccount) {
		String iban = bankAccount.getIban();
		if(StringUtils.isBlank(iban)) return;
		try {
			bankAccount.setAccountNumber(IbanUtil.getAccountNumber(iban));
			bankAccount.setCountry(IbanUtil.getCountryCode(iban));
			bankAccount.setBlz(IbanUtil.getBankCode(iban));
		} catch (Exception e) {
			throw new InvalidIbanException(String.format("The IBAN: %s is not well formatted  eg:DE81100000004076397393 ",iban));
		}
	}
}
