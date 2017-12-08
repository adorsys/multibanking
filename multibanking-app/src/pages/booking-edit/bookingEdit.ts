import { Component } from "@angular/core";
import { NavParams, NavController } from "ionic-angular";
import { Booking } from "../../api/Booking";
import { AnalyticsService } from "../../services/analyticsService";
import { RuleCategory } from "../../api/RuleCategory";
import { Rule } from "../../api/Rule";

@Component({
  selector: 'page-bookingEdit',
  templateUrl: 'bookingEdit.html'
})
export class BookingEditPage {

  booking: Booking;
  categories: Array<RuleCategory>;
  subCategories: Array<RuleCategory>;
  specifications: Array<RuleCategory>;
  mainCategoryId: RuleCategory;
  subCategoryId: RuleCategory;
  specificationId: RuleCategory;

  receiver: string;
  expression: string;
  mainCategory: RuleCategory;
  subCategory: RuleCategory;
  specification: RuleCategory;
  taxRelevant: boolean;

  constructor(
    public navCtrl: NavController,
    navparams: NavParams,
    private analyticsService: AnalyticsService
  ) {
    this.booking = navparams.data.booking;
  }

  ngOnInit() {
    this.analyticsService.getAvailableCategories().subscribe(
      response => {
        this.categories = response;
        if (this.booking.bookingCategory && this.booking.bookingCategory.mainCategory) {
          this.mainCategoryChanged(this.booking.bookingCategory.mainCategory);
          if (this.booking.bookingCategory.subCategory) {
            this.subCategoryChanged(this.booking.bookingCategory.subCategory);
          }
          if (this.booking.bookingCategory.specification) {
            this.specificationChanged(this.booking.bookingCategory.specification);
          }
        }
      })
  }

  mainCategoryChanged(catId) {
    this.mainCategoryId = catId;
    this.mainCategory = this.categories.filter((element: RuleCategory) => element.id == catId)[0];
    this.subCategories = this.mainCategory.subcategories;
  }
  subCategoryChanged(catId) {
    this.subCategoryId = catId;
    this.subCategory = this.subCategories.filter((element: RuleCategory) => element.id == catId)[0];
    this.specifications = this.subCategory.specifications;
  }
  specificationChanged(catId) {
    this.specificationId = catId;
    this.specification = this.specifications.filter((element: RuleCategory) => element.id == catId)[0];
  }

  submit() {
    let rule: Rule = {
      ruleId: this.booking.bookingCategory ? this.booking.bookingCategory.rules.reverse()[0] : null,
      creditorId: this.booking.creditorId,
      receiver: this.receiver,
      expression: this.expression,
      mainCategory: this.mainCategory.id,
      subCategory: this.subCategory ? this.subCategory.id : null,
      specification: this.specification ? this.specification.id : null,
      incoming: this.booking.amount > 0,
      taxRelevant: this.taxRelevant
    };
    this.analyticsService.createRule(rule).subscribe(
      response => {
        this.navCtrl.pop();
      })
  }
}
