package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.metrics.MetricsCollector;
import de.adorsys.multibanking.service.BankService;
import de.adorsys.multibanking.web.mapper.BankMapper;
import de.adorsys.multibanking.web.model.BankTO;
import de.adorsys.smartanalytics.exception.FileUploadException;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Bank")
@RequiredArgsConstructor
@UserResource
@RestController
@RequestMapping(path = {"api/v1/bank", "api/v1/banks"})
public class BankController {

    private final BankMapper bankMapper;
    private final BankService bankService;
    private final MetricsCollector metricsCollector;

    @Operation(description = "get bank by bank code")
    @GetMapping(value = "/{bankCode}")
    public Resource<BankTO> getBank(@PathVariable String bankCode) {
        long start = System.currentTimeMillis();
        Exception exception = null;

        try {
            return mapToResource(bankService.findBank(bankCode));
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;
            metricsCollector.time("bank", bankCode, null, exception, duration);
        }
    }

    @Timed("bank")
    @Operation(description = "find bank")
    @GetMapping
    public Resources<Resource<BankTO>> searchBank(@RequestParam String query) {
        return new Resources<>(mapToResources(bankService.search(query)));
    }

    @Timed("bank")
    @Operation(description = "Upload banks configuration file", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
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
