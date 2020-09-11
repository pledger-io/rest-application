import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthenticateComponent} from './authenticate/authenticate.component';
import {DashboardComponent} from './dashboard/dashboard.component';
import {RegisterComponent} from './authenticate/register.component';
import {TitleService} from './service/title.service';
import {AuthorizationService} from './core/core-services';
import {VerifyTokenComponent} from './authenticate/verify-token.component';
import {BreadcrumbService} from './service/breadcrumb.service';

const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthorizationService, TitleService, BreadcrumbService],
    data: {
      title: 'page.title.dashboard'
    }
  },
  {
    path: 'accounts',
    loadChildren: () => import('./accounts/accounts.routing').then(m => m.AccountRoutingModule)
  },
  {
    path: 'budgets',
    loadChildren: () => import('./budget/budget.routing').then(m => m.BudgetRoutingModule)
  },
  {
    path: 'categories',
    loadChildren: () => import('./category/categories.routing').then(m => m.CategoryRoutingModule)
  },
  {
    path: 'contracts',
    loadChildren: () => import('./contract/contract.routing').then(m => m.ContractRoutingModule)
  },
  {
    path: 'import',
    loadChildren: () => import('./batch-import/batch-import.routing').then(m => m.BatchImportRoutingModule)
  },
  {
    path: 'user',
    loadChildren: () => import('./profile/profile.routing').then(m => m.ProfileRoutingModule)
  },
  {
    path: 'rules',
    loadChildren: () => import('./transaction-rule/transaction-rule.routing').then(m => m.TransactionRuleRoutingModule)
  },
  {
    path: 'reports',
    loadChildren: () => import('./reports/reports.routing').then(m => m.ReportsRoutingModule)
  },
  {
    path: 'schedule/transactions',
    loadChildren: () => import('./transaction-schedule/scheduled.routing').then(m => m.TransactionScheduleRoutingModule)
  },
  {
    path: 'settings',
    loadChildren: () => import('./settings/settings.routing').then(m => m.SettingRoutingModule)
  },
  {
    path: 'transactions',
    loadChildren: () => import('./transaction/transaction.routing').then(m => m.GlobalTransactionRoutingModule)
  },
  {
    path: 'login',
    component: AuthenticateComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'verify',
    component: VerifyTokenComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {paramsInheritanceStrategy: 'always'})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
