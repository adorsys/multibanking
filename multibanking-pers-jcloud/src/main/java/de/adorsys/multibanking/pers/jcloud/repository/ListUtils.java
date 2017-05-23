package de.adorsys.multibanking.pers.jcloud.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.pers.jcloud.domain.UserMainRecord;

public class ListUtils {

	public static BankAccountEntity find(UserMainRecord userMainRecord, String accountId) {
		if(userMainRecord==null || userMainRecord.getBankAccountMap()==null) return null;
		Map<String, List<BankAccountEntity>> bankAccountMap = userMainRecord.getBankAccountMap();
		Collection<List<BankAccountEntity>> values = bankAccountMap.values();
		for (List<BankAccountEntity> list : values) {
			if(list==null) continue;
			for (BankAccountEntity bankAccountEntity : list) {
				if(bankAccountEntity==null) continue;
				if(StringUtils.equals(accountId, bankAccountEntity.getId())) return bankAccountEntity;
			}
		}
		return null;
	}


	public static <T> void add(T t, List<T> list, ListItemHandler<T> handler){
		if(handler.newId(t)){
			list.add(t);
		} else {
			int index = -1;
			for (int i = 0; i < list.size(); i++) {
				T exiting = list.get(i);
				if(exiting==null) continue;
				if(handler.idEquals(exiting, t)) {
					index = i;
					break;
				}
			}
			if(index<0){
				list.add(t);
			} else {
				list.set(index, t);
			}
		}
	}
}
