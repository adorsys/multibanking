package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.web.model.BankApiTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankApiMapper {

    BankApi toBankApi(BankApiTO bankApiTO);

}
