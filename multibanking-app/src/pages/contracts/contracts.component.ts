import { Component, ViewChild } from "@angular/core";
import { NavParams, AlertController, ToastController, LoadingController, Navbar, NavController } from "ionic-angular";

import { ContractService } from "./contract.service";
import { LogoService } from '../../services/LogoService';
import { BankAccountService } from "../../services/bankAccountService";
import { BankAccess } from "../../api/BankAccess";

@Component({
  selector: 'contracts',
  templateUrl: 'contracts.component.html'
})
export class ContractsComponent {

  bankAccess: BankAccess;
  bankAccountId: string;
  getLogo: Function;
  contracts;

  @ViewChild(Navbar) navBar: Navbar;

  constructor(
    public navCtrl: NavController,
    public navParams: NavParams,
    private alertCtrl: AlertController,
    private toastCtrl: ToastController,
    private loadingCtrl: LoadingController,
    private contractService: ContractService,
    private bankAccountService: BankAccountService,
    public logoService: LogoService
  ) {
    this.bankAccess = navParams.data.bankAccess;
    this.bankAccountId = navParams.data.bankAccount.id;
    this.getLogo = logoService.getLogo;
  }

  ngOnInit() {
    this.bankAccountService.bookingsChangedObservable.subscribe(changed => {
      this.loadContracts();
    })
    this.loadContracts();
  }

  ionViewDidLoad() {
    this.navBar.backButtonClick = (e: UIEvent) => {
      this.navCtrl.parent.viewCtrl.dismiss();
    };
  }

  loadContracts() {
    this.contracts = {
      income: [],
      expenses: []
    }
    this.contractService.getContracts(this.bankAccess.id, this.bankAccountId)
      .subscribe(contracts => {
        contracts.reduce((acc, contract) => {
          contract.amount > 0 ? acc.income.push(contract) : acc.expenses.push(contract)
          return acc;
        }, this.contracts)
      });
  }

  syncBookingsPromptPin() {
    let alert = this.alertCtrl.create({
      title: 'Pin',
      inputs: [
        {
          name: 'pin',
          placeholder: 'Bank Account Pin',
          type: 'password'
        }
      ],
      buttons: [
        {
          text: 'Cancel',
          role: 'cancel'
        },
        {
          text: 'Submit',
          handler: data => {
            if (data.pin.length > 0) {
              this.syncBookings(data.pin);
            }
          }
        }
      ]
    });
    alert.present();
  }

  syncBookings(pin) {
    if (!pin && !this.bankAccess.storePin) {
      return this.syncBookingsPromptPin();
    }

    let loading = this.loadingCtrl.create({
      content: 'Please wait...'
    });
    loading.present();

    this.bankAccountService.syncBookings(this.bankAccess.id, this.bankAccountId, pin).subscribe(
      response => {
        loading.dismiss();
      },
      error => {
        if (error && error.messages) {
          error.messages.forEach(message => {

            if (message.key == "SYNC_IN_PROGRESS") {
              this.toastCtrl.create({
                message: 'Account sync in progress',
                showCloseButton: true,
                position: 'top'
              }).present();
            }
            else if (message.key == "INVALID_PIN") {
              this.alertCtrl.create({
                message: 'Invalid pin',
                buttons: ['OK']
              }).present();
            }
          })
        }
      }
    )
  }
}
