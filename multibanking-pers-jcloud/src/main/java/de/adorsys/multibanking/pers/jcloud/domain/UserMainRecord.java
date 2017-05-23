package de.adorsys.multibanking.pers.jcloud.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.UserEntity;
import lombok.Data;

@Data
public class UserMainRecord {
	private List<BankAccessEntity> bankAccesses = new ArrayList<BankAccessEntity>();
	private UserEntity userEntity;
	// bankAccessId, accountId
	private Map<String, List<BankAccountEntity>> bankAccountMap = new HashMap<>();
	
	// accountId, analytics
	private Map<String, AccountAnalyticsEntity> analytics = new HashMap<>();
	
}
