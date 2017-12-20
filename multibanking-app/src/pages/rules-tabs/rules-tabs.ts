import { Component } from '@angular/core';
import { NavParams } from 'ionic-angular';
import { RulesCustomPage } from '../rules-custom/rulesCustom';
import { RulesStaticPage } from '../rules-static/rulesStatic';

@Component({
  templateUrl: 'rules-tabs.html'
})
export class RulesTabsPage {

  navParams;

  tab1Root = RulesCustomPage;
  tab2Root = RulesStaticPage;

  constructor(public navparams: NavParams) {
    this.navParams = navparams.data;
  }
}
