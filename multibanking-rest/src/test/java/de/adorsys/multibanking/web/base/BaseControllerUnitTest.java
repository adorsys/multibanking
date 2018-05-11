package de.adorsys.multibanking.web.base;

import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.multibanking.auth.CacheEntry;
import de.adorsys.multibanking.auth.RequestCounter;
import de.adorsys.multibanking.auth.SystemContext;
import de.adorsys.multibanking.auth.UserContext;
import de.adorsys.multibanking.config.web.ControllerUnitTestConfig;
import de.adorsys.multibanking.service.old.TestConstants;
import de.adorsys.multibanking.web.user.UserDataController;

/**
 * Base class for unit testing controllers.
 * 
 * @author fpo
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ControllerUnitTestConfig.class)
public abstract class BaseControllerUnitTest {
	
	/*
	 * used to load json test data.
	 */
	@Autowired
    protected ObjectMapper mapper;
	
    @MockBean
    protected UserContext userContext;
    
    @Autowired
    protected SystemContext systemContext;
    
    protected MockMvc mockMvc;    

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
    	TestConstants.setup();		
	}

    protected void auth(String userId, String password){
    	UserIDAuth userIDAuth = new UserIDAuth(new UserID(userId), new ReadKeyPassword(password));
    	RequestCounter requestCounter = new RequestCounter();
    	Map<Type, Map<DocumentFQN, CacheEntry<?>>> cache = new HashMap<>();
    	when(userContext.getAuth()).thenReturn(userIDAuth);
    	when(userContext.getRequestCounter()).thenReturn(requestCounter);
    	when(userContext.getCache()).thenReturn(cache);
    	
    }

    protected final UriComponentsBuilder userDataBasePath(){
    	return UriComponentsBuilder.fromPath(UserDataController.BASE_PATH);
    }

}
