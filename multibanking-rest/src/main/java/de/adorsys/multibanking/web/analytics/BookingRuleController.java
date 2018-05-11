package de.adorsys.multibanking.web.analytics;

import java.io.IOException;
import java.util.List;

import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.multibanking.domain.CustomRuleEntity;
import de.adorsys.multibanking.domain.RuleEntity;
import de.adorsys.multibanking.exception.InvalidRulesException;
import de.adorsys.multibanking.service.analytics.CustomBookingRuleService;
import de.adorsys.multibanking.service.analytics.SystemBookingRuleService;
import de.adorsys.multibanking.web.annotation.UserResource;
import de.adorsys.multibanking.web.common.BaseController;
import io.swagger.annotations.Api;

/**
 * @author alexg on 07.02.17.
 * @author fpo 2018-03-20 11:47
 */
@UserResource
@RestController
@RequestMapping(path = BookingRuleController.BASE_PATH)
@Api(value = BookingRuleController.BASE_PATH, 
	tags = "MB-008 - Analytics", description="Manages account and booking analytics.")
public class BookingRuleController extends BaseController {
	public static final String BASE_PATH = BaseController.BASE_PATH + "/analytics/rules"; 

    private static final Logger log = LoggerFactory.getLogger(BookingRuleController.class);
    private static final ObjectMapper YAML_OBJECT_MAPPER = yamlObjectMapper();

    @Autowired
    private CustomBookingRuleService customBookingRuleService;
    @Autowired
    private SystemBookingRuleService systemBookingRuleService;

    @RequestMapping(method = RequestMethod.POST)
    public HttpEntity<Void> createRule(@RequestBody CustomRuleEntity ruleEntity) {
        customBookingRuleService.createOrUpdateRule(ruleEntity);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/custom", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<ByteArrayResource> getCustomRules() {
        DSDocument dsDocument = customBookingRuleService.getBookingRules();
    	return loadBytesForWeb(dsDocument);
    }

    @RequestMapping(value = "/static", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<ByteArrayResource> getStaticRules() {
        DSDocument dsDocument = systemBookingRuleService.getBookingRules();
    	return loadBytesForWeb(dsDocument);
    }

    @RequestMapping(value = "/custom/{ruleId}", method = RequestMethod.PUT)
    public HttpEntity<Void> updateCustomRule(@PathVariable String ruleId, @RequestBody CustomRuleEntity ruleEntity) {
    	ruleEntity.setId(ruleId);
    	customBookingRuleService.createOrUpdateRule(ruleEntity);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/static/{ruleId}", method = RequestMethod.PUT)
    public HttpEntity<Void> updateRule(@PathVariable String ruleId, @RequestBody RuleEntity ruleEntity) {
    	ruleEntity.setId(ruleId);
    	systemBookingRuleService.createOrUpdateRule(ruleEntity);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(path = "/custom", method = RequestMethod.PUT)
    public HttpEntity<Void> createOrUpdateCustomRules(@RequestBody List<CustomRuleEntity> ruleEntities) {
    	customBookingRuleService.createOrUpdateRules(ruleEntities);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(path = "/static", method = RequestMethod.PUT)
    public HttpEntity<Void> createOrUpdateStaticRules(@RequestBody List<RuleEntity> ruleEntities) {
    	systemBookingRuleService.createOrUpdateRules(ruleEntities);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @RequestMapping(path = "/custom/upload", method = RequestMethod.PUT, consumes=MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public HttpEntity<?> uploadReplaceCustomRules(@RequestParam MultipartFile rulesFile) {
    	
        if (!rulesFile.isEmpty())return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("File is empty");

        try {
            List<CustomRuleEntity> rulesEntities = YAML_OBJECT_MAPPER.readValue(rulesFile.getInputStream(), new TypeReference<List<CustomRuleEntity>>() {});
            customBookingRuleService.replaceRules(rulesEntities);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (IOException e) {
            throw new InvalidRulesException(e.getMessage());
        }
    }

    @RequestMapping(path = "/static/upload", method = RequestMethod.PUT, consumes=MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public HttpEntity<?> uploadReplaceStaticRules(@RequestParam MultipartFile rulesFile) {
    	
        if (!rulesFile.isEmpty())return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("File is empty");

        try {
            List<RuleEntity> rulesEntities = YAML_OBJECT_MAPPER.readValue(rulesFile.getInputStream(), new TypeReference<List<RuleEntity>>() {});
            systemBookingRuleService.replaceRules(rulesEntities);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (IOException e) {
            throw new InvalidRulesException(e.getMessage());
        }
    }

    @RequestMapping(value = "/custom/{ruleId}", method = RequestMethod.DELETE)
    public HttpEntity<Void> deleteCustomRule(@PathVariable String ruleId) {
        customBookingRuleService.deleteRule(ruleId);
        log.info("Rule [{}] deleted.", ruleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @RequestMapping(value = "/static/{ruleId}", method = RequestMethod.DELETE)
    public HttpEntity<Void> deleteStaticRule(@PathVariable String ruleId) {
    	systemBookingRuleService.deleteRule(ruleId);
        log.info("Rule [{}] deleted.", ruleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/custom", method = RequestMethod.DELETE)
    public HttpEntity<Void> deleteCustomRules(@PathVariable List<String> ruleIds) {
        customBookingRuleService.deleteRules(ruleIds);
        log.info("Rule [{}] deleted.", ruleIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @RequestMapping(value = "/static", method = RequestMethod.DELETE)
    public HttpEntity<Void> deleteStaticRules(@PathVariable List<String> ruleIds) {
    	systemBookingRuleService.deleteRules(ruleIds);
        log.info("Rule [{}] deleted.", ruleIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
