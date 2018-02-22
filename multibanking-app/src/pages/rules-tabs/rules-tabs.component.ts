import { Component } from '@angular/core';
import { NavParams } from 'ionic-angular';
import { RulesCustomPage } from '../rules-custom/rulesCustom.component';
import { RulesStaticPage } from '../rules-static/rulesStatic.component';

@Component({
  templateUrl: 'rules-tabs.component.html'
})
export class RulesTabsPage {

  navParams;

  tab1Root = RulesCustomPage;
  tab2Root = RulesStaticPage;

  constructor(public navparams: NavParams) {
    this.navParams = navparams.data;
  }
}
