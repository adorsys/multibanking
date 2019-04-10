import { Component, ViewChild } from "@angular/core";
import { NavController, NavParams, LoadingController, AlertController } from "ionic-angular";
import { BankAccessService } from "../../services/bankAccess.service";
import { AutoCompleteComponent } from "ionic2-auto-complete";
import { BankAutoCompleteService } from "../../services/bankAutoComplete.service";
import { BankAccess, Bank } from "../../model/multibanking/models";
import { Consent } from "../../model/multibanking/consent";

@Component({
  selector: 'page-bankaccess-create',
  templateUrl: 'bankAccessCreate.component.html'
})
export class BankAccessCreatePage {

  @ViewChild(AutoCompleteComponent) autocomplete: AutoCompleteComponent;

  selectedBank: Bank;

  userId: string;
  bankAccess: BankAccess = {
    bankCode: '',
    iban: '',
    bankLogin: '',
    bankLogin2: '',
    pin: '',
    userId: '',
    storePin: true,
    storeBookings: true,
    categorizeBookings: true,
    storeAnalytics: true,
    provideDataForMachineLearning: false
  };
  parent;

  constructor(public navCtrl: NavController,
    public navparams: NavParams,
    public bankAutoCompleteService: BankAutoCompleteService,
    private loadingCtrl: LoadingController,
    private alertCtrl: AlertController,
    private bankAccessService: BankAccessService) {

    this.userId = navparams.data.userId;
    this.bankAccess.userId = navparams.data.userId;
    this.parent = navparams.data.parent;
  }

  ngOnInit() {
    this.autocomplete.itemSelected.subscribe(bank => {
      if (!bank.loginSettings) {
        bank.loginSettings = {
          advice: "Bank login data",
          credentials: [{ label: "customer id", masked: false }, { label: "pin", masked: true }]
        }
      }
      this.selectedBank = bank;
    });

    this.autocomplete.searchbarElem.ionClear.subscribe(() => {
      this.selectedBank = undefined;
    });
  }

  public createBankAccess() {
    let loading = this.loadingCtrl.create({
      content: 'Please wait...'
    });
    loading.present();

    this.bankAccess.bankCode = this.selectedBank.bankCode;

    if (this.selectedBank.loginSettings.credentials) {
      for (let i = 0; i < this.selectedBank.loginSettings.credentials.length; i++) {
        let credentials: any = this.selectedBank.loginSettings.credentials[i];
  
        if (i == 0) {
          this.bankAccess.bankLogin = credentials.input;
        }
        else if (i == 1) {
          if (!this.selectedBank.loginSettings.credentials[i].masked) {
            this.bankAccess.bankLogin2 = credentials.input;
          } else {
            this.bankAccess.pin = credentials.input;
          }
        }
        else if (i == 2) {
          this.bankAccess.pin = credentials.input;
        }
      }
    }
    
    this.bankAccessService.createBankAcccess(this.bankAccess).subscribe(
      consent => {
        loading.dismiss();
        if (consent &&  consent.authUrl) {
          console.log(consent.authUrl+'&successUrl='+location.href);
          location.href = consent.authUrl+'&successUrl='+location.href;
        }

        this.parent.bankAccessesChanged();
        this.navCtrl.pop();
      },
      messages => {
        loading.dismiss();
        if (messages instanceof Array) {
          messages.forEach(message => {
            if (message.key == "BANK_ACCESS_ALREADY_EXIST") {
              this.alertCtrl.create({
                message: 'Bank connection already exists',
                buttons: ['OK']
              }).present();
            } else if (message.key == "INVALID_BANK_ACCESS") {
              this.alertCtrl.create({
                message: 'Bank not supported',
                buttons: ['OK']
              }).present();
            } else if (message.key == "INVALID_PIN") {
              this.alertCtrl.create({
                message: 'Invalid pin',
                buttons: ['OK']
              }).present();
            } else {
              this.alertCtrl.create({
                message: message.renderedMessage,
                buttons: ['OK']
              }).present();
            }
          });

        }
      });
  }
}
