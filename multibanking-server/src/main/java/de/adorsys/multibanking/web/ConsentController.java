package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.ConsentStatus;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.service.ConsentService;
import de.adorsys.multibanking.web.mapper.BankApiMapper;
import de.adorsys.multibanking.web.mapper.ConsentMapper;
import de.adorsys.multibanking.web.model.*;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iban4j.Iban;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Timed("consent")
@Tag(name = "Consent")
@RequiredArgsConstructor
@Slf4j
@UserResource
@RestController
@RequestMapping(path = "api/v1/consents")
public class ConsentController {

    private final ConsentService consentService;
    private final ConsentMapper consentMapper;
    private final BankApiMapper bankApiMapper;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final Principal principal;

    @Operation(description = "Create new consent")
    @PostMapping
    public ResponseEntity<EntityModel<CreateConsentResponseTO>> createConsent(@Valid @RequestBody ConsentTO consent,
                                                                           @RequestParam(required = false) BankApiTO bankApi) {
        Consent consentInput = consentMapper.toConsent(consent);
        CreateConsentResponse createConsentResponse = consentService.createConsent(consentInput,
            consent.getTppRedirectUri(),
            bankApiMapper.toBankApi(bankApi));

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(methodOn(ConsentController.class).getConsent(createConsentResponse.getConsentId())).toUri());

        return new ResponseEntity<>(mapToResource(createConsentResponse,
            Iban.valueOf(consentInput.getPsuAccountIban()).getBankCode()), headers, HttpStatus.CREATED);
    }

    @Operation(description = "Read user consents", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping
    public CollectionModel<EntityModel<ConsentTO>> getConsents() {
        List<Consent> consents = bankAccessRepository.findByUserId(principal.getName())
            .stream()
            .map(bankAccessEntity -> consentService.getConsent(bankAccessEntity.getConsentId()))
            .collect(toList());

        return CollectionModel.of(mapToResources(consents));
    }

    @Operation(description = "Read consent", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping("/{consentId}")
    public EntityModel<ConsentTO> getConsent(@PathVariable("consentId") String consentId) {
        return mapToResource(consentService.getConsent(consentId));
    }

    @Operation(description = "Read consent status", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping("/{consentId}/status")
    public EntityModel<ConsentStatusTO> getConsentStatus(@PathVariable("consentId") String consentId) {
        return mapToResource(consentService.getConsentStatus(consentId));
    }

    @Operation(description = "Read consent", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping("redirect/{redirectId}")
    public EntityModel<ConsentTO> getConsentByRedirectId(@PathVariable("redirectId") String redirectId) {
        return mapToResource(consentService.getConsentByRedirectId(redirectId));
    }

    @Operation(description = "Delete consent")
    @DeleteMapping("/{consentId}")
    public HttpEntity<Void> deleteConsent(@PathVariable String consentId) {
        consentService.revokeConsent(consentId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(description = "Submit OAuth2 authorisation code")
    @PostMapping("/{consentId}/token")
    public ResponseEntity<Void> submitAuthorisationCode(@PathVariable String consentId,
                                                        @RequestBody @Valid TokenRequestTO tokenRequest) {
        consentService.submitAuthorisationCode(consentId, tokenRequest.getAuthorisationCode());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private List<EntityModel<ConsentTO>> mapToResources(List<Consent> consents) {
        return consents.stream()
            .map(this::mapToResource)
            .collect(toList());
    }

    private EntityModel<ConsentTO> mapToResource(Consent consent) {
        return EntityModel.of(consentMapper.toConsentTO(consent),
            linkTo(methodOn(ConsentController.class).getConsent(consent.getConsentId())).withSelfRel());
    }

    private EntityModel<ConsentStatusTO> mapToResource(ConsentStatus consentStatus) {
        return EntityModel.of(consentMapper.toConsentStatusTO(consentStatus));
    }

    private EntityModel<CreateConsentResponseTO> mapToResource(CreateConsentResponse createConsentResponse,
                                                            String bankCode) {
        String consentId = createConsentResponse.getConsentId();

        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(BankController.class).getBank(bankCode)).withRel("bank"));

        Optional.ofNullable(createConsentResponse.getAuthorisationId())
            .ifPresent(authorisationId -> links.add(linkTo(methodOn(ConsentAuthorisationController.class).getConsentAuthorisationStatus(consentId, authorisationId)).withRel("authorisationStatus")));

        Optional.ofNullable(createConsentResponse.getRedirectUrl())
            .ifPresent(redirectUrl -> links.add(Link.of(redirectUrl, LinkRelation.of("redirectUrl"))));

        Optional.ofNullable(createConsentResponse.getOauthRedirectUrl())
            .ifPresent(oauthUrl -> links.add(Link.of(oauthUrl, LinkRelation.of("oauthRedirectUrl"))));

        return EntityModel.of(consentMapper.toCreateConsentResponseTO(createConsentResponse), links);
    }

}
