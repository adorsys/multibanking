import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  { path: '', redirectTo: 'bankconnections', pathMatch: 'full' },
  {
    path: 'bankconnections',
    loadChildren: () => import('./pages/bankaccess-list/bankaccess-list.module').then(m => m.BankaccessListPageModule)
  },
  { path: 'admin', loadChildren: './pages/admin/admin.module#AdminPageModule' },
  { path: 'update-auth', loadChildren: './pages/consent/update-auth/update-auth.module#UpdateAuthPageModule' },
  { path: 'sca-method-selection', loadChildren: './pages/consent/sca-method-selection/sca-method-selection.module#ScaMethodSelectionPageModule' },
  { path: 'authorisation', loadChildren: './pages/consent/authorisation/authorisation.module#AuthorisationPageModule' },
  { path: 'create-consent', loadChildren: './pages/consent/create-consent/create-consent.module#CreateConsentPageModule' },
  { path: 'bankaccess-edit', loadChildren: './pages//bankaccess-edit/bankaccess-edit.module#BankaccessEditPageModule' },
  { path: 'bankaccess-create/consents/:consent-id/authorisations/:authorisation-id', loadChildren: './pages//bankaccess-edit/bankaccess-edit.module#BankaccessEditPageModule' },
  { path: 'bankaccess-create/redirect/:redirect-id', loadChildren: './pages//bankaccess-edit/bankaccess-edit.module#BankaccessEditPageModule' },
  { path: 'bankconnections/:access-id', loadChildren: './pages/bankaccount-list/bankaccount-list.module#BankaccountListPageModule' },
  { path: 'bankconnections/:access-id/accounts/:account-id', loadChildren: './pages/booking-list/booking-list.module#BookingListPageModule' }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
