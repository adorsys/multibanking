import { Component, OnInit } from '@angular/core';
import { StaticRulesPage } from '../static-rules/static-rules.page';
import { NavController, LoadingController, AlertController } from '@ionic/angular';
import { ResourceRuleEntity } from 'src/multibanking-api/resourceRuleEntity';
import { RulesService } from 'src/app/services/rest/rules.service';

@Component({
  selector: 'app-custom-rules',
  templateUrl: './custom-rules.page.html',
  styleUrls: ['./custom-rules.page.scss'],
})
export class CustomRulesPage extends StaticRulesPage {

  selectedRule: ResourceRuleEntity;
  rules: ResourceRuleEntity[];
  custom = true;

  constructor(public navCtrl: NavController,
              public loadingCtrl: LoadingController,
              public alertCtrl: AlertController,
              public rulesService: RulesService) {

    super(navCtrl, loadingCtrl, alertCtrl, rulesService);
  }

  registerRulesChangedListener() {
    this.rulesService.rulesChangedObservable.subscribe(rule => {
      this.loadRules();
    });
  }

  releaseRule(rule: ResourceRuleEntity) {
    this.rulesService.createRule(rule, false).subscribe(rules => {
      this.rulesService.deleteRule(rule.id, true).subscribe(rules => {
        this.loadRules();
      });
    });
  }

}
