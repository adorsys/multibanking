import { Component } from "@angular/core";
import { NavParams } from "ionic-angular";
import { BookingGroup } from "../../api/BookingGroup";
import { AppConfig } from "../../app/app.config";

@Component({
  selector: 'page-bookingGroup',
  templateUrl: 'bookingGroup.html'
})
export class BookingGroupPage {

  label: string;
  bookingGroups: Array<BookingGroup>

  constructor(private navparams: NavParams) {
    this.label = navparams.data.label;
    this.bookingGroups = this.sortBookingGroups(navparams.data.bookingGroups);
  }

  sortBookingGroups (bookingGroups: Array<BookingGroup>): Array<BookingGroup> {
    return bookingGroups.sort((group1: BookingGroup, group2: BookingGroup) => {
      if (group1.nextExecutionDate > group2.nextExecutionDate) {
        return 1;
      }

       if (group1.nextExecutionDate < group2.nextExecutionDate) {
        return -1;
      }

      return 0;
    })
  }

  getCompanyLogoUrl(bookingGroup: BookingGroup) {
    return AppConfig.api_url + "/image/" + bookingGroup.contract.logo;
  }
}