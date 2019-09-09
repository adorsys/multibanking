import { Component, OnInit } from '@angular/core';
import { ResourceUpdateAuthResponseTO } from 'src/multibanking-api/resourceUpdateAuthResponseTO';
import { ConsentService } from 'src/app/services/rest/consent.service';
import { NavController, AlertController } from '@ionic/angular';
import { ActivatedRoute } from '@angular/router';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Link } from 'src/multibanking-api/link';

@Component({
  selector: 'app-authorisation',
  templateUrl: './authorisation.page.html',
  styleUrls: ['./authorisation.page.scss'],
})
export class AuthorisationPage implements OnInit {

  consentAuthStatus: ResourceUpdateAuthResponseTO;
  consentId: string;
  authorisationId: string;

  public authorisationFormGroup: FormGroup;

  constructor(private consentService: ConsentService,
              private activatedRoute: ActivatedRoute,
              private formBuilder: FormBuilder,
              private alertController: AlertController,
              private navCtrl: NavController) { }

  ngOnInit() {
    this.consentId = this.activatedRoute.snapshot.paramMap.get('consent-id');
    this.authorisationId = this.activatedRoute.snapshot.paramMap.get('authorisation-id');
    this.consentAuthStatus = this.activatedRoute.snapshot.data.consentAuthStatus;

    this.authorisationFormGroup = this.formBuilder.group({
      scaAuthenticationData: ['', Validators.required]
    });
  }

  public submit() {
    // tslint:disable-next-line:no-string-literal
    const updateAuthenticationLink: Link = this.consentAuthStatus._links['transactionAuthorisation'];
    this.consentService.updateAuthentication(updateAuthenticationLink.href, this.authorisationFormGroup.value)
      .subscribe(
        (response) => {
          if (response.scaStatus === 'FINALISED') {
            this.navCtrl.navigateRoot(`bankaccess-create/${encodeURIComponent(this.consentId)}`);
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
