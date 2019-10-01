import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';

import { IonicModule } from '@ionic/angular';

import { AdminPage } from './admin.page';
import { KeycloakAuthGuard } from 'src/app/guard/keycloak-auth.guard';

const routes: Routes = [
  {
    path: '',
    canActivate: [KeycloakAuthGuard],
    children: [
      {
        path: '',
        component: AdminPage,
      },
      {
        path: 'banks',
        loadChildren: () => import('../banks-config/banks-config.module').then(m => m.BanksConfigPageModule)
      },
      {
        path: 'smartanalytics',
        loadChildren: () => import('../smartanalytics-config/smartanalytics-config.module').then(m => m.SmartanalyticsConfigPageModule)
      }
    ]
  }
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RouterModule.forChild(routes)
  ],
  declarations: [AdminPage]
})
export class AdminPageModule {}
