import { Component, OnInit } from '@angular/core';
import { ImagesService } from 'src/app/services/rest/images.service';
import { AlertController, LoadingController } from '@ionic/angular';

@Component({
  selector: 'app-images',
  templateUrl: './images.page.html',
  styleUrls: ['./images.page.scss'],
})
export class ImagesPage implements OnInit {

  constructor(private imageService: ImagesService,
              public alertCtrl: AlertController,
              public loadingCtrl: LoadingController) {
  }

  ngOnInit() {
  }

  async uploadImages(input) {
    const loading = this.loadingCtrl.create({
      message: 'Please wait...'
    });
    (await loading).present();

    const file: File = input.target.files[0];
    this.imageService.uploadImages(file).subscribe(
      async data => {
        (await loading).dismiss();
      },
      async messages => {
        (await loading).dismiss();
        if (messages instanceof Array) {
          messages.forEach(async message => {
            if (message.key === 'INVALID_FILE') {
              (await this.alertCtrl.create({
                message: 'Invalid logos file',
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
