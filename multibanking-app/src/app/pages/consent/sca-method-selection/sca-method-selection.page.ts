import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AlertController, NavController } from '@ionic/angular';
import { ConsentService } from 'src/app/services/rest/consent.service';
import { Link } from 'src/multibanking-api/link';
import { ResourceUpdateAuthResponseTO } from 'src/multibanking-api/resourceUpdateAuthResponseTO';
import { TanTransportTypeTO } from 'src/multibanking-api/tanTransportTypeTO';

@Component({
  selector: 'app-sca-method-selection',
  templateUrl: './sca-method-selection.page.html',
  styleUrls: ['./sca-method-selection.page.scss'],
})
export class ScaMethodSelectionPage implements OnInit {

  scaMethod: TanTransportTypeTO;
  consentAuthStatus: ResourceUpdateAuthResponseTO;
  consentId: string;
  authorisationId: string;

  constructor(
    private consentService: ConsentService,
    private activatedRoute: ActivatedRoute,
    private alertController: AlertController,
    private navCtrl: NavController) { }

  ngOnInit() {
    this.consentId = this.activatedRoute.snapshot.paramMap.get('consent-id');
    this.authorisationId = this.activatedRoute.snapshot.paramMap.get('authorisation-id');
    this.consentAuthStatus = this.activatedRoute.snapshot.data.consentAuthStatus;
    this.scaMethod = this.consentAuthStatus.scaMethods[0];
  }

  public submit() {
    // tslint:disable-next-line:no-string-literal
    const selectAuthenticationMethodLink: Link = this.consentAuthStatus._links['selectAuthenticationMethod'];

    this.consentService.scaMethodSelection(
      selectAuthenticationMethodLink.href,
      {
        authenticationMethodId: this.scaMethod.id,
        ...(this.scaMethod.medium ? { tanMediaName: this.scaMethod.medium } : {})
      }
    )
      .subscribe(
        (response) => {
          // tslint:disable-next-line:no-string-literal
          if (response._links['transactionAuthorisation']) {
            // tslint:disable-next-line:max-line-length
            this.navCtrl.navigateRoot(`/authorisation/consents/${encodeURIComponent(this.consentId)}/authorisations/${encodeURIComponent(this.authorisationId)}`);
          } else {
            // tslint:disable-next-line:max-line-length
            this.navCtrl.navigateRoot(`bankaccess-create/consents/${encodeURIComponent(this.consentId)}/authorisations/${encodeURIComponent(this.authorisationId)}`);
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
