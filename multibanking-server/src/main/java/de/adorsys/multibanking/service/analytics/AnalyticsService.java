package de.adorsys.multibanking.service.analytics;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.pers.spi.repository.AnalyticsRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BookingRuleRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.ContractRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.smartanalytics.api.AnalyticsResult;
import de.adorsys.smartanalytics.api.BookingGroup;
import de.adorsys.smartanalytics.api.Rule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.Rule.SIMILARITY_MATCH_TYPE.PURPOSE;
import static de.adorsys.multibanking.domain.Rule.SIMILARITY_MATCH_TYPE.REFERENCE_NAME;

@RequiredArgsConstructor
@Service
public class AnalyticsService {

    private final UserRepositoryIf userRepository;
    private final BookingRuleRepositoryIf rulesRepository;
    private final AnalyticsRepositoryIf analyticsRepository;
    private final ContractRepositoryIf contractRepository;
    private final SmartAnalyticsIf smartAnalyticsService;
    private final SmartAnalyticsMapper smartAnalyticsMapper;
    private final Principal principal;

    private static String normalize(String s) {
        if (s == null) {
            return null;
        }
        return s.toLowerCase()
            .replace("ü", "ue")
            .replace("ö", "oe")
            .replace("ä", "ae")
            .replace("ß", "ss")
            .replaceAll("[^a-z ]+", " ");
    }

    @Transactional
    public void createCustomRule(RuleEntity ruleEntity) {
        ruleEntity.setUserId(principal.getName());
        ruleEntity.setRuleType("STOP");
        ruleEntity.setRuleId("custom-" + UUID.randomUUID().toString());
        ruleEntity.setId(ruleEntity.getRuleId());
        if (ruleEntity.getSimilarityMatchType() == PURPOSE) {
            ruleEntity.setExpression(normalize(ruleEntity.getExpression()));
        } else if (ruleEntity.getSimilarityMatchType() == REFERENCE_NAME) {
            ruleEntity.setExpression(ruleEntity.getExpression().toLowerCase());
        }
        rulesRepository.createOrUpdateRule(ruleEntity);
        userRepository.setRulesLastChangeDate(principal.getName(), LocalDateTime.now());
    }

    @Transactional
    public void updateCustomRule(RuleEntity ruleEntity) {
        ruleEntity.setUserId(principal.getName());
        rulesRepository.createOrUpdateRule(ruleEntity);
        userRepository.setRulesLastChangeDate(principal.getName(), LocalDateTime.now());
    }

    private List<Rule> loadUserRules(String userId) {
        return rulesRepository.findByUserId(userId).stream()
            .map(customRuleEntity -> {
                Rule smartanalyticsRule = new Rule();
                BeanUtils.copyProperties(customRuleEntity, smartanalyticsRule);
                if (customRuleEntity.getSimilarityMatchType() != null) {
                    smartanalyticsRule.setSimilarityMatchType(Rule.SIMILARITY_MATCH_TYPE.valueOf(customRuleEntity.getSimilarityMatchType().toString()));
                }
                smartanalyticsRule.setStop(customRuleEntity.getRuleType() == null ||
                    customRuleEntity.getRuleType().equalsIgnoreCase("stop"));
                return smartanalyticsRule;
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteRule(String ruleId) {
        rulesRepository.deleteRule(ruleId);
        userRepository.setRulesLastChangeDate(principal.getName(), LocalDateTime.now());
    }

    public void saveAccountAnalytics(BankAccountEntity bankAccountEntity, List<BookingGroup> bookingGroups) {
        AccountAnalyticsEntity accountAnalyticsEntity = new AccountAnalyticsEntity();
        accountAnalyticsEntity.setUserId(bankAccountEntity.getUserId());
        accountAnalyticsEntity.setAccountId(bankAccountEntity.getId());
        accountAnalyticsEntity.setAnalyticsDate(LocalDateTime.now());
        accountAnalyticsEntity.setBookingGroups(smartAnalyticsMapper.mapBookingGroups(bookingGroups));

        analyticsRepository.deleteByAccountId(bankAccountEntity.getId());
        analyticsRepository.save(accountAnalyticsEntity);
    }

    public void identifyAndStoreContracts(String userId, String accountId, List<BookingGroup> bookingGroups) {
        List<ContractEntity> contractEntities = bookingGroups
            .stream()
            .filter(BookingGroup::isContract)
            .map(category -> smartAnalyticsMapper.toContractEntity(userId, accountId, category))
            .collect(Collectors.toList());

        contractRepository.deleteByAccountId(accountId);
        contractRepository.save(contractEntities);
    }

    public AnalyticsResult analyzeBookings(String userId, List<BookingEntity> bookingEntities) {
        bookingEntities.forEach(bookingEntity -> bookingEntity.setBookingCategory(null));
        return smartAnalyticsService.analyzeBookings(loadUserRules(userId),
            smartAnalyticsMapper.toSmartAnalyticsBookings(bookingEntities));
    }

}
