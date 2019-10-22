import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { IonicModule } from '@ionic/angular';

import { KeycloakAuthGuard } from '../../guard/keycloak-auth.guard';
import { CyclePipe } from '../../pipes/cycle.pipe';
import { BankAccountsResolverService } from './../../services/resolver/bank-accounts-resolver.service';
import { ContractsPage } from './contracts.page';

const routes: Routes = [
  {
    path: '',
    component: ContractsPage,
    canActivate: [KeycloakAuthGuard],
    resolve: {
      bankAccount: BankAccountsResolverService
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
  declarations: [ContractsPage, CyclePipe]
})
export class ContractsPageModule {}
