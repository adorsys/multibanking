import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';

import { IonicModule } from '@ionic/angular';

import { SmartanalyticsConfigPage } from './smartanalytics-config.page';

const routes: Routes = [
  {
    path: '',
    component: SmartanalyticsConfigPage
  },
  {
    path: 'custom-rules',
    loadChildren: () => import('../smartanalytics-config/custom-rules/custom-rules.module').then(m => m.CustomRulesPageModule)
  },
  {
    path: 'static-rules',
    loadChildren: () => import('../smartanalytics-config/static-rules/static-rules.module').then(m => m.StaticRulesPageModule)
  },
  {
    path: 'booking-groups',
    loadChildren: () => import('../smartanalytics-config/booking-groups/booking-groups.module').then(m => m.BookingGroupsPageModule)
  },
  {
    path: 'categories',
    loadChildren: () => import('../smartanalytics-config/categories/categories.module').then(m => m.CategoriesPageModule)
  },
  {
    path: 'contract-blacklist',
    loadChildren: () => import('../smartanalytics-config/contract-blacklist/contract-blacklist.module').then(m => m.ContractBlacklistPageModule)
  },
  {
    path: 'images',
    loadChildren: () => import('../smartanalytics-config/images/images.module').then(m => m.ImagesPageModule)
  },
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RouterModule.forChild(routes)
  ],
  declarations: [SmartanalyticsConfigPage]
})
export class SmartanalyticsConfigPageModule { }
