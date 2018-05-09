package de.adorsys.multibanking.service.analytics;

import static de.adorsys.multibanking.service.analytics.SmartanalyticsMapper.mapBookingGroups;
import static de.adorsys.multibanking.service.analytics.SmartanalyticsMapper.toContract;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.ContractEntity;
import de.adorsys.multibanking.service.BankAccountService;
import de.adorsys.smartanalytics.api.AnalyticsResult;
import de.adorsys.smartanalytics.api.BookingGroup;

@Service
public class AnalyticsService {
	@Autowired
	private BankAccountService bankAccountService;

    public void saveAccountAnalytics(BankAccountEntity bankAccountEntity, AnalyticsResult categoryResult, LocalDate referenceDate) {
        AccountAnalyticsEntity accountAnalyticsEntity = new AccountAnalyticsEntity();
        accountAnalyticsEntity.setUserId(bankAccountEntity.getUserId());
        accountAnalyticsEntity.setAccountId(bankAccountEntity.getId());
        accountAnalyticsEntity.setAnalyticsDate(referenceDate);

        accountAnalyticsEntity.setExpensesFix(categoryResult.getBudget().getExpensesFix());
        accountAnalyticsEntity.setExpensesFixBookings(mapBookingGroups(categoryResult.getBudget().getExpensesFixBookings()));
        accountAnalyticsEntity.setExpensesNext(categoryResult.getBudget().getExpensesNext());
        accountAnalyticsEntity.setExpensesNextBookings(mapBookingGroups(categoryResult.getBudget().getExpensesNextBookings()));
        accountAnalyticsEntity.setExpensesVariable(categoryResult.getBudget().getExpensesVariable());
        accountAnalyticsEntity.setExpensesVariableBookings(mapBookingGroups(categoryResult.getBudget().getExpensesVariableBookings()));
        accountAnalyticsEntity.setExpensesTotal(categoryResult.getBudget().getExpensesTotal());

        accountAnalyticsEntity.setIncomeFix(categoryResult.getBudget().getIncomeFix());
        accountAnalyticsEntity.setIncomeFixBookings(mapBookingGroups(categoryResult.getBudget().getIncomeFixBookings()));
        accountAnalyticsEntity.setIncomeNext(categoryResult.getBudget().getIncomeNext());
        accountAnalyticsEntity.setIncomeNextBookings(mapBookingGroups(categoryResult.getBudget().getIncomeNextBookings()));
        accountAnalyticsEntity.setIncomeVariable(categoryResult.getBudget().getIncomeVariable());
        accountAnalyticsEntity.setIncomeVariableBookings(mapBookingGroups(categoryResult.getBudget().getIncomeVariableBookings()));
        accountAnalyticsEntity.setIncomeTotal(categoryResult.getBudget().getIncomeTotal());

        accountAnalyticsEntity.setBalanceCalculated(
                bankAccountEntity.getBankAccountBalance().getReadyHbciBalance()
                        .add(accountAnalyticsEntity.getIncomeNext()).add(accountAnalyticsEntity.getExpensesNext()));
        bankAccountService.saveAccountAnalytics(bankAccountEntity.getId(), accountAnalyticsEntity);
    }

    public void identifyAndStoreContracts(String userId, String accountId, AnalyticsResult categoryResult) {
        List<ContractEntity> contractEntities = categoryResult.getBookingGroups()
                .stream()
                .filter(BookingGroup::isContract)
                .map(category -> toContract(userId, accountId, category))
                .collect(Collectors.toList());
        bankAccountService.saveContracts(accountId, contractEntities);
    }
}
