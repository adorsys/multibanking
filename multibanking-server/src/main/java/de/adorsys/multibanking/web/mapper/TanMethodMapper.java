package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.bg.domain.Consent;
import de.adorsys.multibanking.hbci.domain.TanMethod;
import de.adorsys.multibanking.web.model.ConsentTO;
import de.adorsys.multibanking.web.model.TanMethodTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", implementationName = "MultibankingTanMethodMapperImpl")
public interface TanMethodMapper {

    TanMethodTO toTanMethodTO(TanMethod consent);

}
