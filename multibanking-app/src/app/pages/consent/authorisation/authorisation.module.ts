import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';

import { IonicModule } from '@ionic/angular';

import { AuthorisationPage } from './authorisation.page';
import { KeycloakAuthGuard } from 'src/app/guard/keycloak-auth.guard';
import { ConsentAuthstatusResolverService } from 'src/app/services/resolver/consent-authstatus-resolver.service';
import { ConsentAuthGuard } from 'src/app/guard/consent-auth.guard';

const routes: Routes = [
  {
    path: 'consents/:consent-id/authorisations/:authorisation-id',
    component: AuthorisationPage,
    canActivate: [KeycloakAuthGuard, ConsentAuthGuard],
    resolve: {
      consentAuthStatus: ConsentAuthstatusResolverService
    }
  }
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    IonicModule,
    RouterModule.forChild(routes)
  ],
  declarations: [AuthorisationPage]
})
export class AuthorisationPageModule {}
