import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';

import { IonicModule } from '@ionic/angular';

import { BankaccountDetailPage } from './bankaccount-detail.page';

const routes: Routes = [
  {
    path: '',
    component: BankaccountDetailPage,
    children:
      [
        {
          path: 'bookings',
          children:
            [
              {
                path: '',
                loadChildren: '../booking-list/booking-list.module#BookingListPageModule'
              },
              {
                path: ':booking-id',
                loadChildren: '../booking-detail/booking-detail.module#BookingDetailPageModule'
              }
            ]
        },
        {
          path: 'analytics',
          children:
            [
              {
                path: '',
                loadChildren: '../analytics/analytics.module#AnalyticsPageModule'
              }
            ]
        },
        {
          path: 'contracts',
          children:
            [
              {
                path: '',
                loadChildren: '../contracts/contracts.module#ContractsPageModule'
              }
            ]
        },
        {
          path: '',
          redirectTo: 'bookings',
          pathMatch: 'full'
        }
      ]
  },
  {
    path: '',
    redirectTo: 'bookings',
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RouterModule.forChild(routes)
  ],
  declarations: [BankaccountDetailPage]
})
export class BankaccountDetailPageModule {}
