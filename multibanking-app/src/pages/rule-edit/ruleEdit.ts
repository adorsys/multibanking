import { Component } from "@angular/core";
import { NavParams, NavController } from "ionic-angular";
import { RuleCategory } from "../../api/RuleCategory";
import { Rule } from "../../api/Rule";
import { RulesService } from "../../services/RulesService";

@Component({
  selector: 'page-ruleEdit',
  templateUrl: 'ruleEdit.html'
})
export class RuleEditPage {

  rule: Rule;
  categories: Array<RuleCategory>;
  subCategories: Array<RuleCategory>;
  specifications: Array<RuleCategory>;
  mainCategoryId: string;
  subCategoryId: string;
  specificationId: string;

  mainCategory: RuleCategory;
  subCategory: RuleCategory;
  specification: RuleCategory;

  constructor(
    public navCtrl: NavController,
    public navparams: NavParams,
    private rulesService: RulesService
  ) {
    this.rule = navparams.data.rule;
  }

  ngOnInit() {
    this.rulesService.getAvailableCategories().subscribe(
      response => {
        this.categories = response;
        if (this.rule.mainCategory) {
          this.mainCategoryChanged(this.rule.mainCategory);
          if (this.rule.subCategory) {
            this.subCategoryChanged(this.rule.subCategory);
          }
          if (this.rule.specification) {
            this.specificationChanged(this.rule.specification);
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
    if (this.rule.receiver && this.rule.receiver.length == 0) {
      this.rule.receiver = undefined;
    }
    if (this.rule.id) {
      this.rulesService.updateRule(this.rule).subscribe(
        response => {
          this.navCtrl.pop();
        })
    } else {
      this.rulesService.createRule(this.rule).subscribe(
        response => {
          this.navCtrl.pop();
        })
    }

  }
}
