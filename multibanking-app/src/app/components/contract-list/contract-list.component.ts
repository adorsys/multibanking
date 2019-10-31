import { Component, Input } from '@angular/core';

import { ContractTO } from '../../../multibanking-api/contractTO';
import { SortedContracts } from '../../model/sortedContracts';

@Component({
  selector: 'app-contract-list',
  templateUrl: './contract-list.component.html'
})
export class ContractListComponent {

  intervals: ContractTO.IntervalEnum[];
  sortedContracts: SortedContracts;

  @Input() set contracts(contracts: SortedContracts) {
    this.sortedContracts = contracts;
    if (contracts) {
      this.intervals = Object.keys(contracts)
        .map(interval => ContractTO.IntervalEnum[interval])
        .filter(interval => this.getIntervalContracts(interval).length > 0);
    }
  }

  getIntervalLabel(interval) {
    switch (interval) {
      case ContractTO.IntervalEnum.WEEKLY:
        return 'Weekly';
      case ContractTO.IntervalEnum.MONTHLY:
        return 'Monthly';
      case ContractTO.IntervalEnum.TWOMONTHLY:
        return 'Two-monthly';
      case ContractTO.IntervalEnum.QUARTERLY:
        return 'Quarterly';
      case ContractTO.IntervalEnum.HALFYEARLY:
        return 'Half-yearly';
      case ContractTO.IntervalEnum.YEARLY:
        return 'Yearly';
    }
  }

  getIntervalContracts(interval) {
    switch (interval) {
      case ContractTO.IntervalEnum.WEEKLY:
        return this.sortedContracts.WEEKLY;
      case ContractTO.IntervalEnum.MONTHLY:
        return this.sortedContracts.MONTHLY;
      case ContractTO.IntervalEnum.TWOMONTHLY:
        return this.sortedContracts.TWOMONTHLY;
      case ContractTO.IntervalEnum.QUARTERLY:
        return this.sortedContracts.QUARTERLY;
      case ContractTO.IntervalEnum.HALFYEARLY:
        return this.sortedContracts.HALFYEARLY;
      case ContractTO.IntervalEnum.YEARLY:
        return this.sortedContracts.YEARLY;
    }
  }
}
