package de.adorsys.multibanking.bg;

import de.adorsys.multibanking.banking_gateway_b2c.model.ConsentTO;
import de.adorsys.multibanking.banking_gateway_b2c.model.CreateConsentResponseTO;
import de.adorsys.xs2a.adapter.model.ConsentStatusTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "bankinggateway-client", url = "${bankinggateway.base.url}")
public interface BankingGatewayClient {

    String CONSENTS = "/v1/ais/consents";
    String GET_CONSENT_STATUS_BY_ID_URI = CONSENTS + "/{consentId}/status";
    String GET_CONSENT_BY_ID_URI = CONSENTS + "/{consentId}";

    @GetMapping(value = GET_CONSENT_STATUS_BY_ID_URI)
    public ResponseEntity<ConsentStatusTO> getConsentStatus(@PathVariable String consentId);

    @GetMapping(value = BankingGatewayClient.GET_CONSENT_BY_ID_URI, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ConsentTO> getConsent(@PathVariable String consentId);

    @PostMapping(path = BankingGatewayClient.CONSENTS, produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<CreateConsentResponseTO> createConsent(@RequestHeader(value = "PSU-ID", required = false) String psuId,
                                                          @RequestBody ConsentTO consentTO);
}
