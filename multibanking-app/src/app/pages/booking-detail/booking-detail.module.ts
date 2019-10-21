import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { IonicModule } from '@ionic/angular';
import { KeycloakAuthGuard } from 'src/app/guard/keycloak-auth.guard';

import { BookingResolverService } from './../../services/resolver/booking-resolver.service';
import { BookingDetailPage } from './booking-detail.page';

const routes: Routes = [
  {
    path: '',
    component: BookingDetailPage,
    canActivate: [KeycloakAuthGuard],
    resolve: {
      booking: BookingResolverService
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
  declarations: [BookingDetailPage]
})
export class BookingDetailPageModule {}
