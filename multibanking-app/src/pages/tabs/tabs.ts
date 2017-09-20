import { Component } from '@angular/core';
import { BookingListPage } from '../booking/bookingList';
import { ContractsComponent } from '../contracts/contracts.component';
import { AnalyticsPage } from '../analytics/analytics';
import { NavParams } from 'ionic-angular';
import { BankAccess } from '../../api/BankAccess';

@Component({
  templateUrl: 'tabs.html'
})
export class TabsPage {

  navParams;

  tab1Root = BookingListPage;
  tab2Root = AnalyticsPage;
  tab3Root = ContractsComponent;

  constructor(private navparams: NavParams) {
    this.navParams = navparams.data;
  }
}
