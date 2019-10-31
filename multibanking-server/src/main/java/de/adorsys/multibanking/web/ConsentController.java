package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.service.ConsentService;
import de.adorsys.multibanking.web.mapper.BankApiMapper;
import de.adorsys.multibanking.web.mapper.ConsentMapper;
import de.adorsys.multibanking.web.model.BankApiTO;
import de.adorsys.multibanking.web.model.ConsentTO;
import de.adorsys.multibanking.web.model.CreateConsentResponseTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iban4j.Iban;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
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
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Api(tags = "Multibanking consent")
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

    @ApiOperation(value = "Create new consent")
    @PostMapping
    public ResponseEntity<Resource<CreateConsentResponseTO>> createConsent(@Valid @RequestBody ConsentTO consent,
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

    @ApiOperation(value = "Read user consents")
    @GetMapping
    public Resources<Resource<ConsentTO>> getConsents() {
        List<Consent> consents = bankAccessRepository.findByUserId(principal.getName())
            .stream()
            .map(bankAccessEntity -> consentService.getConsent(bankAccessEntity.getConsentId()))
            .collect(toList());

        return new Resources<>(mapToResources(consents));
    }

    @ApiOperation(value = "Read consent")
    @GetMapping("/{consentId}")
    public Resource<ConsentTO> getConsent(@PathVariable("consentId") String consentId) {
        return mapToResource(consentService.getConsent(consentId));
    }

    @ApiOperation(value = "Read consent")
    @GetMapping("redirect/{redirectId}")
    public Resource<ConsentTO> getConsentByRedirectId(@PathVariable("redirectId") String redirectId) {
        return mapToResource(consentService.getConsentByRedirectId(redirectId));
    }

    @ApiOperation(value = "Delete consent")
    @DeleteMapping("/{consentId}")
    public HttpEntity<Void> deleteConsent(@PathVariable String consentId) {
        consentService.revokeConsent(consentId);

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

    private Resource<CreateConsentResponseTO> mapToResource(CreateConsentResponse createConsentResponse,
                                                            String bankCode) {
        String consentId = createConsentResponse.getConsentId();
        String authorisationId = createConsentResponse.getAuthorisationId();

        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(BankController.class).getBank(bankCode)).withRel("bank"));
        links.add(linkTo(methodOn(ConsentAuthorisationController.class).getConsentAuthorisationStatus(consentId,
            authorisationId)).withRel("authorisationStatus"));

        Optional.ofNullable(createConsentResponse.getRedirectUrl())
            .ifPresent(redirectUrl -> links.add(new Link(redirectUrl, "redirectUrl")));

        return new Resource<>(consentMapper.toCreateConsentResponseTO(createConsentResponse), links);
    }

}
