package de.adorsys.multibanking.web;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import de.adorsys.multibanking.config.web.WebMvcUnitTest;
import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.exception.BankAccessAlreadyExistException;
import de.adorsys.multibanking.service.BankAccessService;
import de.adorsys.multibanking.utils.Ids;
import de.adorsys.multibanking.web.account.BankAccessController;
import de.adorsys.multibanking.web.base.BaseControllerUnitTest;

@WebMvcUnitTest(controllers = BankAccessController.class)
public class BankAccessControllerTest extends BaseControllerUnitTest {
        
    @InjectMocks
    private BankAccessController bankAccessController;

    @MockBean
    private BankAccessService bankAccessService;
        
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(bankAccessController).build();
	}
	
	@Test
	public void testCreateBankaccess201() throws Exception {
    	BankAccessEntity newBankAccess = newBankAccess();
        newBankAccess.setId(Ids.uuid());

        BDDMockito.when(bankAccessService.createBankAccess(newBankAccess)).thenReturn(newBankAccess);
        String uuid = Ids.uuid();
        // Set user context.
        auth(uuid, Ids.uuid());

        String newBankAccessJson = mapper.writeValueAsString(newBankAccess);

		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(basePath().build().toString())
					.contentType(MediaType.APPLICATION_JSON).content(newBankAccessJson)
                .accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(MockMvcResultMatchers.status().isCreated())
        		.andReturn();
		
		String locationHeader = mvcResult.getResponse().getHeader("Location");
		Assert.assertTrue(StringUtils.endsWith(locationHeader, userDataBasePath().build().toString()));
	}

	@Test
	public void testCreateBankaccess409() throws Exception {
    	BankAccessEntity newBankAccess = newBankAccess();
        newBankAccess.setId(Ids.uuid());

        BDDMockito.when(bankAccessService.createBankAccess(newBankAccess)).thenThrow(new BankAccessAlreadyExistException(newBankAccess.getId()));
        String newBankAccessJson = mapper.writeValueAsString(newBankAccess);

		mockMvc.perform(MockMvcRequestBuilders.post(basePath().build().toString()).contentType(MediaType.APPLICATION_JSON).content(newBankAccessJson)
                .accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(MockMvcResultMatchers.status().isConflict());
	}
	
	@Test
	public void testDeleteBankAccess204() throws Exception {
        String accessId = Ids.uuid();
		BDDMockito.when(bankAccessService.deleteBankAccess(accessId)).thenReturn(true);
		mockMvc.perform(MockMvcRequestBuilders.delete(idPath().build(accessId).toString())
                .accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(MockMvcResultMatchers.status().isNoContent());
	}

	@Test
	public void testDeleteBankAccess410() throws Exception {
        String accessId = Ids.uuid();
		BDDMockito.when(bankAccessService.deleteBankAccess(accessId)).thenReturn(false);
		mockMvc.perform(MockMvcRequestBuilders.delete(idPath().build(accessId).toString())
                .accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(MockMvcResultMatchers.status().isGone());
	}
	
	private BankAccessEntity newBankAccess(){
    	BankAccessEntity newBankAccess = new BankAccessEntity();
        newBankAccess.setBankCode("19999999");
        newBankAccess.setBankLogin("adsfdsfad");
        newBankAccess.setPin("12345");
        newBankAccess.setBankName("Mock");
        return newBankAccess;
	}
	
	/* *******  URLS *******/
    private static final UriComponentsBuilder basePath(){
    	return UriComponentsBuilder.fromPath(BankAccessController.BASE_PATH);
    }
    private static final UriComponentsBuilder idPath(){
    	return basePath().path("/{accessId}");
    }
}
