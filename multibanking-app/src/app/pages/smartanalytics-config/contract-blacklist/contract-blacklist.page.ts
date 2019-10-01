import { Component, OnInit } from '@angular/core';
import { ResourceContractBlacklist } from 'src/multibanking-api/resourceContractBlacklist';
import { RulesService } from 'src/app/services/rest/rules.service';
import { AlertController, LoadingController } from '@ionic/angular';

@Component({
  selector: 'app-contract-blacklist',
  templateUrl: './contract-blacklist.page.html',
  styleUrls: ['./contract-blacklist.page.scss'],
})
export class ContractBlacklistPage implements OnInit {

  contractBlacklistConfig: ResourceContractBlacklist;

  constructor(private rulesService: RulesService,
              public alertCtrl: AlertController,
              public loadingCtrl: LoadingController) {
  }

  ngOnInit() {
    this.loadContractBlacklistConfig();
  }

  loadContractBlacklistConfig() {
    this.rulesService.getContractBlacklist().subscribe(result => {
      this.contractBlacklistConfig = result;
    },
      messages => {
        if (messages instanceof Array) {
          messages.forEach(async message => {
            if (message.key !== 'RESOURCE_NOT_FOUND') {
              (await this.alertCtrl.create({
                message: message.renderedMessage,
                buttons: ['OK']
              })).present();
            }
          });
        }
      });
  }

  async uploadContractBlacklist(input) {
    const loading = this.loadingCtrl.create({
      message: 'Please wait...'
    });
    (await loading).present();

    const file: File = input.target.files[0];
    this.rulesService.uploadContractBlacklist(file).subscribe(
      async data => {
        this.loadContractBlacklistConfig();
        (await loading).dismiss();
      },
      async messages => {
        (await loading).dismiss();
        if (messages instanceof Array) {
          messages.forEach(async message => {
            if (message.key === 'INVALID_FILE') {
              (await this.alertCtrl.create({
                message: 'Invalid contract blacklist file',
                buttons: ['OK']
              })).present();
            } else {
              (await this.alertCtrl.create({
                message: message.renderedMessage,
                buttons: ['OK']
              })).present();
            }
          });
        }
      });
    input.target.value = null;
  }

}
