package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.ContractEntity;
import de.adorsys.multibanking.web.model.ContractTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    ContractTO toContractTO(ContractEntity contractEntity);

    List<ContractTO> toContractTOs(List<ContractEntity> contractEntities);

}
