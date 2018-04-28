package de.adorsys.multibanking.utils;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import de.adorsys.multibanking.domain.common.IdentityIf;

public class Ids {
	public static final String uuid(){
		return UUID.randomUUID().toString();
	}
	
	public static boolean eq(String id1, String id2){
		return StringUtils.equalsAnyIgnoreCase(id1, id2);
	}

	public static <T extends IdentityIf> void id(IdentityIf n) {
		if(StringUtils.isBlank(n.getId()))
			n.setId(uuid());
		
	}

}
