import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';

import { IonicModule } from '@ionic/angular';

import { UpdateAuthPage } from './update-auth.page';
import { KeycloakAuthGuard } from 'src/app/guard/keycloak-auth.guard';
import { ConsentAuthstatusResolverService } from 'src/app/services/resolver/consent-authstatus-resolver.service';
import { ConsentAuthGuard } from 'src/app/guard/consent-auth.guard';

const routes: Routes = [
  {
    path: 'consents/:consent-id/authorisations/:authorisation-id',
    component: UpdateAuthPage,
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
  declarations: [UpdateAuthPage]
})
export class UpdateAuthPageModule {}
