import { Component } from "@angular/core";
import { NavParams } from "ionic-angular";

import { Contract } from './contract.model';
import { ContractService } from "./contract.service";
import { LogoService } from '../../services/LogoService';

@Component({
  selector: 'contracts',
  templateUrl: 'contracts.component.html'
})
export class ContractsComponent {

  bankAccessId: string;
  bankAccountId: string;
  getLogo: Function;
  contracts;

  constructor(
    private navParams: NavParams,
    private contractService: ContractService,
    private logoService: LogoService
  ) {
    this.bankAccessId = navParams.data.bankAccess.id;
    this.bankAccountId = navParams.data.bankAccountId;
    this.getLogo = logoService.getLogo;
    this.contracts = {
      income: [],
      expenses: []
    }
  }

  ngOnInit() {
    this.contractService.getContracts(this.bankAccessId, this.bankAccountId)
      .subscribe(contracts => {
        contracts.reduce((acc, contract) => {
          contract.amount > 0 ? acc.income.push(contract) : acc.expenses.push(contract)
          return acc;
        }, this.contracts)
      });
  }
}
