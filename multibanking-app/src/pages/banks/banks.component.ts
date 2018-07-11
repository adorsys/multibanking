import { Component } from "@angular/core";
import { LoadingController, AlertController } from 'ionic-angular';
import { BankService } from "../../services/bank.service";

@Component({
  selector: 'page-banks',
  templateUrl: 'banks.component.html'
})
export class BanksPage {

  constructor(private bankService: BankService,
    public alertCtrl: AlertController,
    public loadingCtrl: LoadingController) {
  }

  ngOnInit() {
  }

  uploadBanks(input) {
    let loading = this.loadingCtrl.create({
      content: 'Please wait...'
    });
    loading.present();

    let file: File = input.target.files[0];
    this.bankService.uploadBanks(file).subscribe(
      data => {
        loading.dismiss();
      },
      messages => {
        loading.dismiss();
        if (messages instanceof Array) {
          messages.forEach(message => {
            if (message.key == "INVALID_FILE") {
              this.alertCtrl.create({
                message: "Invalid banks file",
                buttons: ['OK']
              }).present();
            } else {
              this.alertCtrl.create({
                message: message.renderedMessage,
                buttons: ['OK']
              }).present();
            }
          })
        }
      });
    input.target.value = null;
  }

}