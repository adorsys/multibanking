package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.TransactionAuthorisationRequiredException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.multibanking.service.BankAccessService;
import de.adorsys.multibanking.service.ConsentService;
import de.adorsys.multibanking.web.mapper.BankAccessMapper;
import de.adorsys.multibanking.web.mapper.ConsentAuthorisationMapper;
import de.adorsys.multibanking.web.model.BankAccessTO;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Timed("bank-access")
@Tag(name = "Bankaccess")
@RequiredArgsConstructor
@Slf4j
@UserResource
@RestController
@RequestMapping(path = "api/v1/bankaccesses")
public class BankAccessController {

    private final BankAccessMapper bankAccessMapper;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final UserRepositoryIf userRepository;
    private final BankAccessService bankAccessService;
    private final ConsentService consentService;
    private final ConsentAuthorisationMapper consentAuthorisationMapper;
    private final Principal principal;

    @Operation(description = "Create new bank access", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @ApiResponse(responseCode = "201", description = "Created bank access", content = {
        @Content(schema = @Schema(ref = "#/components/schemas/ResourceBankAccess"))
    })
    @ApiResponse(responseCode = "202", description = "Challenge response", content = {
        @Content(schema = @Schema(ref = "#/components/schemas/ResourceUpdateAuthResponseTO"))
    })
    @PostMapping
    public ResponseEntity createBankAccess(@RequestBody BankAccessTO bankAccess) {
        Consent consent = consentService.getConsent(bankAccess.getConsentId());

        try {
            BankAccessEntity persistedBankAccess =
                bankAccessService.createBankAccess(bankAccessMapper.toBankAccessEntity(bankAccess,
                    principal.getName(), false, consent.getPsuAccountIban()));

            return ResponseEntity.created(linkTo(methodOn(BankAccessController.class).getBankAccess(persistedBankAccess.getId())).toUri())
                .body(mapToResource(persistedBankAccess));
        } catch (TransactionAuthorisationRequiredException e) {
            List<Link> links = new ArrayList<>();
            links.add(linkTo(methodOn(ConsentAuthorisationController.class).getConsentAuthorisationStatus(e.getConsentId(),
                e.getAuthorisationId())).withSelfRel());
            links.add(linkTo(methodOn(ConsentAuthorisationController.class).transactionAuthorisation(e.getConsentId(),
                e.getAuthorisationId(), null)).withRel("transactionAuthorisation"));
            return ResponseEntity.accepted().body(new Resource<>(consentAuthorisationMapper.toUpdateAuthResponseTO(e.getResponse()), links));
        }
    }

    @Operation(description = "Read bank accesses", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping
    public Resources<Resource<BankAccessTO>> getBankAccesses() {
        if (!userRepository.exists(principal.getName())) {
            return new Resources<>(Collections.emptyList());
        }

        List<BankAccessEntity> accessEntities = bankAccessRepository.findByUserId(principal.getName());
        return new Resources<>(mapToResources(accessEntities));
    }

    @Operation(description = "Read bank access", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping("/{accessId}")
    public Resource<BankAccessTO> getBankAccess(@PathVariable String accessId) {
        BankAccessEntity bankAccessEntity = bankAccessRepository.findByUserIdAndId(principal.getName(), accessId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));

        return mapToResource(bankAccessEntity);
    }

    @Operation(description = "Update bank access", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @PutMapping("/{accessId}")
    public HttpEntity<Void> updateBankAccess(@PathVariable String accessId,
                                             @RequestBody BankAccessTO bankAccess) {
        bankAccessService.updateBankAccess(accessId, bankAccessMapper.toBankAccessEntity(bankAccess,
            principal.getName(), false, null));
        log.info("Bank access [{}] updated.", accessId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(description = "Delete bank accesses", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @DeleteMapping("/{accessId}")
    public HttpEntity<Void> deleteBankAccess(@PathVariable String accessId) {
        if (bankAccessService.deleteBankAccess(principal.getName(), accessId)) {
            log.info("Bank Access [{}] deleted.", accessId);
        } else {
            throw new ResourceNotFoundException(BankAccessEntity.class, accessId);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private List<Resource<BankAccessTO>> mapToResources(List<BankAccessEntity> accessEntities) {
        return accessEntities.stream()
            .map(this::mapToResource)
            .collect(toList());
    }

    private Resource<BankAccessTO> mapToResource(BankAccessEntity accessEntity) {
        return new Resource<>(bankAccessMapper.toBankAccessTO(accessEntity),
            linkTo(methodOn(BankAccessController.class).getBankAccess(accessEntity.getId())).withSelfRel(),
            linkTo(methodOn(BankAccountController.class).getBankAccounts(accessEntity.getId())).withRel("accounts"));
    }

}
