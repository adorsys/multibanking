import { Component } from '@angular/core';
import { NavParams } from 'ionic-angular';
import { RulesCustomPage } from '../rules-custom/rulesCustom.component';
import { RulesStaticPage } from '../rules-static/rulesStatic.component';
import { CategoriesPage } from '../categories/categories.component';
import { BookingGroupsPage } from '../booking-groups/booking-groups.component';
import { ContractBlacklistPage } from '../contract-blacklist/contract-blacklist.component';

@Component({
  templateUrl: 'config-tabs.component.html'
})
export class ConfigTabsPage {

  navParams;

  tab1Root = RulesCustomPage;
  tab2Root = RulesStaticPage;
  tab3Root = CategoriesPage;
  tab4Root = BookingGroupsPage;
  tab5Root = ContractBlacklistPage;

  constructor(public navparams: NavParams) {
    this.navParams = navparams.data;
  }
}
