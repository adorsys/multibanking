package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.service.bankinggateway.BankingGatewayAuthorisationService;
import de.adorsys.multibanking.web.mapper.ConsentMapper;
import de.adorsys.multibanking.web.model.ConsentTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Api(tags = "Multibanking consent")
@RequiredArgsConstructor
@Slf4j
@UserResource
@RestController
@RequestMapping(path = "api/v1/consents")
public class ConsentController {

    private final BankingGatewayAuthorisationService authorisationService;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final ConsentMapper consentMapper;
    private final Principal principal;

    @ApiOperation(
        value = "Create new consent",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @PostMapping
    public ResponseEntity<Resource<ConsentTO>> createConsent(@RequestBody ConsentTO consent) {
        Consent consentResponse = authorisationService.createConsent(consent);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(methodOn(ConsentController.class).getConsent(consentResponse.getConsentId())).toUri());

        return new ResponseEntity<>(mapToResource(consentResponse), headers, HttpStatus.CREATED);
    }

    @ApiOperation(
        value = "Read user consents",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @GetMapping
    public Resources<Resource<ConsentTO>> getConsents() {
        List<Consent> consents = bankAccessRepository.findByUserIdAndPsd2ConsentIdNotNull(principal.getName())
            .stream()
            .map(bankAccessEntity -> authorisationService.getConsent(bankAccessEntity.getPsd2ConsentId()))
            .collect(toList());

        return new Resources<>(mapToResources(consents));
    }

    @ApiOperation(
        value = "Read consent",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @GetMapping("/{consentId}")
    public Resource<ConsentTO> getConsent(@PathVariable("consentId") String consentId) {
        Consent consent = authorisationService.getConsent(consentId);

        return mapToResource(consent);
    }

    @ApiOperation(
        value = "Delete consent",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @DeleteMapping("/{consentId}")
    public HttpEntity<Void> deleteConsent(@PathVariable String consentId) {
        authorisationService.revokeConsent(consentId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private List<Resource<ConsentTO>> mapToResources(List<Consent> consents) {
        return consents.stream()
            .map(this::mapToResource)
            .collect(toList());
    }

    private Resource<ConsentTO> mapToResource(Consent consent) {
        return new Resource<>(consentMapper.toConsentTO(consent),
            linkTo(methodOn(ConsentController.class).getConsent(consent.getConsentId())).withSelfRel());
    }
}