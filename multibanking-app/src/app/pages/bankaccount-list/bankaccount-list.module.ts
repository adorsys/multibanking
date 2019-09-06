import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';

import { IonicModule } from '@ionic/angular';

import { BankaccountListPage } from './bankaccount-list.page';
import { KeycloakAuthGuard } from 'src/app/guard/keycloak-auth.guard';
import { BankAccountsResolverService } from 'src/app/services/resolver/bank-accounts-resolver.service';

const routes: Routes = [
  {
    path: '',
    component: BankaccountListPage,
    canActivate: [KeycloakAuthGuard],
    resolve: {
      bankAccounts: BankAccountsResolverService
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
  declarations: [BankaccountListPage]
})
export class BankaccountListPageModule {}
