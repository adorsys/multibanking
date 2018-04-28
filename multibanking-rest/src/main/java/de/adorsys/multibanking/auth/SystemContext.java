package de.adorsys.multibanking.auth;

import lombok.Data;

@Data
public class SystemContext {
	private UserContext user;

	public SystemContext(UserContext user) {
		this.user = user;
	}
	
}
