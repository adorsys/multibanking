import { ContractTO } from './../../multibanking-api/contractTO';

export interface SortedContracts {
  WEEKLY: ContractTO[];
  MONTHLY: ContractTO[];
  TWOMONTHLY: ContractTO[];
  QUARTERLY: ContractTO[];
  HALFYEARLY: ContractTO[];
  YEARLY: ContractTO[];
}
