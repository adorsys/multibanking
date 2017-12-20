import { Component } from "@angular/core";
import { NavParams, NavController } from "ionic-angular";
import { Booking } from "../../api/Booking";
import { RuleEditPage } from "../rule-edit/ruleEdit";
import { RulesService } from "../../services/RulesService";
import { Rule } from "../../api/Rule";

@Component({
  selector: 'page-bookingDetail',
  templateUrl: 'bookingDetail.html'
})
export class BookingDetailPage {

  booking: Booking;

  constructor(
    public navCtrl: NavController,
    public navparams: NavParams,
    private rulesService: RulesService
  ) {
    this.booking = navparams.data.booking;
  }

  editRule() {
    if (this.booking.bookingCategory && this.booking.bookingCategory.rules) {
      this.rulesService.getRule(this.booking.bookingCategory.rules[0]).subscribe(rule => {
        this.navCtrl.push(RuleEditPage, {
          rule: rule
        });
      })
    } else {
      let rule: Rule = {
        creditorId: this.booking.creditorId,
        receiver: this.booking.otherAccount? this.booking.otherAccount.owner : undefined
      };
      this.navCtrl.push(RuleEditPage, {
        rule: rule
      });
    }




  }

}
