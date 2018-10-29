package de.adorsys.multibanking.mock.loader;

import de.adorsys.multibanking.mock.service.XLSBankAccountService;
import domain.*;
import exception.ResourceNotFoundException;
import org.apache.poi.ss.usermodel.Row;
import org.iban4j.IbanUtil;

import java.util.Random;
import java.util.logging.Logger;

import static de.adorsys.multibanking.mock.utils.CellUtils.bigDecimalCell;
import static de.adorsys.multibanking.mock.utils.CellUtils.stringCell;

public class BankAccountLoader {
    private static final Logger LOG = Logger.getLogger(BankAccountLoader.class.getName());
    private XLSBankAccountService bankAccountServiceService;
    private MockBankCatalogue bankCatalogue;

    public BankAccountLoader(XLSBankAccountService bankAccountServiceService, MockBankCatalogue bankCatalogue) {
        this.bankAccountServiceService = bankAccountServiceService;
        this.bankCatalogue = bankCatalogue;
    }

    public void update(Row row) {
        String bankLogin = stringCell(row, 0, false);

        BankAccount bankAccount = new BankAccount();
        bankAccount.setIban(stringCell(row, 1, false));

        String iban = bankAccount.getIban();
        try {
            bankAccount.setAccountNumber(IbanUtil.getAccountNumber(iban));
            bankAccount.setCountry(IbanUtil.getCountryCode(iban));
            bankAccount.setBlz(IbanUtil.getBankCode(iban));
            Bank bankEntity =
					bankCatalogue.getBank(bankAccount.getBlz()).orElseThrow(() -> new ResourceNotFoundException("Bank " +
							"not Found " + bankAccount.getBlz()));
            bankAccount.setBankName(bankEntity.getName());
            bankAccount.setBic(bankEntity.getBic());
        } catch (Exception e) {
            LOG.fine("The IBAN: " + iban + " is not well formatted  eg:DE81100000004076397393 ");
        }

        bankAccount.setType(BankAccountType.valueOf(stringCell(row, 2, false)));
        bankAccount.setCurrency(stringCell(row, 3, false));
        bankAccount.owner(stringCell(row, 4, false));
        bankAccount.bankAccountBalance(new BalancesReport());

        bankAccount.getBalances().readyHbciBalance(Balance.builder().amount(bigDecimalCell(row, 5, true)).build());
        bankAccount.getBalances().unreadyHbciBalance(Balance.builder().amount(bigDecimalCell(row, 6, true)).build());
        bankAccount.getBalances().creditHbciBalance(Balance.builder().amount(bigDecimalCell(row, 7, true)).build());
        bankAccount.getBalances().availableHbciBalance(Balance.builder().amount(bigDecimalCell(row, 8, true)).build());
        bankAccount.getBalances().usedHbciBalance(Balance.builder().amount(bigDecimalCell(row, 9, true)).build());
        bankAccount.externalId(BankApi.MOCK, new Random().nextInt(1000) + "");
        bankAccountServiceService.addBankAccount(bankLogin, bankAccount);
    }

}
