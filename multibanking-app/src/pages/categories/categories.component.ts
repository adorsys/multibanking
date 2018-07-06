import { Component } from "@angular/core";
import { RulesService } from "../../services/rules.service";
import { CategoriesContainer } from "api/CategoriesContainer";
import { LoadingController, AlertController } from 'ionic-angular';

@Component({
  selector: 'page-categories',
  templateUrl: 'categories.component.html'
})
export class CategoriesPage {

  categoriesContainer: CategoriesContainer;

  constructor(private rulesService: RulesService,
    public alertCtrl: AlertController,
    public loadingCtrl: LoadingController) {
  }

  ngOnInit() {
    this.loadCategories();
  }

  loadCategories() {
    this.rulesService.getAvailableCategories().subscribe(result => {
      this.categoriesContainer = result;
    })
  }

  uploadCategories(input) {
    let loading = this.loadingCtrl.create({
      content: 'Please wait...'
    });
    loading.present();

    let file: File = input.target.files[0];
    this.rulesService.uploadCategories(file).subscribe(
      data => {
        this.loadCategories();
        loading.dismiss();
      },
      messages => {
        loading.dismiss();
        if (messages instanceof Array) {
          messages.forEach(message => {
            if (message.key == "INVALID_CATEGORIES") {
              this.alertCtrl.create({
                message: "Invalid categories file",
                buttons: ['OK']
              }).present();
            }
          })
        }
      });
  }

}