import { Component, OnInit } from '@angular/core';
import { ResourceRuleEntity } from 'src/multibanking-api/resourceRuleEntity';
import { Pageable } from 'src/app/model/pageable';
import { NavController, LoadingController, AlertController } from '@ionic/angular';
import { RulesService } from 'src/app/services/rest/rules.service';

@Component({
  selector: 'app-static-rules',
  templateUrl: './static-rules.page.html',
  styleUrls: ['./static-rules.page.scss'],
})
export class StaticRulesPage implements OnInit {

  rulesStatus;
  selectedRule: ResourceRuleEntity;
  rules: ResourceRuleEntity[];
  pageable: Pageable;
  custom = false;

  constructor(public navCtrl: NavController,
              public loadingCtrl: LoadingController,
              public alertCtrl: AlertController,
              public rulesService: RulesService) {

    this.registerRulesChangedListener();
  }

  ngOnInit() {
    this.loadRules();
  }

  registerRulesChangedListener() {
    this.rulesService.rulesChangedObservable.subscribe(rule => {
      this.loadRules();
    });
  }

  loadRules() {
    this.rulesService.getRules(this.custom).subscribe(response => {
      this.pageable = response;
      this.rules = response._embedded ? response._embedded.ruleEntityList : [];

      this.selectedRule = undefined;

      this.rulesService.getRulesStatus().subscribe(rulesStatus => {
        this.rulesStatus = rulesStatus;
      });
    });
  }

  loadNextRules(infiniteScroll) {
    if (this.pageable._links.next) {
      this.rulesService.getNextRules(this.pageable._links.next.href).subscribe(response => {
        this.pageable = response;
        this.rules = this.rules.concat(response._embedded.ruleEntityList);

        infiniteScroll.target.complete();
      });
    } else {
      infiniteScroll.target.complete();
    }
  }

  doInfinite(infiniteScroll) {
    this.loadNextRules(infiniteScroll);
  }

  createRule() {
    // this.navCtrl.push(RuleEditPage, { customRule: false });
  }

  editRule(rule: ResourceRuleEntity) {
    // this.navCtrl.push(RuleEditPage, { rule, customRule: false });
  }

  deleteRule(rule: ResourceRuleEntity) {
    this.rulesService.deleteRule(rule.id, this.custom).subscribe(rules => {
      this.loadRules();
    });
  }

  searchRules(ev: any) {
    const val = ev.target.value;

    if (val && val.trim() !== '') {
      this.rulesService.searchRules(this.custom, val).subscribe((rules) => {
        this.rules = rules;
      });
    } else {
      this.rules = [];
    }
  }

  downloadRules() {
    this.rulesService.downloadRules(this.custom).subscribe(data => {
      this.showFile(data);
    });
  }

  showFile(blob) {
    // It is necessary to create a new blob object with mime-type explicitly set
    // otherwise only Chrome works like it should
    const newBlob = new Blob([blob], { type: 'application/x-yaml' });

    // IE doesn't allow using a blob object directly as link href
    // instead it is necessary to use msSaveOrOpenBlob
    if (window.navigator && window.navigator.msSaveOrOpenBlob) {
      window.navigator.msSaveOrOpenBlob(newBlob);
      return;
    }

    // For other browsers:
    // Create a link pointing to the ObjectURL containing the blob.
    const data = window.URL.createObjectURL(newBlob);
    const link = document.createElement('a');
    link.href = data;
    link.download = 'rules.csv';
    link.click();
    setTimeout(() =>
      // For Firefox it is necessary to delay revoking the ObjectURL
      window.URL.revokeObjectURL(data), 100);
  }

  async uploadRules(input) {
    const loading = await this.loadingCtrl.create({
      message: 'Please wait...'
    });
    loading.present();

    const file: File = input.target.files[0];
    this.rulesService.uploadRules(file).subscribe(
      data => {
        this.loadRules();
        loading.dismiss();
      },
      messages => {
        loading.dismiss();
        if (messages instanceof Array) {
          messages.forEach(async message => {
            if (message.key === 'INVALID_RULES') {
              (await this.alertCtrl.create({
                message: 'Invalid rules file',
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
