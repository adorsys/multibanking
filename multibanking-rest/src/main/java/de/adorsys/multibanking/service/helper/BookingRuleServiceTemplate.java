package de.adorsys.multibanking.service.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;

import de.adorsys.multibanking.domain.RuleEntity;
import de.adorsys.multibanking.service.base.CacheBasedService;
import de.adorsys.multibanking.service.base.ListUtils;

/**
 * TODO: reset rule provider after update.
 * 		ruleToUpdate.updateSearchIndex();
 * 
 * @author fpo 2018-03-24 01:43
 *
 */
public abstract class BookingRuleServiceTemplate<T extends RuleEntity>{
	protected abstract CacheBasedService cbs();
	protected abstract TypeReference<List<T>> listType();
	
    @Autowired
    private DocumentSafeService documentSafeService;
	
    public DSDocument getBookingRules() {
        return documentSafeService.readDocument(cbs().auth(), RuleUtils.bookingRulesFQN);
    }

	public void createOrUpdateRule(T ruleEntity) {
		createOrUpdateRules(Collections.singletonList(ruleEntity));
	}
	
	public void createOrUpdateRules(List<T> ruleEntities) {
		List<T> persList = cbs().load(RuleUtils.bookingRulesFQN, listType()).orElse(new ArrayList<>());
		persList = ListUtils.updateList(RuleUtils.normalize(ruleEntities), persList);
		cbs().store(RuleUtils.bookingRulesFQN, listType(), persList);
	}
	
	public void replaceRules(List<T> ruleEntities) {
		ruleEntities = ListUtils.setId(ruleEntities);
		cbs().store(RuleUtils.bookingRulesFQN, listType(), ruleEntities);
	}

	public boolean deleteRule(String ruleId) {
		return deleteRules(Collections.singletonList(ruleId));
	}
	public boolean deleteRules(List<String> ruleIds) {
		List<T> persList = cbs().load(RuleUtils.bookingRulesFQN, listType()).orElse(new ArrayList<>());
		List<T> newPersList = ListUtils.deleteListById(ruleIds, persList);
		cbs().store(RuleUtils.bookingRulesFQN, listType(), newPersList);
		return (newPersList.size() - persList.size()) !=0;
	}
	
	public List<T> loadRules(){
		return cbs().load(RuleUtils.bookingRulesFQN, listType()).orElse(new ArrayList<>());
	}
}
