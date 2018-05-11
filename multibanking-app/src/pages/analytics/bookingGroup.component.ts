import { Component } from "@angular/core";
import { NavParams } from "ionic-angular";
import { BookingGroup } from "../../api/BookingGroup";
import { AppConfig } from "../../app/app.config";
import { GroupType } from "../../api/GroupType";
import { AggregatedGroups } from "../../api/AggregatedGroups";

@Component({
  selector: 'page-bookingGroup',
  templateUrl: 'bookingGroup.component.html'
})
export class BookingGroupPage {

  label: string;
  bookingGroups: BookingGroup[];

  constructor(public navparams: NavParams) {
    this.label = navparams.data.label;
    this.bookingGroups = this.sortBookingGroups(navparams.data.bookingGroups);
  }

  sortBookingGroups(bookingGroups: AggregatedGroups): BookingGroup[] {
    return bookingGroups.groups.sort((group1: BookingGroup, group2: BookingGroup) => {
      if (group1.type == GroupType.CUSTOM) {
        return 1;
      }

      if (group2.type== GroupType.CUSTOM) {
        return -1;
      }
    });
  }

  getCompanyLogoUrl(bookingGroup: BookingGroup) {
    return AppConfig.api_url + "/image/" + bookingGroup.contract.logo;
  }
}
