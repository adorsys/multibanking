package de.adorsys.multibanking.service.old;

import java.lang.reflect.Field;
import java.security.Security;

import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.adorsys.multibanking.auth.SystemContext;
import de.adorsys.multibanking.auth.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConstants {
    private final static Logger LOGGER = LoggerFactory.getLogger(TestConstants.class);
	public static final void setup(){
//        turnOffEncPolicy();
        Security.addProvider(new BouncyCastleProvider());
	}

//	public static void turnOffEncPolicy(){
//		// Warning: do not do this for productive code. Download and install the jce unlimited strength policy file
//		// see http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
//		try {
//	        Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
//	        field.setAccessible(true);
//	        field.set(null, Boolean.FALSE);
//	    } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
//            LOGGER.warn("can not turn off Enc Policy");
//	    }
//	}

	private static SystemContext systemId = null;
	public static SystemContext getSystemUserIDAuth(){
		if(systemId!=null) return systemId;

		UserContext userContext = new UserContext();
		UserIDAuth userIDAuth = new UserIDAuth(new UserID("system"), new ReadKeyPassword("systemPassword123"));
		userContext.setAuth(userIDAuth);
		systemId = new SystemContext(userContext);
		return systemId;
	}
}
