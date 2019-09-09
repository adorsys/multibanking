import { Component, OnInit } from '@angular/core';
import { LoadingController, AlertController } from '@ionic/angular';
import { BankService } from 'src/app/services/rest/bank.service';

@Component({
  selector: 'app-banks-config',
  templateUrl: './banks-config.page.html',
  styleUrls: ['./banks-config.page.scss'],
})
export class BanksConfigPage implements OnInit {

  constructor(private loadingController: LoadingController,
              private bankService: BankService,
              private alertController: AlertController) { }

  ngOnInit() {
  }

  async uploadBanks(input) {
    const loading = await this.loadingController.create({
      message: 'Please wait...',
    });

    await loading.present();

    const file: File = input.target.files[0];
    this.bankService.uploadBanks(file).subscribe(
      () => {
        loading.dismiss();
      },
      messages => {
        loading.dismiss();
        if (messages instanceof Array) {
          messages.forEach(async message => {
            const alert = await this.alertController.create({
              header: 'Invalid banks file',
              message: message.renderedMessage,
              buttons: [
                {
                  text: 'Ok',
                  role: 'cancel'
                }
              ]
            });
            alert.present();
          });
        }
      });
    input.target.value = null;
  }


}
