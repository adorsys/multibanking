package de.adorsys.multibanking.service;

import static org.mockito.Mockito.when;

import java.io.IOException;

import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import de.adorsys.multibanking.service.old.TestConstants;

@RunWith(SpringRunner.class)
@ActiveProfiles({"InMemory"})
public class UserIdTest {

    @MockBean
    UserIDAuth userIdAuth;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() {
    	TestConstants.setup();
    }

    @Before
    public void beforeTest() throws IOException {
    	when(userIdAuth.getUserID()).thenReturn(new UserID("sample"));
    	when(userIdAuth.getReadKeyPassword()).thenReturn(new ReadKeyPassword("readKeyPassword123"));
    }

    @Test
    public void userIdAuth_userId() {
    	Assert.assertEquals("sample", userIdAuth.getUserID().getValue());
    }

}
