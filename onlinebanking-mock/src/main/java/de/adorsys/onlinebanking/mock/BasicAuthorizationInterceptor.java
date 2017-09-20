package de.adorsys.onlinebanking.mock;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;

import java.io.IOException;

public class BasicAuthorizationInterceptor  implements ClientHttpRequestInterceptor {

	private final String basicToken;

	public BasicAuthorizationInterceptor(String basicToken) {
		Assert.hasLength(basicToken, "basicToken must not be empty");
		this.basicToken = basicToken;
	}


	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {

		request.getHeaders().add("Authorization", "Basic " + basicToken);
		return execution.execute(request, body);
	}


}
