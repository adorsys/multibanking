package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.Balance;
import de.adorsys.multibanking.domain.BalancesReport;
import de.adorsys.multibanking.web.model.BalanceTO;
import de.adorsys.multibanking.web.model.BalancesReportTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BalancesMapper {

    BalancesReportTO toBalancesReportTO(BalancesReport balancesReport);

    BalanceTO toBalancesTO(Balance balance);
}
