package de.adorsys.multibanking.service.base;

import org.adorsys.docusafe.business.exceptions.UserIDAlreadyExistsException;
import org.adorsys.docusafe.business.exceptions.UserIDDoesNotExistException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.nimbusds.jose.jwk.JWK;

import de.adorsys.multibanking.config.service.BaseServiceTest;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.utils.Ids;

@RunWith(SpringRunner.class)
public class StorageUserServiceTest extends BaseServiceTest {

	@Autowired
	private StorageUserService storageUserService;

    @Test
	public void testCreateUserAndCheckUserExists() {
    	auth(Ids.uuid(), Ids.uuid(), false);
    	storageUserService.createUser(auth());
    	Assert.assertTrue(storageUserService.userExists(auth().getUserID()));
	}

	@Test
	public void testUserExists_false() {
    	auth(Ids.uuid(), Ids.uuid(), false);
    	Assert.assertFalse(storageUserService.userExists(auth().getUserID()));
	}

	@Test(expected=UserIDAlreadyExistsException.class)
	public void testCreateUserAgain() {
    	auth(Ids.uuid(), Ids.uuid(), false);
    	storageUserService.createUser(auth());
    	Assume.assumeTrue(storageUserService.userExists(auth().getUserID()));
    	storageUserService.createUser(auth());
	}

	@Test
	public void testDeleteUser() {
    	auth(Ids.uuid(), Ids.uuid(), false);
    	storageUserService.createUser(auth());
    	Assume.assumeTrue(storageUserService.userExists(auth().getUserID()));
    	storageUserService.deleteUser(auth());
    	Assert.assertFalse(storageUserService.userExists(auth().getUserID()));
	}

	@Test(expected=UserIDDoesNotExistException.class)
	public void testDeleteUserAgain() {
    	auth(Ids.uuid(), Ids.uuid(), false);
    	storageUserService.createUser(auth());
    	Assume.assumeTrue(storageUserService.userExists(auth().getUserID()));
    	storageUserService.deleteUser(auth());
    	Assume.assumeFalse(storageUserService.userExists(auth().getUserID()));
    	storageUserService.deleteUser(auth());
	}

	@Test
	public void testFindPublicEncryptionKey() {
    	auth(Ids.uuid(), Ids.uuid(), false);
    	storageUserService.createUser(auth());
    	JWK jwk = storageUserService.findPublicEncryptionKey(auth().getUserID());
    	Assert.assertNotNull(jwk);
	}

	@Test (expected = ResourceNotFoundException.class)
	public void testFindPublicEncryptionKeyWrongUser() {
            auth(Ids.uuid(), Ids.uuid(), false);
            storageUserService.findPublicEncryptionKey(auth().getUserID());
	}

}
