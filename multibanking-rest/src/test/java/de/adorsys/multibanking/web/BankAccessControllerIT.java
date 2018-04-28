package de.adorsys.multibanking.web;

import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.service.BankService;
import de.adorsys.multibanking.service.old.TestUtil;
import de.adorsys.multibanking.utils.Ids;
import de.adorsys.multibanking.web.account.BankAccessController;
import de.adorsys.multibanking.web.base.BaseControllerIT;
import de.adorsys.multibanking.web.base.PasswordGrantResponse;

@RunWith(SpringRunner.class)
//@Ignore
public class BankAccessControllerIT extends BaseControllerIT {

    @Autowired
    private BankService bankService;
    
    private String userId = Ids.uuid();
    private String password = Ids.uuid();
    private PasswordGrantResponse resp;
	
    @Before
    public void setup() throws Exception {
    	resp = auth(userId, password);
    	Assume.assumeFalse(StringUtils.isEmpty(resp.getAccessToken()));
    	Assume.assumeTrue("Bearer".equals(resp.getTokenType()));
    	
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("mock_bank.json");
		bankService.importBanks(inputStream);
		Optional<BankEntity> bankEntity = bankService.findByBankCode("19999999");
		Assume.assumeTrue(bankEntity.isPresent());
    }
    
	@Test
	public void testCreateBankaccess201() throws Exception {
		
		BankAccessEntity be = new BankAccessEntity();
		be.setBankCode("19999999");
		be.setBankLogin("m.becker");
		be.setPin("12345");
//		= TestUtil.getBankAccessEntity(null, null, "19999999", "12345");
    	URI uri = bankAccessPath().build().toUri();
    	
        URI location = testRestTemplate.postForLocation(uri, be);
        String userData = testRestTemplate.getForObject(location, String.class);
        Assert.assertNotNull(userData);
	}

	private UriComponentsBuilder bankAccessPath() {
		return path(BankAccessController.BASE_PATH);
	}
}
