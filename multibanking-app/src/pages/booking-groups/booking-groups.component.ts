import { Component } from "@angular/core";
import { RulesService } from "../../services/rules.service";
import { LoadingController, AlertController } from 'ionic-angular';
import { GroupConfig } from "../../model/multibanking/groupConfig";



@Component({
  selector: 'page-booking-groups ',
  templateUrl: 'booking-groups.component.html'
})
export class BookingGroupsPage {

  bookingGroupConfig: GroupConfig

  constructor(private rulesService: RulesService,
    public alertCtrl: AlertController,
    public loadingCtrl: LoadingController) {
  }

  ngOnInit() {
    this.loadBookingGroups();
  }

  loadBookingGroups() {
    this.rulesService.getBookingGroups().subscribe(
      result => {
        this.bookingGroupConfig = result;
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

  uploadGroups(input) {
    let loading = this.loadingCtrl.create({
      content: 'Please wait...'
    });
    loading.present();

    let file: File = input.target.files[0];
    this.rulesService.uploadBookingGroups(file).subscribe(
      data => {
        this.loadBookingGroups();
        loading.dismiss();
      },
      messages => {
        loading.dismiss();
        if (messages instanceof Array) {
          messages.forEach(message => {
            if (message.key == "INVALID_FILE") {
              this.alertCtrl.create({
                message: "Invalid booking groups file",
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
