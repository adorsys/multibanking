package de.adorsys.multibanking.web;

import java.io.InputStream;
import java.util.List;

import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import de.adorsys.multibanking.config.mock.SimpleMockBanking;
import de.adorsys.multibanking.config.web.WebMvcUnitTest;
import de.adorsys.multibanking.service.BankAccessService;
import de.adorsys.multibanking.service.BankAccountService;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.utils.FQNUtils;
import de.adorsys.multibanking.web.account.BookingController;
import de.adorsys.multibanking.web.base.BaseControllerUnitTest;
import domain.BankAccess;
import domain.BankAccount;
import domain.Booking;
import domain.LoadBookingsResponse;

@WebMvcUnitTest(controllers = BookingController.class)
public class BookingControllerTest extends BaseControllerUnitTest {
    @InjectMocks
    private BookingController bookingController;
    @MockBean
    private BankAccountService bankAccountService;
    @MockBean
    private BankAccessService bankAccessService;
    @MockBean
    private BookingService bookingService;

    private String bookingsStr;
    private DSDocument dsDocument;
	private String bankAccessId = "5a998c0c7077e800014ca672";
	private String accountId = "5a998c0c7077e800014ca673";
	private String period = "ALL";
	private String query = basePath().queryParam("period",period).build().toString();		
    
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
		
        InputStream bookingsStream = SimpleMockBanking.class.getClassLoader().getResourceAsStream("/BookingControllerTest/test_data.xls");
		SimpleMockBanking simpleMockBanking = new SimpleMockBanking(null, null, bookingsStream);
		BankAccess bankAccess = new BankAccess();
		bankAccess.setBankLogin("m.becker");
		BankAccount bankAccount = new BankAccount();
		bankAccount.setIban("DE81199999993528307800");
		LoadBookingsResponse bookingsResponse = simpleMockBanking.loadBookings(null, bankAccess, null, bankAccount, null);
		List<Booking> bookings = bookingsResponse.getBookings();
		bookingsStr = mapper.writeValueAsString(bookings);
		
        DocumentContent documentContent = new DocumentContent(bookingsStr.getBytes("UTF-8"));
		dsDocument = new DSDocument(FQNUtils.banksFQN(), documentContent, null);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testGetBookings200() throws Exception {
		BDDMockito.when(bankAccessService.exists(bankAccessId)).thenReturn(true);
		BDDMockito.when(bankAccountService.exists(bankAccessId, accountId)).thenReturn(true);
		BDDMockito.when(bankAccountService.getSyncStatus(bankAccessId, accountId)).thenReturn(BankAccount.SyncStatus.READY);
		BDDMockito.when(bookingService.getBookings(bankAccessId, accountId, period)).thenReturn(dsDocument);

        mockMvc.perform(MockMvcRequestBuilders.get(query, bankAccessId, accountId)
        		.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.content().json(bookingsStr));
	}

	@Test
	public void testGetBookings404BankAccess() throws Exception {
		BDDMockito.when(bankAccessService.exists(bankAccessId)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get(query, bankAccessId, accountId)
        		.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void testGetBookings404BankAccount() throws Exception {
		BDDMockito.when(bankAccessService.exists(bankAccessId)).thenReturn(true);
		BDDMockito.when(bankAccountService.exists(bankAccessId, accountId)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get(query, bankAccessId, accountId)
        		.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void testGetBookings102OngoingSynch() throws Exception {
		BDDMockito.when(bankAccessService.exists(bankAccessId)).thenReturn(true);
		BDDMockito.when(bankAccountService.exists(bankAccessId, accountId)).thenReturn(true);
		BDDMockito.when(bankAccountService.getSyncStatus(bankAccessId, accountId)).thenReturn(BankAccount.SyncStatus.SYNC);

        mockMvc.perform(MockMvcRequestBuilders.get(query, bankAccessId, accountId)
        		.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isProcessing());
	}

	/* *******  URLS *******/
    private static final UriComponentsBuilder basePath(){
    	return UriComponentsBuilder.fromPath(BookingController.BASE_PATH);
    }
}
