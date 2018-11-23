import { Component } from "@angular/core";
import { RulesService } from "../../services/rules.service";
import { LoadingController, AlertController } from 'ionic-angular';
import { CategoriesTree } from "model/multibanking/categoriesTree";


@Component({
  selector: 'page-categories',
  templateUrl: 'categories.component.html'
})
export class CategoriesPage {

  categoriesContainer: CategoriesTree;

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
    },
      messages => {
        if (messages instanceof Array) {
          messages.forEach(message => {
            if (message.key != "RESOURCE_NOT_FOUND") {
              this.alertCtrl.create({
                message: message.renderedMessage,
                buttons: ['OK']
              }).present();
            }
          })
        }
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
            if (message.key == "INVALID_FILE") {
              this.alertCtrl.create({
                message: "Invalid categories file",
                buttons: ['OK']
              }).present();
            } else {
              this.alertCtrl.create({
                message: message.renderedMessage,
                buttons: ['OK']
              }).present();
            }
          })
        }
      });
    input.target.value = null;
  }

}