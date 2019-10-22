import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AlertController, LoadingController, ToastController } from '@ionic/angular';
import * as moment from 'moment';
import { Observable, Subscriber } from 'rxjs';
import { ImagesService } from 'src/app/services/rest/images.service';

import { ContractTO } from '../../../multibanking-api/contractTO';
import { Link } from '../../../multibanking-api/link';
import { ResourceBankAccount } from '../../../multibanking-api/resourceBankAccount';
import { ResourceUpdateAuthResponseTO } from '../../../multibanking-api/resourceUpdateAuthResponseTO';
import { BankAccountService } from '../../services/rest/bankAccount.service';
import { ConsentService } from '../../services/rest/consent.service';
import { ContractService } from '../../services/rest/contract.service';
import { getHierarchicalRouteParam } from '../../utils/utils';
import { SortedContracts } from './../../model/sortedContracts';

@Component({
  selector: 'app-contracts',
  templateUrl: './contracts.page.html',
  styleUrls: ['./contracts.page.scss'],
})
export class ContractsPage implements OnInit {

  bankAccount: ResourceBankAccount;
  bankAccessId: string;
  contracts: { income: SortedContracts, expenses: SortedContracts };

  constructor(
    private activatedRoute: ActivatedRoute,
    private alertController: AlertController,
    private toastController: ToastController,
    private loadingController: LoadingController,
    private contractService: ContractService,
    private consentService: ConsentService,
    private bankAccountService: BankAccountService,
    private imagesService: ImagesService
  ) {}

  ngOnInit() {
    this.bankAccessId = getHierarchicalRouteParam(this.activatedRoute.snapshot, 'access-id');
    this.bankAccount = this.activatedRoute.snapshot.data.bankAccount;

    if (!this.bankAccount.lastSync || moment(this.bankAccount.lastSync).isBefore(moment(), 'day')) {
      this.syncBookings();
    } else {
      this.loadContracts();
    }
  }

  getLogo(image: string): string {
    return this.imagesService.getImage(image);
  }

  loadContracts() {
    this.contractService.getContracts(this.bankAccessId, this.bankAccount.id)
      .subscribe(contracts => {
        this.contractsLoaded(contracts);
      });
  }

  contractsLoaded(contracts: ContractTO[]) {
    this.contracts = {
      income: {
        WEEKLY: [],
        MONTHLY: [],
        TWOMONTHLY: [],
        QUARTERLY: [],
        HALFYEARLY: [],
        YEARLY: []
      },
      expenses: {
        WEEKLY: [],
        MONTHLY: [],
        TWOMONTHLY: [],
        QUARTERLY: [],
        HALFYEARLY: [],
        YEARLY: []
      }
    };
    contracts.forEach(contract => {
      if (contract.amount > 0) {
        switch (contract.interval) {
          case ContractTO.IntervalEnum.WEEKLY:
            this.contracts.income.WEEKLY.push(contract);
            break;
          case ContractTO.IntervalEnum.MONTHLY:
            this.contracts.income.MONTHLY.push(contract);
            break;
          case ContractTO.IntervalEnum.TWOMONTHLY:
            this.contracts.income.TWOMONTHLY.push(contract);
            break;
          case ContractTO.IntervalEnum.QUARTERLY:
            this.contracts.income.QUARTERLY.push(contract);
            break;
          case ContractTO.IntervalEnum.HALFYEARLY:
            this.contracts.income.HALFYEARLY.push(contract);
            break;
          case ContractTO.IntervalEnum.YEARLY:
            this.contracts.income.YEARLY.push(contract);
            break;
        }
      } else {
        switch (contract.interval) {
          case ContractTO.IntervalEnum.WEEKLY:
            this.contracts.expenses.WEEKLY.push(contract);
            break;
          case ContractTO.IntervalEnum.MONTHLY:
            this.contracts.expenses.MONTHLY.push(contract);
            break;
          case ContractTO.IntervalEnum.TWOMONTHLY:
            this.contracts.expenses.TWOMONTHLY.push(contract);
            break;
          case ContractTO.IntervalEnum.QUARTERLY:
            this.contracts.expenses.QUARTERLY.push(contract);
            break;
          case ContractTO.IntervalEnum.HALFYEARLY:
            this.contracts.expenses.HALFYEARLY.push(contract);
            break;
          case ContractTO.IntervalEnum.YEARLY:
            this.contracts.expenses.YEARLY.push(contract);
            break;
        }
      }
    });
  }

  async syncBookings() {
    const loading = await this.loadingController.create({
      message: 'Please wait...'
    });
    loading.present();

    this.bankAccountService.syncBookings(this.bankAccessId, this.bankAccount.id).subscribe(
      response => {
        loading.dismiss();
        if (response && response.challenge) {
          this.presentTanPrompt(response).subscribe(tan => {
            this.submitTan(response, tan);
          });
        } else {
          this.loadContracts();
          this.bankAccount.lastSync = moment().toDate();
        }
      },
      messages => {
        loading.dismiss();
        if (messages instanceof Array) {
          messages.forEach(async message => {
            if (message.key === 'SYNC_IN_PROGRESS') {
              const toast = await this.toastController.create({
                message: 'Account sync in progress',
                showCloseButton: true,
                position: 'top'
              });
              toast.present();
            } else if (message.key === 'INVALID_PIN') {
              const toast = await this.toastController.create({
                message: 'Invalid pin',
                buttons: ['OK']
              });
              toast.present();
            } else {
              const toast = await this.toastController.create({
                message: message.renderedMessage,
                buttons: ['OK']
              });
              toast.present();
            }
          });
        }
      });
  }

  presentTanPrompt(consentAuthStatus: ResourceUpdateAuthResponseTO): Observable<string> {
    return new Observable((observer: Subscriber<string>) => {
      this.alertController.create({
        header: consentAuthStatus.challenge.additionalInformation,
        inputs: [
          {
            name: 'TAN',
            type: 'text',
          }
        ],
        buttons: [
          {
            text: 'Ok',
            handler: data => {
              observer.next(data.TAN);
            }
          }
        ]
      })
        .then(alert => alert.present());
    });
  }

  private submitTan(consentAuthStatus: ResourceUpdateAuthResponseTO, tan: string) {
    // tslint:disable-next-line:no-string-literal
    const updateAuthenticationLink: Link = consentAuthStatus._links['transactionAuthorisation'];
    this.consentService.updateAuthentication(updateAuthenticationLink.href, { scaAuthenticationData: tan })
      .subscribe(
        () => {
          this.syncBookings();
        },
        messages => {
          if (messages instanceof Array) {
            messages.forEach(async message => {
              const alert = await this.alertController.create({
                message: message.renderedMessage,
                buttons: ['OK']
              });
              alert.present();
            });
          }
        });
  }

}
