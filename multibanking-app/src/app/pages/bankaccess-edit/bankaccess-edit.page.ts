import { Component, OnInit } from '@angular/core';
import { ResourceBankAccess } from 'src/multibanking-api/resourceBankAccess';
import { FormGroup, FormBuilder } from '@angular/forms';
import { BankAccessService } from 'src/app/services/rest/bankAccess.service';
import { NavController, AlertController } from '@ionic/angular';
import { ActivatedRoute } from '@angular/router';
import { ResourceConsentTO } from 'src/multibanking-api/resourceConsentTO';
import { ConsentAuthstatusResolverService } from 'src/app/services/resolver/consent-authstatus-resolver.service';

@Component({
  selector: 'app-bankaccess-edit',
  templateUrl: './bankaccess-edit.page.html',
  styleUrls: ['./bankaccess-edit.page.scss'],
})
export class BankaccessEditPage implements OnInit {

  consentId: string;
  authorisationId: string;
  bankAccess: ResourceBankAccess;
  bankAccessForm: FormGroup;

  constructor(private bankAccessService: BankAccessService,
              private formBuilder: FormBuilder,
              private activatedRoute: ActivatedRoute,
              private alertController: AlertController,
              private authstatusResolver: ConsentAuthstatusResolverService,
              private navCtrl: NavController) { }

  ngOnInit() {
    this.consentId = this.activatedRoute.snapshot.paramMap.get('consent-id').trim();
    this.authorisationId = this.activatedRoute.snapshot.paramMap.get('authorisation-id').trim();
    this.bankAccess = this.activatedRoute.snapshot.data.bankAccess;

    if (!this.bankAccess) {
      this.bankAccess = {
        consentId: this.consentId,
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
        (response) => {
          if (response.challenge) {

          } else {
            this.navCtrl.navigateRoot('/bankconnections');
          }
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
