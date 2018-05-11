package de.adorsys.multibanking.service.analytics;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

import de.adorsys.multibanking.domain.RuleEntity;
import de.adorsys.multibanking.service.base.SystemObjectService;
import de.adorsys.multibanking.service.base.CacheBasedService;
import de.adorsys.multibanking.service.helper.BookingRuleServiceTemplate;

/**
 * TODO: reset rule provider after update.
 * 		ruleToUpdate.updateSearchIndex();
 * @author fpo
 *
 */
@Service
public class SystemBookingRuleService extends BookingRuleServiceTemplate<RuleEntity>  {
	@Autowired
	private SystemObjectService sos;

	@Override
	protected CacheBasedService cbs() {
		return sos;
	}

	@Override
	protected TypeReference<List<RuleEntity>> listType() {
		return new TypeReference<List<RuleEntity>>() {};
	}
}
