package de.adorsys.multibanking.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.multibanking.domain.RuleEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.BookingRuleRepositoryIf;
import de.adorsys.multibanking.service.analytics.AnalyticsService;
import de.adorsys.multibanking.web.mapper.RuleMapper;
import de.adorsys.multibanking.web.model.RuleTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RequiredArgsConstructor
@Slf4j
@UserResource
@RestController
@RequestMapping(path = "api/v1/analytics/rules")
public class CustomRulesController {

    private final RuleMapper ruleMapper;
    private final AnalyticsService analyticsService;
    private final BookingRuleRepositoryIf rulesRepository;

    @ApiOperation(
        value = "Create user custom rule",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @PostMapping
    public HttpEntity<Void> createRule(@RequestBody RuleTO rule) {
        analyticsService.createCustomRule(ruleMapper.toRuleEntity(rule));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(
        value = "Read user custom rule",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @GetMapping("{ruleId}")
    public Resource<RuleTO> getRule(@PathVariable String ruleId) {
        RuleEntity ruleEntity = rulesRepository.findByRuleId(ruleId)
            .orElseThrow(() -> new ResourceNotFoundException(RuleEntity.class, ruleId));

        return mapToResource(ruleEntity);
    }

    @ApiOperation(
        value = "Update user custom rule",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @PutMapping("{ruleId}")
    public HttpEntity<Void> updateRule(@PathVariable String ruleId, @RequestBody RuleTO rule) {
        analyticsService.updateCustomRule(ruleMapper.toRuleEntity(rule));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(
        value = "Delete user custom rule",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @DeleteMapping("{ruleId}")
    public HttpEntity<Void> deleteRule(@PathVariable String ruleId) {
        analyticsService.deleteRule(ruleId);
        log.info("Rule [{}] deleted.", ruleId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(
        value = "Read user custom rules",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @GetMapping
    public Resources<Resource<RuleTO>> getRules(@PageableDefault(size = 20) Pageable pageable,
                                                PagedResourcesAssembler<RuleTO> assembler) {
        Page<RuleEntity> pageableResult = rulesRepository.findAllPageable(pageable);
        return assembler.toResource(pageableResult.map(ruleMapper::toRuleTO));
    }

    @ApiOperation(
        value = "Search user custom rules",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @GetMapping("/search")
    public Resources<Resource<RuleTO>> searchRules(@RequestParam String query) {
        return new Resources<>(mapToResources(rulesRepository.search(query)));
    }

    @ApiOperation(
        value = "Download user custom rules",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @GetMapping(path = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public HttpEntity<InputStreamResource> downloadRules() throws JsonProcessingException {
        List<RuleTO> rules = ruleMapper.toRuleTOs(rulesRepository.findAll());

        final YAMLFactory ymlFactory = new YAMLFactory();
        ObjectMapper objectMapper = new ObjectMapper(ymlFactory);

        return ResponseEntity.ok()
            .body(new InputStreamResource(new ByteArrayInputStream(objectMapper.writeValueAsBytes(rules))));
    }

    private List<Resource<RuleTO>> mapToResources(List<RuleEntity> entities) {
        return entities.stream()
            .map(this::mapToResource)
            .collect(toList());
    }

    private Resource<RuleTO> mapToResource(RuleEntity entity) {
        return new Resource<>(ruleMapper.toRuleTO(entity),
            linkTo(methodOn(CustomRulesController.class).getRule(entity.getId())).withSelfRel());
    }

}
