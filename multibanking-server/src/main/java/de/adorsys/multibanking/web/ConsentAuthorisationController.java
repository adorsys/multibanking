package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.service.ConsentService;
import de.adorsys.multibanking.service.OAuthService;
import de.adorsys.multibanking.web.mapper.ConsentAuthorisationMapper;
import de.adorsys.multibanking.web.model.SelectPsuAuthenticationMethodRequestTO;
import de.adorsys.multibanking.web.model.TransactionAuthorisationRequestTO;
import de.adorsys.multibanking.web.model.UpdateAuthResponseTO;
import de.adorsys.multibanking.web.model.UpdatePsuAuthenticationRequestTO;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iban4j.Iban;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static de.adorsys.multibanking.domain.BankApi.HBCI;
import static de.adorsys.multibanking.domain.ScaApproach.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Api(tags = "Multibanking consent authorisation")
@RequiredArgsConstructor
@Slf4j
@UserResource
@RestController
@RequestMapping(path = "api/v1/consents/{consentId}/authorisations/{authorisationId}")
public class ConsentAuthorisationController {

    private final ConsentService consentService;
    private final OAuthService oAuthService;
    private final ConsentAuthorisationMapper consentAuthorisationMapper;

    @ApiOperation(value = "Update authorisation (authenticate user)")
    @PutMapping("/updatePsuAuthentication")
    public ResponseEntity<Resource<UpdateAuthResponseTO>> updateAuthentication(@PathVariable String consentId,
                                                                               @PathVariable String authorisationId,
                                                                               @RequestBody @Valid UpdatePsuAuthenticationRequestTO updatePsuAuthenticationRequestTO) {
        String bankCode = Iban.valueOf(consentService.getInternalConsent(consentId).getPsuAccountIban()).getBankCode();

        UpdateAuthResponse updateAuthResponse =
            consentService.updatePsuAuthentication(updatePsuAuthenticationRequestTO, consentId);

        return ResponseEntity.ok(mapToResource(updateAuthResponse,
            consentId, authorisationId, bankCode));
    }

    @ApiOperation(value = "Update authorisation (select SCA method)")
    @PutMapping("/selectPsuAuthenticationMethod")
    public ResponseEntity<Resource<UpdateAuthResponseTO>> selectAuthenticationMethod(@PathVariable String consentId,
                                                                                     @PathVariable String authorisationId,
                                                                                     @RequestBody @Valid SelectPsuAuthenticationMethodRequestTO selectPsuAuthenticationMethodRequest) {
        String bankCode = Iban.valueOf(consentService.getInternalConsent(consentId).getPsuAccountIban()).getBankCode();

        UpdateAuthResponse updateAuthResponse =
            consentService.selectPsuAuthenticationMethod(selectPsuAuthenticationMethodRequest, consentId);

        return ResponseEntity.ok(mapToResource(updateAuthResponse, consentId, authorisationId, bankCode));
    }

    @ApiOperation(value = "Update authorisation (authorize transaction)")
    @PutMapping("/transactionAuthorisation")
    public ResponseEntity<Resource<UpdateAuthResponseTO>> transactionAuthorisation(@PathVariable String consentId,
                                                                                   @PathVariable String authorisationId,
                                                                                   @RequestBody @Valid TransactionAuthorisationRequestTO transactionAuthorisationRequest) {
        String bankCode = Iban.valueOf(consentService.getInternalConsent(consentId).getPsuAccountIban()).getBankCode();

        UpdateAuthResponse updateAuthResponse =
            consentService.authorizeConsent(transactionAuthorisationRequest, consentId);

        return ResponseEntity.ok(mapToResource(updateAuthResponse, consentId, authorisationId, bankCode));
    }

    @ApiOperation(value = "Get consent authorisation status")
    @GetMapping()
    public ResponseEntity<Resource<UpdateAuthResponseTO>> getConsentAuthorisationStatus(@PathVariable String consentId,
                                                                                        @PathVariable String authorisationId) {
        String bankCode = Iban.valueOf(consentService.getInternalConsent(consentId).getPsuAccountIban()).getBankCode();

        UpdateAuthResponse updateAuthResponse = consentService.getAuthorisationStatus(consentId,
            authorisationId);

        return ResponseEntity.ok(mapToResource(updateAuthResponse, consentId, authorisationId, bankCode));
    }

    @ApiOperation(value = "submit oauth authorization code")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Response", response = UpdateAuthResponseTO.class)
    })
    @PostMapping("/submitOAuthCode")
    public ResponseEntity submitAuthorizationCode(@Valid @RequestBody AuthorizationCode authorizationCode,
                                                  @PathVariable("consentId") String consentId,
                                                  @PathVariable("authorisationId") String authorisationId) {
        oAuthService.submitAuthCode(consentId, authorisationId, authorizationCode.getCode());
        return ResponseEntity.ok().build();
    }

    private Resource<UpdateAuthResponseTO> mapToResource(UpdateAuthResponse response, String consentId,
                                                         String authorisationId, String bankCode) {
        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(ConsentAuthorisationController.class).getConsentAuthorisationStatus(consentId,
            authorisationId)).withSelfRel());
        links.add(linkTo(methodOn(BankController.class).getBank(bankCode)).withRel("bank"));

        if (response.getScaApproach() != REDIRECT && response.getScaApproach() != OAUTH) {
            appendEmbeddedAuthLinks(response, consentId, authorisationId, links);
        }

        return new Resource<>(consentAuthorisationMapper.toUpdateAuthResponseTO(response), links);
    }

    private void appendEmbeddedAuthLinks(UpdateAuthResponse response, String consentId, String authorisationId,
                                         List<Link> links) {
        switch (response.getScaStatus()) {
            case RECEIVED:
            case STARTED:
            case PSUIDENTIFIED:
                links.add(linkTo(methodOn(ConsentAuthorisationController.class).updateAuthentication(consentId,
                    authorisationId, null)).withRel("updateAuthentication"));
                break;
            case PSUAUTHENTICATED:
                links.add(linkTo(methodOn(ConsentAuthorisationController.class).selectAuthenticationMethod(consentId,
                    authorisationId, null)).withRel("selectAuthenticationMethod"));
                break;
            case SCAMETHODSELECTED:
                if (response.getScaApproach() == EMBEDDED && response.getBankApi() != HBCI) {
                    links.add(linkTo(methodOn(ConsentAuthorisationController.class).transactionAuthorisation(consentId,
                        authorisationId, null)).withRel("transactionAuthorisation"));
                }
                break;
            case FINALISED:
            case FAILED:
            case EXEMPTED:
                break;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthorizationCode {
        @NotNull
        @ApiModelProperty("Authorization Code")
        private String code;
    }
}
