import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';

import { IonicModule } from '@ionic/angular';

import { ContractBlacklistPage } from './contract-blacklist.page';

const routes: Routes = [
  {
    path: '',
    component: ContractBlacklistPage
  }
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RouterModule.forChild(routes)
  ],
  declarations: [ContractBlacklistPage]
})
export class ContractBlacklistPageModule {}
