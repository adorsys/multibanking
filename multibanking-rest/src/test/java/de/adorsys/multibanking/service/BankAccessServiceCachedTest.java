package de.adorsys.multibanking.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import de.adorsys.multibanking.service.interceptor.CacheInterceptor;

@RunWith(SpringRunner.class)
public class BankAccessServiceCachedTest extends BankAccessServiceBlankTest {

	@Autowired
	private CacheInterceptor cacheInterceptor;
	
    @Before
    public void beforeTest() throws Exception {
    	MockitoAnnotations.initMocks(this);
        when(bankingServiceProducer.getBankingService(anyString())).thenReturn(mockBanking);
    	randomAuthAndUser();
    	when(userContext.isCacheEnabled()).thenReturn(true);
    	cacheInterceptor.preHandle(null, null, null);
    	importBanks();
    }
    
    @After
    public void after() throws Exception{
    	if(userContext!=null){
    		cacheInterceptor.postHandle(null, null, null, null);
    		super.after();
    	}
    	
    }
   
}
