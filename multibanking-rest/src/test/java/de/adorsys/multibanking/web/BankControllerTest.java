package de.adorsys.multibanking.web;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.apache.commons.io.IOUtils;
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

import de.adorsys.multibanking.config.web.WebMvcUnitTest;
import de.adorsys.multibanking.service.BankService;
import de.adorsys.multibanking.utils.FQNUtils;
import de.adorsys.multibanking.web.banks.BankController;
import de.adorsys.multibanking.web.base.BaseControllerUnitTest;

@WebMvcUnitTest(controllers = BankController.class)
public class BankControllerTest extends BaseControllerUnitTest {
    @InjectMocks
    private BankController bankController;
    @MockBean
	BankService bankService;

    private String banksStr;
    private DSDocument dsDocument;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(bankController).build();
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("BankControllerTest/mock_bank.json");
        banksStr = IOUtils.toString(stream, Charset.forName("UTF-8"));
        DocumentContent documentContent = new DocumentContent(banksStr.getBytes("UTF-8"));
		dsDocument = new DSDocument(FQNUtils.banksFQN(), documentContent, null);
	}

	@Test
	public void testLoadBank() throws Exception {
		BDDMockito.when(bankService.loadDocument()).thenReturn(dsDocument);
        mockMvc.perform(MockMvcRequestBuilders.get(basePath().build().toString())
                .accept(MediaType.APPLICATION_JSON_VALUE))
        		.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.content().json(banksStr));
	}
	
	/* *******  URLS *******/
    private static final UriComponentsBuilder basePath(){
    	return UriComponentsBuilder.fromPath(BankController.BASE_PATH);
    }
}
