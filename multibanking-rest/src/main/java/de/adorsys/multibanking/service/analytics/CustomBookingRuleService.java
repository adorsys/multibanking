package de.adorsys.multibanking.service.analytics;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

import de.adorsys.multibanking.domain.CustomRuleEntity;
import de.adorsys.multibanking.service.base.CacheBasedService;
import de.adorsys.multibanking.service.base.UserObjectService;
import de.adorsys.multibanking.service.helper.BookingRuleServiceTemplate;

/**
 * TODO: reset rule provider after update.
 * 		ruleToUpdate.updateSearchIndex();
 * 
 * @author fpo 2018-03-24 01:43
 *
 */
@Service
public class CustomBookingRuleService extends BookingRuleServiceTemplate<CustomRuleEntity> {
	@Autowired
	private UserObjectService uos;

	@Override
	protected CacheBasedService cbs() {
		return uos;
	}

	@Override
	protected TypeReference<List<CustomRuleEntity>> listType() {
		return new TypeReference<List<CustomRuleEntity>>() {};
	}
}
