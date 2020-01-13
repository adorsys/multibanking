import { Component, OnInit, ViewChild } from '@angular/core';
import { IonSearchbar, NavController, AlertController } from '@ionic/angular';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { BankTO } from 'src/multibanking-api/bankTO';
import { BankService } from 'src/app/services/rest/bank.service';
import { ConsentService } from 'src/app/services/rest/consent.service';
import { ResourceCreateConsentResponseTO } from 'src/multibanking-api/resourceCreateConsentResponseTO';
import * as moment from 'moment';
import { v4 as uuidv4 } from 'uuid';
import { ConsentTO } from 'src/multibanking-api/consentTO';
import { Link } from 'src/multibanking-api/link';
import { SettingsService } from '../../../services/settings/settings.service';
import { ActivatedRoute } from '@angular/router';
import { ResourceConsentTO } from 'src/multibanking-api/models';

@Component({
  selector: 'app-create-consent',
  templateUrl: './create-consent.page.html',
  styleUrls: ['./create-consent.page.scss'],
})
export class CreateConsentPage implements OnInit {

  consentId: string;
  createConsentForm: FormGroup;

  banks: BankTO[];
  isItemAvailable = false;
  @ViewChild(IonSearchbar, { static: false }) searchbar: IonSearchbar;

  constructor(private bankService: BankService,
              private consentService: ConsentService,
              private settingsService: SettingsService,
              private formBuilder: FormBuilder,
              private activatedRoute: ActivatedRoute,
              private alertController: AlertController,
              private navCtrl: NavController) { }

  ngOnInit() {
    this.consentId = this.activatedRoute.snapshot.params['consent-id']; // after oauth prestep

    this.createConsentForm = this.formBuilder.group({
      iban: ['', Validators.required],
      validUntil: [moment().add(90, 'days').toISOString(), Validators.required],
      frequencyPerDay: [3, Validators.required],
      recurringIndicator: [true, Validators.required]
    });

    if (this.consentId) {
      this.consentService.getConsent(this.consentId).subscribe((consent: ResourceConsentTO) => {
        this.createConsentForm.controls.iban.setValue(consent.psuAccountIban);
        this.createConsent(this.createConsentTO());
      });
    }
  }

  getItems(ev: any) {
    const val = ev.target.value;

    if (val && val.trim() !== '') {
      this.bankService.searchBanks(val).subscribe((banks) => {
        this.banks = banks;
        this.isItemAvailable = this.banks.length > 0;
      });
    } else {
      this.banks = [];
      this.isItemAvailable = false;
    }
  }

  public submit() {
    this.createConsent(this.createConsentTO());
  }

  createConsent(consent: ConsentTO) {
    this.consentService.createConsent(consent).subscribe(
      response => this.consentCreated(response),
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
      }
    );
  }

  createConsentTO(): ConsentTO {
    const redirectUUID = uuidv4();
    return {
      id: this.consentId,
      psuAccountIban: this.createConsentForm.value.iban,
      accounts: [{ iban: this.createConsentForm.value.iban }],
      balances: [{ iban: this.createConsentForm.value.iban }],
      transactions: [{ iban: this.createConsentForm.value.iban }],
      validUntil: this.createConsentForm.value.validUntil,
      frequencyPerDay: this.createConsentForm.value.frequencyPerDay,
      recurringIndicator: this.createConsentForm.value.recurringIndicator,
      redirectId: redirectUUID,
      tppRedirectUri: `${this.settingsService.settings.baseUrl}/bankaccess-create/redirect/${redirectUUID}`,
    };
  }

  consentCreated(response: ResourceCreateConsentResponseTO) {
    // tslint:disable-next-line:no-string-literal
    const redirectUrl: Link = response._links['redirectUrl'];
    // tslint:disable-next-line:no-string-literal
    const oauthRedirectUrl: Link = response._links['oauthRedirectUrl'];
    if (redirectUrl) {
      window.location.href = redirectUrl.href;
    } else if (oauthRedirectUrl) {
      window.location.href = oauthRedirectUrl.href;
    } else {
      // tslint:disable-next-line:max-line-length
      this.navCtrl.navigateForward(`/update-auth/consents/${encodeURIComponent(response.consentId)}/authorisations/${encodeURIComponent(response.authorisationId)}`);
    }
  }
}
