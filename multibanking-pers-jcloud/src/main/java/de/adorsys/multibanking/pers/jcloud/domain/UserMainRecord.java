package de.adorsys.multibanking.pers.jcloud.domain;

import java.util.List;
import java.util.Map;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.UserEntity;
import lombok.Data;

@Data
public class UserMainRecord {
	private List<BankAccessEntity> bankAccesses;
	private UserEntity userEntity;
	// bankAccessId, accountId
	private Map<String, List<BankAccountEntity>> bankAccountMap;
}
