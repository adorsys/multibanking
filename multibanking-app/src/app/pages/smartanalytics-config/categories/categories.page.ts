import { Component, OnInit } from '@angular/core';
import { CategoriesTree } from 'src/multibanking-api/categoriesTree';
import { RulesService } from 'src/app/services/rest/rules.service';
import { AlertController, LoadingController } from '@ionic/angular';

@Component({
  selector: 'app-categories',
  templateUrl: './categories.page.html',
  styleUrls: ['./categories.page.scss'],
})
export class CategoriesPage implements OnInit {

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
          messages.forEach(async message => {
            if (message.key !== 'RESOURCE_NOT_FOUND') {
              (await this.alertCtrl.create({
                message: message.renderedMessage,
                buttons: ['OK']
              })).present();
            }
          });
        }
      });
  }

  async uploadCategories(input) {
    const loading = this.loadingCtrl.create({
      message: 'Please wait...'
    });
    (await loading).present();

    const file: File = input.target.files[0];
    this.rulesService.uploadCategories(file).subscribe(
      async data => {
        this.loadCategories();
        (await loading).dismiss();
      },
      async messages => {
        (await loading).dismiss();
        if (messages instanceof Array) {
          messages.forEach(async message => {
            if (message.key === 'INVALID_FILE') {
              (await this.alertCtrl.create({
                message: 'Invalid categories file',
                buttons: ['OK']
              })).present();
            } else {
              (await this.alertCtrl.create({
                message: message.renderedMessage,
                buttons: ['OK']
              })).present();
            }
          });
        }
      });
    input.target.value = null;
  }
}
