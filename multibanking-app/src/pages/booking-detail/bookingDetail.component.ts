import { Component } from "@angular/core";
import { NavParams, NavController } from "ionic-angular";
import { RuleEditPage } from "../rule-edit/ruleEdit.component";
import { RulesService } from "../../services/rules.service";
import { Booking, Rule } from "../../model/multibanking/models";

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
        rule.similarityMatchType = Rule.SimilarityMatchTypeEnum.IBAN;
        rule.expression = this.booking.otherAccount.iban;
      } else if (rule.receiver) {
        rule.similarityMatchType = Rule.SimilarityMatchTypeEnum.REFERENCENAME;
        rule.expression = rule.receiver;
      } else {
        rule.similarityMatchType = Rule.SimilarityMatchTypeEnum.PURPOSE;
        rule.expression = this.booking.usage;
      }
      this.navCtrl.push(RuleEditPage, {
        rule: rule,
        customRule: true
      });
    }




  }

}
