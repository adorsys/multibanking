import { Component } from "@angular/core";
import { RulesService } from "../../services/rules.service";
import { LoadingController, AlertController } from 'ionic-angular';
import { ContractBlacklist } from "../../model/multibanking/models";

@Component({
  selector: 'page-contract-blacklist',
  templateUrl: 'contract-blacklist.component.html'
})
export class ContractBlacklistPage {

  contractBlacklistConfig: ContractBlacklist;

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
          messages.forEach(message => {
            if (message.key != "RESOURCE_NOT_FOUND") {
              this.alertCtrl.create({
                message: message.renderedMessage,
                buttons: ['OK']
              }).present();
            }
          })
        }
      })
  }

  uploadContractBlacklist(input) {
    let loading = this.loadingCtrl.create({
      content: 'Please wait...'
    });
    loading.present();

    let file: File = input.target.files[0];
    this.rulesService.uploadContractBlacklist(file).subscribe(
      data => {
        this.loadContractBlacklistConfig();
        loading.dismiss();
      },
      messages => {
        loading.dismiss();
        if (messages instanceof Array) {
          messages.forEach(message => {
            if (message.key == "INVALID_FILE") {
              this.alertCtrl.create({
                message: "Invalid contract blacklist file",
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
