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
  contracts: Array<Contract>;

  constructor(
    private navParams: NavParams,
    private contractService: ContractService,
    private logoService: LogoService
  ) {
    this.bankAccessId = navParams.data.bankAccess.id;
    this.bankAccountId = navParams.data.bankAccountId;
    this.getLogo = logoService.getLogo;
  }

  ngOnInit() {
    this.contractService.getContracts(this.bankAccessId, this.bankAccountId)
      .subscribe(contracts => this.contracts = contracts);
  }
}
