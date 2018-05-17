import { Component } from "@angular/core";
import { NavParams, NavController } from "ionic-angular";
import { Booking } from "../../api/Booking";
import { RuleEditPage } from "../rule-edit/ruleEdit.component";
import { RulesService } from "../../services/rules.service";
import { Rule } from "../../api/Rule";
import { SimilarityMatchType } from "../../api/SimilarityMatchType";

@Component({
  selector: 'page-bookingDetail',
  templateUrl: 'bookingDetail.component.html'
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
    if (this.booking.bookingCategory && this.booking.bookingCategory.rules && this.booking.bookingCategory.rules[0].startsWith("custom")) {
      this.rulesService.getRule(this.booking.bookingCategory.rules[0]).subscribe(rule => {
        this.navCtrl.push(RuleEditPage, {
          rule: rule,
          customRule: true
        });
      });
    } else {
      let rule: Rule = {
        creditorId: this.booking.creditorId,
        receiver: this.booking.otherAccount ? this.booking.otherAccount.owner : undefined,
        incoming: this.booking.amount > 0
      };
      if (this.booking.otherAccount && this.booking.otherAccount.iban) {
        rule.similarityMatchType = SimilarityMatchType.IBAN;
        rule.expression = this.booking.otherAccount.iban;
      } else if (rule.receiver) {
        rule.similarityMatchType = SimilarityMatchType.REFERENCE_NAME;
        rule.expression = rule.receiver;
      } else {
        rule.similarityMatchType = SimilarityMatchType.PURPOSE;
        rule.expression = this.booking.usage;
      }
      this.navCtrl.push(RuleEditPage, {
        rule: rule,
        customRule: true
      });
    }




  }

}
