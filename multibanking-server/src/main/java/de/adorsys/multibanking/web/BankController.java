package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.service.BankService;
import de.adorsys.multibanking.web.mapper.BankMapper;
import de.adorsys.multibanking.web.model.BankTO;
import de.adorsys.smartanalytics.exception.FileUploadException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RequiredArgsConstructor
@UserResource
@RestController
@RequestMapping(path = {"api/v1/bank", "api/v1/banks"})
public class BankController {

    private final BankMapper bankMapper;
    private final BankService bankService;

    @GetMapping(value = "/{bankCode}")
    public Resource<BankTO> getBank(@PathVariable String bankCode) {
        return mapToResource(bankService.findBank(bankCode));
    }

    @GetMapping
    public Resources<Resource<BankTO>> searchBank(@RequestParam String query) {
        return new Resources<>(mapToResources(bankService.search(query)));
    }

    @ApiOperation(
        value = "Upload banks configuration file",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @PostMapping("/upload")
    public HttpEntity<Void> uploadBanks(@RequestParam MultipartFile banksFile) {
        if (!banksFile.isEmpty()) {
            bankService.importBanks(banksFile);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            throw new FileUploadException("File is empty");
        }
    }

    private List<Resource<BankTO>> mapToResources(List<BankEntity> entities) {
        return entities.stream()
            .map(this::mapToResource)
            .collect(toList());
    }

    private Resource<BankTO> mapToResource(BankEntity entity) {
        return new Resource<>(bankMapper.toBankTO(entity),
            linkTo(methodOn(BankController.class).getBank(entity.getBankCode())).withSelfRel());
    }

}
