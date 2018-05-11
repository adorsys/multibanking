package de.adorsys.multibanking.loader;

import static de.adorsys.multibanking.utils.CellUtils.bigDecimalCell;
import static de.adorsys.multibanking.utils.CellUtils.localDate;
import static de.adorsys.multibanking.utils.CellUtils.stringCell;

import org.apache.poi.ss.usermodel.Row;

import de.adorsys.multibanking.service.XLSStandingOrderService;
import de.adorsys.multibanking.utils.IbanUtils;
import domain.BankAccount;
import domain.Cycle;
import domain.StandingOrder;

public class StandingOrderLoader {
	private XLSStandingOrderService standingOrderService;

	public StandingOrderLoader(XLSStandingOrderService standingOrderService) {
		this.standingOrderService = standingOrderService;
	}

	public void update(Row row) {
		String bankLogin=stringCell(row, 0, false);		
		String iban = stringCell(row, 1, false);;

		StandingOrder standingOrder = new StandingOrder();
		standingOrder.setAmount(bigDecimalCell(row, 2, false));
		standingOrder.setCycle(Cycle.valueOf(stringCell(row, 3, false)));
		standingOrder.setExecutionDay(bigDecimalCell(row, 4, false).intValue());
		standingOrder.setFirstExecutionDate(localDate(row, 5, false));
		standingOrder.setLastExecutionDate(localDate(row, 6, true));
		standingOrder.setUsage(stringCell(row, 7, false));

		// set other account
		BankAccount bankAccount = new BankAccount();
		bankAccount.setOwner(stringCell(row, 8, true));
		bankAccount.setIban(stringCell(row, 9, true));
		bankAccount.setBic(stringCell(row, 10, true));
		bankAccount.setBankName(stringCell(row, 11, true));
		IbanUtils.extractDetailFromIban(bankAccount);

		standingOrder.setOtherAccount(bankAccount);
		standingOrder.setOrderId(orderId(standingOrder));
		standingOrderService.addStandingOrders(bankLogin, iban, standingOrder);
	}

	private String orderId(StandingOrder standingOrder) {
		String iban = standingOrder.getOtherAccount().getIban();
		return "" + standingOrder.getAmount() + standingOrder.getCycle() + standingOrder.getUsage() + iban;
	}

}
