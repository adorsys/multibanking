package de.adorsys.multibanking.web;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import de.adorsys.multibanking.config.web.ControllerUnitTestConfig;
import de.adorsys.multibanking.config.web.WebMvcUnitTest;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BankAccountData;
import de.adorsys.multibanking.service.BankAccessService;
import de.adorsys.multibanking.service.BankAccountService;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.web.account.BankAccountController;
import de.adorsys.multibanking.web.base.BaseControllerUnitTest;
import domain.BankAccount.SyncStatus;

@WebMvcUnitTest(controllers = BankAccountController.class)
@ContextConfiguration(classes={ControllerUnitTestConfig.class})
public class BankAccountControllerTest extends BaseControllerUnitTest {

    @InjectMocks
    private BankAccountController bankAccountController;
    @MockBean
    private BankAccountService bankAccountService;
    @MockBean
    private BankAccessService bankAccessService;
    @MockBean
    private BookingService bookingService;

	private String bankAccessId = "5a998c0c7077e800014ca672";
	private String accountId = "5a998c0c7077e800014ca673";

    @Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(bankAccountController).build();
	}

	@Test
	public void testSyncBookings404BadBankAccount() throws Exception {
		BDDMockito.when(bankAccessService.exists(bankAccessId)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.put(idPath().path("/sync").build().toString(), bankAccessId, accountId)
        		.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}
	
	@Test
	public void testSyncBookings102OngoingSynch() throws Exception {
		BDDMockito.when(bankAccessService.exists(bankAccessId)).thenReturn(true);
		BDDMockito.when(bankAccountService.exists(bankAccessId, accountId)).thenReturn(true);
		BankAccountData bankAccountData = new BankAccountData();
		bankAccountData.setSyncStatusTime(LocalDateTime.now());
		bankAccountData.setBankAccount(new BankAccountEntity());
		bankAccountData.getBankAccount().setSyncStatus(SyncStatus.SYNC);
		BDDMockito.when(bankAccountService.loadBankAccount(bankAccessId, accountId)).thenReturn(bankAccountData);
        mockMvc.perform(MockMvcRequestBuilders.put(idPath().path("/sync").build().toString(), bankAccessId, accountId)
        		.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(MockMvcResultMatchers.status().isProcessing());
	}
	
	@Test
	public void testSyncBookings200() throws Exception {
		BDDMockito.when(bankAccessService.exists(bankAccessId)).thenReturn(true);
		BDDMockito.when(bankAccountService.exists(bankAccessId, accountId)).thenReturn(true);
		BankAccountData bankAccountData = new BankAccountData();
		bankAccountData.setSyncStatusTime(LocalDateTime.now());
		bankAccountData.setBankAccount(new BankAccountEntity());
		bankAccountData.getBankAccount().setSyncStatus(SyncStatus.READY);
		BDDMockito.when(bankAccountService.loadBankAccount(bankAccessId, accountId)).thenReturn(bankAccountData);
        mockMvc.perform(MockMvcRequestBuilders.put(idPath().path("/sync").build().toString(), bankAccessId, accountId)
        		.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(MockMvcResultMatchers.status().isNoContent());
	}

	/* *******  URLS *******/
    private static final UriComponentsBuilder basePath(){
    	return UriComponentsBuilder.fromPath(BankAccountController.BASE_PATH);
    }
    private static final UriComponentsBuilder idPath(){
    	return basePath().path("/{accountId}");
    }
}
