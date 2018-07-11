import { Component } from "@angular/core";
import { LoadingController, AlertController } from 'ionic-angular';
import { ImageService } from "../../services/image.service";

@Component({
  selector: 'page-booking-images',
  templateUrl: 'booking-images.component.html'
})
export class BookingImagesPage {

  constructor(private imageService: ImageService,
    public alertCtrl: AlertController,
    public loadingCtrl: LoadingController) {
  }

  ngOnInit() {
  }

  uploadImages(input) {
    let loading = this.loadingCtrl.create({
      content: 'Please wait...'
    });
    loading.present();

    let file: File = input.target.files[0];
    this.imageService.uploadImages(file).subscribe(
      data => {
        loading.dismiss();
      },
      messages => {
        loading.dismiss();
        if (messages instanceof Array) {
          messages.forEach(message => {
            if (message.key == "INVALID_FILE") {
              this.alertCtrl.create({
                message: "Invalid logos file",
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