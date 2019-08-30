package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.multibanking.service.BankAccessService;
import de.adorsys.multibanking.web.mapper.BankAccessMapper;
import de.adorsys.multibanking.web.model.BankAccessTO;
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
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Api(tags = "Multibanking bankaccess")
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
    private final Principal principal;

    @ApiOperation(
        value = "Read bank accesses",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @GetMapping
    public Resources<Resource<BankAccessTO>> getBankAccesses() {
        if (!userRepository.exists(principal.getName())) {
            return new Resources<>(Collections.emptyList());
        }

        List<BankAccessEntity> accessEntities = bankAccessRepository.findByUserId(principal.getName());
        return new Resources<>(mapToResources(accessEntities));
    }

    @ApiOperation(
        value = "Create new bank accesses",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @PostMapping
    public ResponseEntity<Resource<BankAccessTO>> createBankAccess(@RequestBody BankAccessTO bankAccess) {
        BankAccessEntity persistedBankAccess =
            bankAccessService.createBankAccess(bankAccessMapper.toBankAccessEntity(bankAccess, principal.getName(),
                false));

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(methodOn(BankAccessController.class).getBankAccess(persistedBankAccess.getId())).toUri());

        return new ResponseEntity<>(mapToResource(persistedBankAccess), headers, HttpStatus.CREATED);
    }

    @ApiOperation(
        value = "Read bank access",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @GetMapping("/{accessId}")
    public Resource<BankAccessTO> getBankAccess(@PathVariable String accessId) {
        BankAccessEntity bankAccessEntity = bankAccessRepository.findByUserIdAndId(principal.getName(), accessId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));

        return mapToResource(bankAccessEntity);
    }

    @ApiOperation(
        value = "Update bank access",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @PutMapping("/{accessId}")
    public HttpEntity<Void> updateBankAccess(@PathVariable String accessId,
                                             @RequestBody BankAccessTO bankAccess) {
        bankAccessService.updateBankAccess(accessId, bankAccessMapper.toBankAccessEntity(bankAccess,
            principal.getName(), false));
        log.info("Bank access [{}] updated.", accessId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(
        value = "Delete bank accesses",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
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
