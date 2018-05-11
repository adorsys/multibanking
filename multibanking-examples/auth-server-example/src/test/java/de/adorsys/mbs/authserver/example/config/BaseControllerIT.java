package de.adorsys.mbs.authserver.example.config;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class BaseControllerIT {
    public final static Logger LOGGER = LoggerFactory.getLogger(BaseControllerIT.class);

    @LocalServerPort
    private int port;
    
    @Autowired
    protected TestRestTemplate testRestTemplate;

    protected PasswordGrantResponse auth(String userId, String password){
    	URI uri = authPath()
		.queryParam("grant_type", "password")
		.queryParam("username", userId)
		.queryParam("password", password)
		.queryParam("audience", "multibanking")
		.build().toUri();
    	
    	PasswordGrantResponse resp = testRestTemplate.getForObject(uri, PasswordGrantResponse.class);

        final String accessTokenValue = resp.getAccessToken();

        testRestTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                	String authHeader = "Bearer " + accessTokenValue;
                    request.getHeaders().add("Authorization", authHeader);
                    return execution.execute(request, body);
                }));
        
        return resp;
    }
    
    protected void authenticateUserForFurtherRequests(String userName) {
    	auth(userName, UUID.randomUUID().toString());
    }

    protected final UriComponentsBuilder authPath(){
    	return path("/token/password-grant");
	}
    
    protected final UriComponentsBuilder path(String path){
    	return UriComponentsBuilder.fromUriString(getBaseUri()).path(path);
	}    
    
    protected String getBaseUri() {
        return "http://localhost:" + port;
    }
    
}
