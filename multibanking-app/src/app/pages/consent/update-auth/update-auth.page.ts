import { Component, OnInit, Input } from '@angular/core';
import { ConsentService } from 'src/app/services/rest/consent.service';
import { BankService } from 'src/app/services/rest/bank.service';
import { ResourceBankTO } from 'src/multibanking-api/resourceBankTO';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Link } from 'src/multibanking-api/link';
import { ResourceUpdateAuthResponseTO } from 'src/multibanking-api/resourceUpdateAuthResponseTO';
import { ActivatedRoute } from '@angular/router';
import { NavController, AlertController } from '@ionic/angular';

@Component({
  selector: 'app-update-auth',
  templateUrl: './update-auth.page.html',
  styleUrls: ['./update-auth.page.scss'],
})
export class UpdateAuthPage implements OnInit {

  bank: ResourceBankTO;
  consentAuthStatus: ResourceUpdateAuthResponseTO;
  consentId: string;
  authorisationId: string;
  psuMessage: string;

  public loginFormGroup: FormGroup;

  constructor(private bankService: BankService,
              private consentService: ConsentService,
              private formBuilder: FormBuilder,
              private activatedRoute: ActivatedRoute,
              private alertController: AlertController,
              private navCtrl: NavController) { }

  ngOnInit() {
    this.consentId = this.activatedRoute.snapshot.paramMap.get('consent-id');
    this.authorisationId = this.activatedRoute.snapshot.paramMap.get('authorisation-id');
    this.consentAuthStatus = this.activatedRoute.snapshot.data.consentAuthStatus;
    this.proceedConsentAuthorisation();
  }

  proceedConsentAuthorisation() {
    // tslint:disable-next-line:no-string-literal
    const bankLink: Link = this.consentAuthStatus._links['bank'];
    this.bankService.getBankByLink(bankLink.href).subscribe((bank) => {
      this.bank = bank;
      this.loginFormGroup = this.formBuilder.group(this.createLoginFormControl());
    });
  }

  public createLoginFormControl(): {} {
    const credentials = this.bank.loginSettings.credentials;
    const formControl: any = {};

    let credentialsCount = 0;
    for (const field of credentials) {
      let value = [];
      if (!field.optional) {
        value = ['', Validators.required];
      }

      field.fieldName = field.fieldName
        ? field.fieldName
        : field.masked
          ? 'password'
          : credentialsCount === 0
            ? 'psuId'
            : 'psuCorporateId';

      formControl[field.fieldName] = value;

      if (!field.masked) {
        credentialsCount++;
      }
    }
    return formControl;
  }

  public submit() {
    // tslint:disable-next-line:no-string-literal
    const updateAuthenticationLink: Link = this.consentAuthStatus._links['updateAuthentication'];
    this.consentService.updateAuthentication(updateAuthenticationLink.href, this.loginFormGroup.value)
      .subscribe(
        (response) => {
          if (response.scaApproach === 'DECOUPLED') {
            this.psuMessage = response.psuMessage;
          } else if (response.scaStatus === 'PSUAUTHENTICATED') {
            // tslint:disable-next-line:max-line-length
            this.navCtrl.navigateRoot(`/sca-method-selection/consents/${encodeURIComponent(this.consentId)}/authorisations/${encodeURIComponent(this.authorisationId)}`);
          } else {
            // tslint:disable-next-line:max-line-length
            this.navCtrl.navigateRoot(`/authorisation/consents/${encodeURIComponent(this.consentId)}/authorisations/${encodeURIComponent(this.authorisationId)}`);
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

  continue() {
    // tslint:disable-next-line:max-line-length
    this.navCtrl.navigateRoot(`bankaccess-create/consents/${encodeURIComponent(this.consentId)}/authorisations/${encodeURIComponent(this.authorisationId)}`);
  }


}
