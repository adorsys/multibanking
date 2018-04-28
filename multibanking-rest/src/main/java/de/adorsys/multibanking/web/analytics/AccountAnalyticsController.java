package de.adorsys.multibanking.web.analytics;

import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.multibanking.service.analytics.AnalyticsService;
import de.adorsys.multibanking.web.annotation.UserResource;
import de.adorsys.multibanking.web.common.BaseController;
import io.swagger.annotations.Api;

/**
 * Created by alexg on 07.02.17.
 */
@UserResource
@RestController
@RequestMapping(path = AccountAnalyticsController.BASE_PATH)
@Api(value = AccountAnalyticsController.BASE_PATH, 
	tags = "MB-008 - Analytics", description="Manages account and booking analytics.")
public class AccountAnalyticsController extends BaseController {
    private final static Logger LOGGER = LoggerFactory.getLogger(AccountAnalyticsController.class);
	public static final String BASE_PATH = "api/v1/bankaccesses/{accessId}/accounts/{accountId}/analytics"; 
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<ByteArrayResource> getAccountAnalytics(@PathVariable String accessId, @PathVariable String accountId) {
        LOGGER.info("Start getAccountAnalytics for " + userId() + " " + accessId + " " + accountId);
    	DSDocument dsDocument = analyticsService.loadDomainAnalytics(accessId,accountId);
        LOGGER.info("finished getAccountAnalytics for "  + userId() + " " + dsDocument.getDocumentFQN());
    	return loadBytesForWeb(dsDocument);
    }
}
