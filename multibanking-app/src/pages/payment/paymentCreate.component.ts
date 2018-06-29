import { Component } from "@angular/core";
import { NavController, NavParams, LoadingController, AlertController, ToastController } from "ionic-angular";
import { PaymentService } from "../../services/payment.service";
import { Payment } from "../../api/Payment";
import { BankAccount } from "../../api/BankAccount";
import { BankAccess } from "../../api/BankAccess";

@Component({
  selector: 'page-payment-create',
  templateUrl: 'paymentCreate.component.html'
})
export class PaymentCreatePage {

  bankAccess: BankAccess;
  bankAccount: BankAccount;
  payment: Payment = { receiver: "", purpose: "", amount: undefined };

  constructor(public navCtrl: NavController,
    public navparams: NavParams,
    private loadingCtrl: LoadingController,
    private toastCtrl: ToastController,
    private alertCtrl: AlertController,
    private paymentService: PaymentService) {

    this.bankAccount = navparams.data.bankAccount;
    this.bankAccess = navparams.data.bankAccess;
  }

  createPayment(pin: string) {
    if (!pin && !this.bankAccess.storePin) {
      return this.createPaymentPromptPin();
    }

    let loading = this.loadingCtrl.create({
      content: 'Please wait...'
    });
    loading.present();

    this.paymentService.createPayment(this.bankAccess.id, this.bankAccount.id, { payment: this.payment, pin: pin }).subscribe(
      paymentLocation => {
        loading.dismiss();
        this.askForTan(paymentLocation);
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

  askForTan(paymentLocation: string) {
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

  submitPayment(payment: Payment, tan: string) {
    this.paymentService.submitPayment(this.bankAccess.id, this.bankAccount.id, payment.id, { tan: tan }).subscribe(
      response => {
        this.toastCtrl.create({
          message: 'Payment successful',
          showCloseButton: true,
          position: 'top'
        }).present();
      }
    );
  }

}
