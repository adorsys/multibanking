import { Component, Input } from '@angular/core';
import 'rxjs/Rx';
import { SortedContracts } from '../model/sortedContracts';
import { ContractEntity } from '../model/multibanking/models';

@Component({
    selector: 'contract-list',
    templateUrl: 'contractList.directive.html'
})

export class ContractListDirective {

    intervals: ContractEntity.IntervalEnum[];
    sortedContracts: SortedContracts

    @Input() set contracts(contracts: SortedContracts) {
        this.sortedContracts = contracts;
        if (contracts) {
            this.intervals = Object.keys(contracts)
                .map(interval => ContractEntity.IntervalEnum[interval])
                .filter(interval => this.getIntervalContracts(interval).length > 0)
        }
    }

    getIntervalLabel(interval) {
        switch (interval) {
            case ContractEntity.IntervalEnum.WEEKLY:
                return 'Weekly'
            case ContractEntity.IntervalEnum.MONTHLY:
                return 'Monthly'
            case ContractEntity.IntervalEnum.TWOMONTHLY:
                return 'Two-monthly'
            case ContractEntity.IntervalEnum.QUARTERLY:
                return 'Quarterly'
            case ContractEntity.IntervalEnum.HALFYEARLY:
                return 'Half-yearly'
            case ContractEntity.IntervalEnum.YEARLY:
                return 'Yearly'
        }
    }

    getIntervalContracts(interval) {
        switch (interval) {
            case ContractEntity.IntervalEnum.WEEKLY:
                return this.sortedContracts.WEEKLY;
            case ContractEntity.IntervalEnum.MONTHLY:
                return this.sortedContracts.MONTHLY;
            case ContractEntity.IntervalEnum.TWOMONTHLY:
                return this.sortedContracts.TWOMONTHLY;
            case ContractEntity.IntervalEnum.QUARTERLY:
                return this.sortedContracts.QUARTERLY;
            case ContractEntity.IntervalEnum.HALFYEARLY:
                return this.sortedContracts.HALFYEARLY;
            case ContractEntity.IntervalEnum.YEARLY:
                return this.sortedContracts.YEARLY;
        }
    }
}