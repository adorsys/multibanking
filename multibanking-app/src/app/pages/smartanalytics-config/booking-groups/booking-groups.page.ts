import { Component, OnInit } from '@angular/core';
import { ResourceGroupConfig } from 'src/multibanking-api/resourceGroupConfig';
import { RulesService } from 'src/app/services/rest/rules.service';
import { AlertController, LoadingController } from '@ionic/angular';

@Component({
  selector: 'app-booking-groups',
  templateUrl: './booking-groups.page.html',
  styleUrls: ['./booking-groups.page.scss'],
})
export class BookingGroupsPage implements OnInit {

  bookingGroupConfig: ResourceGroupConfig;

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

  async uploadGroups(input) {
    const loading = this.loadingCtrl.create({
      message: 'Please wait...'
    });
    (await loading).present();

    const file: File = input.target.files[0];
    this.rulesService.uploadBookingGroups(file).subscribe(
      async data => {
        this.loadBookingGroups();
        (await loading).dismiss();
      },
      async messages => {
        (await loading).dismiss();
        if (messages instanceof Array) {
          messages.forEach(async message => {
            if (message.key === 'INVALID_FILE') {
              (await this.alertCtrl.create({
                message: 'Invalid booking groups file',
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
