import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';
import { IonicModule } from '@ionic/angular';
import { BankaccessEditPage } from './bankaccess-edit.page';
import { BankAccessResolverService } from 'src/app/services/resolver/bank-access-resolver.service';
import { ConsentResolverService } from 'src/app/services/resolver/consent-resolver.service';
import { KeycloakAuthGuard } from 'src/app/guard/keycloak-auth.guard';

const routes: Routes = [
  {
    path: '',
    component: BankaccessEditPage,
    resolve: {
      bankAccess: BankAccessResolverService,
      consent: ConsentResolverService,
    },
    canActivate: [KeycloakAuthGuard]
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
  declarations: [BankaccessEditPage]
})
export class BankaccessEditPageModule { }
