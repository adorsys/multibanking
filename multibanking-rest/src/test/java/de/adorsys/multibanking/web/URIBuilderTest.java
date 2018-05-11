package de.adorsys.multibanking.web;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import de.adorsys.multibanking.web.account.BankAccessController;

public class URIBuilderTest {
	
	@Test
	public void testSimpleUriComponentsBuilder(){
		UriComponents uriComponents = UriComponentsBuilder.newInstance().path(BankAccessController.BASE_PATH).build();
		Assert.assertEquals(BankAccessController.BASE_PATH, uriComponents.toString());
	}

	@Test
	public void testTemplateUriComponentsBuilder(){
		UriComponents uriComponents = UriComponentsBuilder.fromPath(BankAccessController.BASE_PATH).path("/{access_id}").build();
		Assert.assertEquals(BankAccessController.BASE_PATH + "/{access_id}", uriComponents.toString());
	}
}
