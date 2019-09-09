import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';

import { IonicModule } from '@ionic/angular';

import { CreateConsentPage } from './create-consent.page';
import { KeycloakAuthGuard } from 'src/app/guard/keycloak-auth.guard';

const routes: Routes = [
  {
    path: '',
    component: CreateConsentPage,
    canActivate: [KeycloakAuthGuard],
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
  declarations: [CreateConsentPage]
})
export class CreateConsentPageModule {}
