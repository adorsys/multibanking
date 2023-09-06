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
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Timed("custom-rule")
@Tag(name = "Custom rule")
@RequiredArgsConstructor
@Slf4j
@UserResource
@RestController
@RequestMapping(path = "api/v1/analytics/rules")
public class CustomRulesController {

    private final RuleMapper ruleMapper;
    private final AnalyticsService analyticsService;
    private final BookingRuleRepositoryIf rulesRepository;

    @Operation(description = "Create user custom rule", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @PostMapping
    public HttpEntity<Void> createRule(@RequestBody RuleTO rule) {
        analyticsService.createCustomRule(ruleMapper.toRuleEntity(rule));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(description = "Read user custom rule", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping("{ruleId}")
    public EntityModel<RuleTO> getRule(@PathVariable String ruleId) {
        RuleEntity ruleEntity = rulesRepository.findByRuleId(ruleId)
            .orElseThrow(() -> new ResourceNotFoundException(RuleEntity.class, ruleId));

        return mapToResource(ruleEntity);
    }

    @Operation(description = "Update user custom rule", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @PutMapping("{ruleId}")
    public HttpEntity<Void> updateRule(@PathVariable String ruleId, @RequestBody RuleTO rule) {
        analyticsService.updateCustomRule(ruleMapper.toRuleEntity(rule));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(description = "Delete user custom rule", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @DeleteMapping("{ruleId}")
    public HttpEntity<Void> deleteRule(@PathVariable String ruleId) {
        analyticsService.deleteRule(ruleId);
        log.info("Rule [{}] deleted.", ruleId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(description = "Read user custom rules", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping
    public CollectionModel<EntityModel<RuleTO>> getRules(@PageableDefault(size = 20) Pageable pageable,
                                                PagedResourcesAssembler<RuleTO> assembler) {
        Page<RuleEntity> pageableResult = rulesRepository.findAllPageable(pageable);
        return assembler.toModel(pageableResult.map(ruleMapper::toRuleTO));
    }

    @Operation(description = "Search user custom rules", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping("/search")
    public CollectionModel<EntityModel<RuleTO>> searchRules(@RequestParam String query) {
        return CollectionModel.of(mapToResources(rulesRepository.search(query)));
    }

    @Operation(description = "Download user custom rules", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping(path = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public HttpEntity<InputStreamResource> downloadRules() throws JsonProcessingException {
        List<RuleTO> rules = ruleMapper.toRuleTOs(rulesRepository.findAll());

        final YAMLFactory ymlFactory = new YAMLFactory();
        ObjectMapper objectMapper = new ObjectMapper(ymlFactory);

        return ResponseEntity.ok()
            .body(new InputStreamResource(new ByteArrayInputStream(objectMapper.writeValueAsBytes(rules))));
    }

    private List<EntityModel<RuleTO>> mapToResources(List<RuleEntity> entities) {
        return entities.stream()
            .map(this::mapToResource)
            .collect(toList());
    }

    private EntityModel<RuleTO> mapToResource(RuleEntity entity) {
        return EntityModel.of(ruleMapper.toRuleTO(entity),
            linkTo(methodOn(CustomRulesController.class).getRule(entity.getId())).withSelfRel());
    }

}
