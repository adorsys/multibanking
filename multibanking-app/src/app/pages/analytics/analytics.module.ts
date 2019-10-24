import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { IonicModule } from '@ionic/angular';
import { ChartsModule } from 'ng2-charts';
import { MomentModule } from 'ngx-moment';

import { KeycloakAuthGuard } from '../../guard/keycloak-auth.guard';
import { BankAccountsResolverService } from '../../services/resolver/bank-accounts-resolver.service';
import { AnalyticsPage } from './analytics.page';

const routes: Routes = [
  {
    path: '',
    component: AnalyticsPage,
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
    RouterModule.forChild(routes),
    ChartsModule,
    MomentModule
  ],
  declarations: [AnalyticsPage]
})
export class AnalyticsPageModule {}
