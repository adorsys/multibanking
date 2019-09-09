import { Component, OnInit } from '@angular/core';
import { ResourceBankAccess } from 'src/multibanking-api/resourceBankAccess';
import { FormGroup, FormBuilder } from '@angular/forms';
import { BankAccessService } from 'src/app/services/rest/bankAccess.service';
import { NavController, AlertController } from '@ionic/angular';
import { ActivatedRoute } from '@angular/router';
import { ResourceConsentTO } from 'src/multibanking-api/resourceConsentTO';

@Component({
  selector: 'app-bankaccess-edit',
  templateUrl: './bankaccess-edit.page.html',
  styleUrls: ['./bankaccess-edit.page.scss'],
})
export class BankaccessEditPage implements OnInit {

  bankAccess: ResourceBankAccess;
  consent: ResourceConsentTO;
  bankAccessForm: FormGroup;

  constructor(private bankAccessService: BankAccessService,
              private formBuilder: FormBuilder,
              private activatedRoute: ActivatedRoute,
              private alertController: AlertController,
              private navCtrl: NavController) { }

  ngOnInit() {
    this.consent = this.activatedRoute.snapshot.data.consent;
    this.bankAccess = this.activatedRoute.snapshot.data.bankAccess;

    if (!this.bankAccess) {
      this.bankAccess = {
        consentId: this.consent.id,
        categorizeBookings: true,
        storeBookings: true,
        storeAnalytics: true,
      };
    }

    this.bankAccessForm = this.formBuilder.group({
      id: [this.bankAccess.id],
      consentId: [this.bankAccess.consentId],
      categorizeBookings: [this.bankAccess.categorizeBookings],
      storeBookings: [this.bankAccess.storeBookings],
      storeAnalytics: [this.bankAccess.storeAnalytics],
      storeAnonymizedBookings: [this.bankAccess.storeAnonymizedBookings],
      provideDataForMachineLearning: [this.bankAccess.provideDataForMachineLearning]
    });
  }

  saveBankAccess() {
    if (this.bankAccessForm.value.id) {
      this.bankAccessService.updateBankAcccess(this.bankAccessForm.value).subscribe(() => {
        this.navCtrl.navigateRoot('/bankconnections');
      });
    } else {
      this.bankAccessService.createBankAcccess(this.bankAccessForm.value).subscribe(
        () => {
          this.navCtrl.navigateRoot('/bankconnections');
        },
        messages => {
          if (messages instanceof Array) {
            messages.forEach(async message => {
              const alert = await this.alertController.create({
                message: message.renderedMessage,
                buttons: ['OK']
              });
              alert.present();
            });
          }
        });
    }
  }
}
