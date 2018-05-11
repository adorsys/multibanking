package de.adorsys.multibanking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.util.Assert.isInstanceOf;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import de.adorsys.multibanking.config.service.BaseServiceTest;
import de.adorsys.multibanking.domain.BankAccessData;
import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountData;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.exception.InvalidBankAccessException;
import de.adorsys.multibanking.exception.InvalidPinException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.service.base.SystemObjectService;
import de.adorsys.multibanking.service.base.UserObjectService;
import de.adorsys.multibanking.service.old.TestConstants;
import de.adorsys.multibanking.service.old.TestUtil;
import de.adorsys.multibanking.service.producer.OnlineBankingServiceProducer;
import de.adorsys.onlinebanking.mock.MockBanking;
import figo.FigoBanking;

@RunWith(SpringRunner.class)
public class BankAccessServiceBlankTest extends BaseServiceTest {

    @MockBean
    protected FigoBanking figoBanking;
    @MockBean
    protected MockBanking mockBanking;
    @MockBean
    protected OnlineBankingServiceProducer bankingServiceProducer;
    @Autowired
    private BankAccessService bankAccessService;
    @Autowired
    private UserDataService uds;
	@Autowired
	private UserObjectService uos;
	@Autowired
	private SystemObjectService sos;

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @BeforeClass
    public static void beforeClass() {
    	TestConstants.setup();
    }

    @Before
    public void beforeTest() throws Exception {
    	MockitoAnnotations.initMocks(this);
    	sos.enableCaching();
    	uos.enableCaching();
        when(bankingServiceProducer.getBankingService(anyString())).thenReturn(mockBanking);
    	randomAuthAndUser();
    	importBanks();
    }
    
    @After
    public void after() throws Exception{
    	sos.flush();
    	uos.flush();

    	if(userContext!=null)
    		rcMap.put(userContext.getAuth().getUserID().getValue()+ ":"+testName.getMethodName(), userContext.getRequestCounter());
    	if(systemContext!=null)
    		rcMap.put(systemContext.getUser().getAuth().getUserID().getValue()+ ":"+testName.getMethodName(), systemContext.getUser().getRequestCounter());
    }
    

    /**
     * Creates a bank access with a non existing bank code.
     * 
     */
    @Test
    public void create_bank_access_not_supported() {
        when(mockBanking.bankSupported(anyString())).thenReturn(false);
        thrown.expect(InvalidBankAccessException.class);
        
        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity(userId(), 
        		randomAccessId(), "unsupported", "0000");
        bankAccessEntity.setBankCode("unsupported");
        // "testUserId", 
        bankAccessService.createBankAccess(bankAccessEntity);
    }

    @Test
    public void create_bank_access_no_accounts() {
        when(mockBanking.bankSupported(anyString())).thenReturn(true);
        thrown.expect(InvalidBankAccessException.class);

        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity(userId(), randomAccessId(), "29999999", "0000");
        // "testUserId", 
        bankAccessService.createBankAccess(bankAccessEntity);
    }

    @Test
    public void create_bank_access_invalid_pin() {
    	
    	// Mock bank access
        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity(userId(), randomAccessId(), "29999999", "0000");

        when(mockBanking.bankSupported(anyString())).thenReturn(true);
        when(mockBanking.loadBankAccounts(any(), any(), anyString(), anyString(), anyBoolean()))
                .thenThrow(new InvalidPinException(bankAccessEntity.getId()));
        thrown.expect(InvalidPinException.class);

        // "testUserId", 
        bankAccessService.createBankAccess(bankAccessEntity);
    }

    @Test
    public void create_bank_access_ok() {

        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity(userId(), randomAccessId(), "29999999", "0000");
        BankAccountEntity bankAccountEntity = TestUtil.getBankAccountEntity(bankAccessEntity, randomAccountId());
        
        when(mockBanking.bankSupported(anyString())).thenReturn(true);
        when(mockBanking.loadBankAccounts(any(), any(), anyString(), anyString(), anyBoolean()))
                .thenReturn(Arrays.asList(bankAccountEntity));

        bankAccessService.createBankAccess(bankAccessEntity);
        isInstanceOf(BankAccessData.class, uds.load().bankAccessData(bankAccessEntity.getId()));
        isInstanceOf(BankAccountData.class, uds.load().bankAccountData(bankAccessEntity.getId(), bankAccountEntity.getId()));
    }

    @Test
    public void when_delete_bankAcces_user_exist_should_return_false() {
        // userId, 
        boolean deleteBankAccess = bankAccessService.deleteBankAccess("access");
        assertThat(deleteBankAccess).isEqualTo(false);
    }

    @Test
    public void when_delete_bankAcces_user_exist_should_return_true() {
        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity(userId(), randomAccessId(), "29999999", "0000");
        BankAccountEntity bankAccountEntity = TestUtil.getBankAccountEntity(bankAccessEntity, randomAccountId());
        
        when(mockBanking.bankSupported(anyString())).thenReturn(true);
        when(mockBanking.loadBankAccounts(any(), any(), anyString(), anyString(), anyBoolean()))
                .thenReturn(Arrays.asList(bankAccountEntity));

    	bankAccessService.createBankAccess(bankAccessEntity);
        boolean deleteBankAccess = bankAccessService.deleteBankAccess(bankAccessEntity.getId());
        assertThat(deleteBankAccess).isEqualTo(true);
        thrown.expect(ResourceNotFoundException.class);
        isInstanceOf(BankAccessData.class, uds.load().bankAccessData(bankAccessEntity.getId()));
    }
}
