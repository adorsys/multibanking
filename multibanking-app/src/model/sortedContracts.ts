import { ContractEntity } from "./multibanking/models";

export interface SortedContracts {

    WEEKLY: ContractEntity[],
    MONTHLY: ContractEntity[],
    TWOMONTHLY: ContractEntity[],
    QUARTERLY: ContractEntity[],
    HALFYEARLY: ContractEntity[],
    YEARLY: ContractEntity[]
}
