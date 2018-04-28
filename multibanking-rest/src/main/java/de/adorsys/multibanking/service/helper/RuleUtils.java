package de.adorsys.multibanking.service.helper;

import java.util.List;

import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.apache.commons.lang3.StringUtils;

import de.adorsys.multibanking.domain.RuleEntity;
import de.adorsys.multibanking.utils.FQNUtils;

/**
 * @author fpo 2018-03-24 01:43
 *
 */
public abstract class RuleUtils{
	/*
	 * We use this for the static and custom rules. Caller must pass the correct userIdAuth
	 */
	public static final DocumentFQN bookingRulesFQN = FQNUtils.bookingRulesFQN();

	public static <T extends RuleEntity> List<T> normalize(List<T> list){
		for (T t : list) {
			// Normalize creditor id
			t.setCreditorId(StringUtils.removeAll(t.getCreditorId(), StringUtils.EMPTY));
			t.updateSearchIndex();
		}
		return list;
	}
}
