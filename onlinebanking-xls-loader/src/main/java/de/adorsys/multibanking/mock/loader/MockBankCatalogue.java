package de.adorsys.multibanking.mock.loader;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import domain.Bank;
import lombok.Data;

@Data
public class MockBankCatalogue {

	List<? extends Bank> banks = Collections.emptyList();

	public Optional<? extends Bank> getBank(String bankcode){
		return  this.banks.stream()
				.filter(b -> bankcode.equals(b.getBankCode()))
				.findFirst();
	}

}
