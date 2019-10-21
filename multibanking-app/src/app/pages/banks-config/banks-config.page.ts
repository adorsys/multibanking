import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { AlertController, LoadingController } from '@ionic/angular';
import { debounceTime } from 'rxjs/operators';
import { BankService } from 'src/app/services/rest/bank.service';
import { BankTO } from 'src/multibanking-api/bankTO';

@Component({
  selector: 'app-banks-config',
  templateUrl: './banks-config.page.html',
  styleUrls: ['./banks-config.page.scss'],
})
export class BanksConfigPage implements OnInit {

  public searchControl: FormControl;
  public banks: BankTO[];

  constructor(private loadingController: LoadingController,
              private bankService: BankService,
              private alertController: AlertController) {
    this.searchControl = new FormControl();
  }

  ngOnInit() {
    this.searchControl.valueChanges
      .pipe(debounceTime(700))
      .subscribe(search => {
        this.setFilteredItems(search);
      });
  }

  setFilteredItems(searchTerm) {
    this.bankService.searchBanks(searchTerm).subscribe(bankList => {
      this.banks = bankList;
    });
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
