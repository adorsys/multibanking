import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';

import { IonicModule } from '@ionic/angular';

import { ScaMethodSelectionPage } from './sca-method-selection.page';
import { KeycloakAuthGuard } from 'src/app/guard/keycloak-auth.guard';
import { ConsentAuthstatusResolverService } from 'src/app/services/resolver/consent-authstatus-resolver.service';
import { ConsentAuthGuard } from 'src/app/guard/consent-auth.guard';

const routes: Routes = [
  {
    path: 'consents/:consent-id/authorisations/:authorisation-id',
    component: ScaMethodSelectionPage,
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
    IonicModule,
    RouterModule.forChild(routes)
  ],
  declarations: [ScaMethodSelectionPage]
})
export class ScaMethodSelectionPageModule {}
