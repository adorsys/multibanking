import { Component } from '@angular/core';
import { BookingListPage } from '../booking/bookingList.component';
import { ContractsComponent } from '../contracts/contracts.component';
import { AnalyticsPage } from '../analytics/analytics.component';
import { NavParams } from 'ionic-angular';

@Component({
  templateUrl: 'booking-tabs.component.html'
})
export class BookingTabsPage {

  navParams;

  tab1Root = BookingListPage;
  tab2Root = AnalyticsPage;
  tab3Root = ContractsComponent;

  constructor(public navparams: NavParams) {
    this.navParams = navparams.data;
  }
}
