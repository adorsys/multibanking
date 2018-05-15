package de.adorsys.multibanking.config.service;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import de.adorsys.multibanking.auth.CacheEntry;
import de.adorsys.multibanking.auth.RequestCounter;
import de.adorsys.multibanking.auth.SystemContext;
import de.adorsys.multibanking.auth.UserContext;
import de.adorsys.multibanking.service.BankService;
import de.adorsys.multibanking.service.UserDataService;
import de.adorsys.multibanking.service.base.StorageUserService;
import de.adorsys.multibanking.service.old.TestConstants;
import de.adorsys.multibanking.utils.Ids;
import de.adorsys.multibanking.utils.PrintMap;

@ActiveProfiles({"InMemory"})
@SpringBootTest(properties={Tp.p1,Tp.p2,Tp.p3,Tp.p4,Tp.p5,Tp.p6,Tp.p7,Tp.p8,Tp.p9,Tp.p10,Tp.p11,Tp.p12,
		Tp.p13,Tp.p14,Tp.p15,Tp.p16,Tp.p17,Tp.p18,Tp.p19,Tp.p20,Tp.p21,Tp.p22,Tp.p23,
		Tp.p24,Tp.p25,Tp.p26,Tp.p27,Tp.p28,Tp.p29,Tp.p30,Tp.p31,Tp.p32,Tp.p33,Tp.p34,Tp.p35,Tp.p36,Tp.p37  })
public abstract class BaseServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(BaseServiceTest.class);

	@Autowired
    protected UserDataService uds;

    @Autowired
    private BankService bankService;
	
    @MockBean
    protected UserContext userContext;
    
    @Autowired
    protected SystemContext systemContext;
    
    @Autowired
    private StorageUserService storageUserService;
    
    @Rule
    public TestName testName = new TestName();
    
    protected static final Map<String, RequestCounter> rcMap = new HashMap<>();

    @BeforeClass
    public static void beforeClass() {
    	TestConstants.setup();
    }
    
    @AfterClass
    public static void afterClass(){
    	LOGGER.debug(PrintMap.print(rcMap));
    }
    
    protected void auth(String userId, String password){
    	auth(userId, password, true);
    }
    protected void auth(String userId, String password, boolean createUser){
    	UserIDAuth userIDAuth = new UserIDAuth(new UserID(userId), new ReadKeyPassword(password));
    	if(createUser && !storageUserService.userExists(userIDAuth.getUserID()))
    		storageUserService.createUser(userIDAuth);
    	RequestCounter requestCounter = new RequestCounter();
    	Map<Type, Map<DocumentFQN, CacheEntry<?>>> cache = new HashMap<>();
    	when(userContext.getAuth()).thenReturn(userIDAuth);
    	when(userContext.getRequestCounter()).thenReturn(requestCounter);
    	when(userContext.getCache()).thenReturn(cache);
    }
    protected void randomAuthAndUser(){
    	randomAuthAndUser(null);
    }
    protected void randomAuthAndUser(Date expire){
    	auth(randomUserId(), allwaysTheSamePassword());
    	uds.createUser(expire);
    }
    
    boolean banksImported = false;
    protected void importBanks() throws IOException{
        if (!banksImported) {
        	InputStream inputStream = BaseServiceTest.class.getClassLoader().getResource("catalogue/banks/test-bank-catalogue.yml").openStream();
        	bankService.importBanks(inputStream);
        	IOUtils.closeQuietly(inputStream);
            banksImported = true;
        }
    }
    
    /**
     * For documentation purpose.
     * @return
     */
    protected final String userId(){
    	return userContext.getAuth().getUserID().getValue();
    }
    protected final UserIDAuth auth(){
    	return userContext.getAuth();
    }
    
    /**
     * For documentation purpose.
     * @return
     */
    protected final String randomAccessId(){
    	return Ids.uuid();
    }
    protected final String randomAccountId(){
    	return Ids.uuid();
    }

    private final String randomUserId(){
    	return Ids.uuid();
    }

    private final String allwaysTheSamePassword(){
        return "same-password-all-the-time";
//    	return Ids.uuid();
    }
}
