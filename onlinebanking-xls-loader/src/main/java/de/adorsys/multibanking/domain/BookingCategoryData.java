package de.adorsys.multibanking.domain;

import java.util.List;

import domain.BankAccount;
import domain.BookingCategory;
import lombok.Data;

@Data
public class BookingCategoryData {
	private List<BookingCategory> expenceCategory ;
	private List<BookingCategory> incomeCategory ;
	private List<BookingCategory> insuranceCategory ;
	private List<BankAccount> otherAccounts ;
}
