package de.adorsys.multibanking.analytics.connector;

import java.util.Collections;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import de.adorsys.multibanking.exception.SmartanalyticsException;
import de.adorsys.smartanalytics.api.AnalyticsRequest;
import de.adorsys.smartanalytics.api.AnalyticsResult;
import de.adorsys.smartanalytics.api.SmartAnalyticsFacade;

/**
 * Connector to consume smartAnalytics installed on a separated server.
 * 
 * @author fpo 2018-05-06 06:02
 *
 */
public class RemoteSmartAnalyticsConnector implements SmartAnalyticsFacade {
	private RestTemplate smartanalyticsRestTemplate;

	public RemoteSmartAnalyticsConnector(RestTemplate smartanalyticsRestTemplate) {
		super();
		this.smartanalyticsRestTemplate = smartanalyticsRestTemplate;
	}

	@Override
	public AnalyticsResult analyzeBookings(AnalyticsRequest analyticsRequest) {
		ResponseEntity<Resource<AnalyticsResult>> responseEntity = smartanalyticsRestTemplate.exchange(
				"/api/v1/analytics", HttpMethod.PUT, new HttpEntity<Object>(analyticsRequest),
				new ParameterizedTypeReference<Resource<AnalyticsResult>>() {
				}, Collections.emptyMap());

		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			return responseEntity.getBody().getContent();
		}

		throw new SmartanalyticsException(responseEntity.getStatusCode(), null);
	}
}
