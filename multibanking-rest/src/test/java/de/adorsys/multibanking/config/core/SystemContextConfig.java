package de.adorsys.multibanking.config.core;

import de.adorsys.multibanking.auth.SystemContext;
import de.adorsys.multibanking.auth.UserContext;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Created by peter on 13.04.18 at 11:27.
 */
@Configuration
public class SystemContextConfig {
    private SystemContext systemContext;

    @Bean
    SystemContext systemContext() {
        return systemContext;
    }


    @Value("${docusafe.system.user.name}")
    String docusafeSystemUserName;
    @Value("${docusafe.system.user.password}")
    String docusafeSystemUserPassword;

    @PostConstruct
    public void postConstruct() {
        UserContext userContext = new UserContext();
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(docusafeSystemUserName), new ReadKeyPassword(docusafeSystemUserPassword));
        userContext.setAuth(userIDAuth);
        systemContext = new SystemContext(userContext);
    }

}
