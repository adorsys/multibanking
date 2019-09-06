import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';

import { IonicModule } from '@ionic/angular';

import { BankaccessListPage } from './bankaccess-list.page';
import { KeycloakAuthGuard } from 'src/app/guard/keycloak-auth.guard';

const routes: Routes = [
  {
    path: '',
    component: BankaccessListPage,
    canActivate: [KeycloakAuthGuard],
  },
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RouterModule.forChild(routes)
  ],
  declarations: [BankaccessListPage]
})
export class BankaccessListPageModule { }
