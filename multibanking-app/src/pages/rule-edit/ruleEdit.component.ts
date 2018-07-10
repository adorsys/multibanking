import { Component } from "@angular/core";
import { NavParams, NavController } from "ionic-angular";
import { RulesService } from "../../services/rules.service";
import { ResourceRuleEntity, Category, Rule } from "../../model/multibanking/models";


@Component({
  selector: 'page-ruleEdit',
  templateUrl: 'ruleEdit.component.html'
})
export class RuleEditPage {

  customRule: boolean;
  rule: ResourceRuleEntity;
  similarityMatcher: Array<any> = [];
  categories: Array<Category>;
  subCategories: Array<Category>;
  specifications: Array<Category>;
  mainCategoryId: string;
  subCategoryId: string;
  specificationId: string;

  mainCategory: Category;
  subCategory: Category;
  specification: Category;

  constructor(
    public navCtrl: NavController,
    public navparams: NavParams,
    private rulesService: RulesService
  ) {
    this.rule = navparams.data.rule ? navparams.data.rule : {};
    this.customRule = navparams.data.customRule;
    for (var n in Rule.SimilarityMatchTypeEnum) {
      this.similarityMatcher.push(n);
    }
  }

  ngOnInit() {
    this.rulesService.getAvailableCategories().subscribe(
      response => {
        this.categories = response.categories;
        if (this.categories) {
          if (this.rule.mainCategory) {
            this.mainCategoryChanged(this.rule.mainCategory);
            if (this.rule.subCategory) {
              this.subCategoryChanged(this.rule.subCategory);
              if (this.rule.specification) {
                this.specificationChanged(this.rule.specification);
              }
            }
          }
        }
      });
  }

  mainCategoryChanged(catId) {
    this.mainCategoryId = catId;
    this.mainCategory = this.categories.filter((element: Category) => element.id == catId)[0];
    this.subCategories = this.mainCategory.subcategories;
  }
  subCategoryChanged(catId) {
    this.subCategoryId = catId;
    this.subCategory = this.subCategories.filter((element: Category) => element.id == catId)[0];
    this.specifications = this.subCategory.specifications;
  }
  specificationChanged(catId) {
    this.specificationId = catId;
    this.specification = this.specifications.filter((element: Category) => element.id == catId)[0];
  }

  similarityMatherChanged($event) {
    this.rule.expression = null;
  }

  submit() {
    if (this.rule.receiver && this.rule.receiver.length == 0) {
      this.rule.receiver = undefined;
    }
    if (this.rule.similarityMatchType == Rule.SimilarityMatchTypeEnum.CUSTOM) {
      this.rule.similarityMatchType = undefined;
    }
    if (this.rule.id) {
      this.rulesService.updateRule(this.rule).subscribe(
        response => {
          this.navCtrl.pop();
        });
    } else {
      this. rulesService.createRule(this.rule, true).subscribe(
        response => {
          this.navCtrl.pop();
        });
    }

  }
}
