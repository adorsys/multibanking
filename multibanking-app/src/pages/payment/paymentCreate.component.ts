import { Component } from "@angular/core";
import { NavController, NavParams, LoadingController, AlertController, ToastController } from "ionic-angular";
import { PaymentService } from "../../services/payment.service";
import { BankAccess, Payment, ResourceBankAccount, ResourcePaymentEntity, TanTransportType } from "../../model/multibanking/models";
import { BankService } from "../../services/bank.service";

@Component({
  selector: 'page-payment-create',
  templateUrl: 'paymentCreate.component.html'
})
export class PaymentCreatePage {

  bankAccess: BankAccess;
  bankAccount: ResourceBankAccount;
  tanTransportType: TanTransportType;
  payment: Payment = { receiver: "", purpose: "", amount: undefined };
  pin: string;

  constructor(public navCtrl: NavController,
    public navparams: NavParams,
    private loadingCtrl: LoadingController,
    private toastCtrl: ToastController,
    private alertCtrl: AlertController,
    private bankService: BankService,
    private paymentService: PaymentService) {

    this.bankAccount = navparams.data.bankAccount;
    this.bankAccess = navparams.data.bankAccess;
  }

  ngOnInit() {
    if (this.bankAccess.tanTransportTypes && Object.keys(this.bankAccess.tanTransportTypes).length > 0) {
      this.bankService.getBank(this.bankAccess.bankCode).subscribe(bank => {
        let tanTransportTypes = this.bankAccess.tanTransportTypes[bank.bankApi];
        if (tanTransportTypes) {
          this.tanTransportType = tanTransportTypes[0];
          this.payment.tanMedia = this.tanTransportType;
        } else {
          this.showMissingTanTransportTypeError();
        }
      })
    } else {
      this.showMissingTanTransportTypeError();
    }
  }

  showMissingTanTransportTypeError() {
    this.alertCtrl.create({
      message: 'Tan tansport media not available!',
      buttons: ['OK']
    }).present();
  }

  createPayment(pin: string) {
    if (!pin && !this.bankAccess.storePin) {
      return this.createPaymentPromptPin();
    }

    this.pin = pin;

    let loading = this.loadingCtrl.create({
      content: 'Please wait...'
    });
    loading.present();

    this.paymentService.createPayment(this.bankAccess.id, this.bankAccount.id, { payment: this.payment, pin: pin }).subscribe(
      paymentLocation => {
        loading.dismiss();
        this.executePayment(paymentLocation);
      },
      messages => {
        if (messages instanceof Array) {
          messages.forEach(message => {
            if (message.key == "ERROR_PAYMENT") {
              this.alertCtrl.create({
                message: 'Payment Error',
                buttons: ['OK']
              }).present();
            }
            else if (message.key == "INVALID_PIN") {
              this.alertCtrl.create({
                message: 'Invalid pin',
                buttons: ['OK']
              }).present();
            }
          });
        }
      })
  }

  createPaymentPromptPin() {
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
              this.createPayment(data.pin);
            }
          }
        }
      ]
    });
    alert.present();
  }

  executePayment(paymentLocation: string) {
    this.paymentService.getPayment(paymentLocation).subscribe(
      payment => {
        let alert = this.alertCtrl.create({
          title: payment.paymentChallenge.title,
          inputs: [
            {
              name: 'tan',
              placeholder: 'Tan',
              type: 'text'
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
                if (data.tan.length > 0) {
                  this.submitPayment(payment, data.tan);
                }
              }
            }
          ]
        });
        alert.present();

      }
    )
  }

  submitPayment(payment: ResourcePaymentEntity, tan: string) {
    this.paymentService.submitPayment(this.bankAccess.id, this.bankAccount.id, payment.id, { pin: this.pin, tan: tan }).subscribe(
      response => {
        this.payment = { receiver: "", purpose: "", receiverIban: "", amount: undefined, tanMedia: this.tanTransportType };
        this.toastCtrl.create({
          message: 'Payment successful',
          showCloseButton: true,
          position: 'top'
        }).present();
      }
    );
  }

}
