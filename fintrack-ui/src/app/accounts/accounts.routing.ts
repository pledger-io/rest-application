import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {OwnAccountsComponent} from "./own-accounts/own-accounts.component";
import {AccountOverviewComponent} from "./account-overview/account-overview.component";
import {EditAccountComponent} from "./edit-account/edit-account.component";
import {AccountsModule} from "./accounts.module";
import {AuthorizationService} from "../core/core-services";
import {BreadcrumbService} from "../service/breadcrumb.service";
import {TitleService} from "../service/title.service";
import {Breadcrumb} from "../core/core-models";
import {TransactionOverviewComponent} from "./transaction-overview/transaction-overview.component";
import {AccountResolverService} from "./account-resolver.service";
import {TransactionQuickActionComponent} from "./transaction-quick-action/transaction-quick-action.component";
import {DateRangeResolverService} from "../core/date-range-resolver.service";
import {TransactionResolverService} from "./transaction-resolver.service";
import {LiabilitiesOverviewComponent} from "./liabilities-overview/liabilities-overview.component";
import {EditLiabilityComponent} from "./edit-liability/edit-liability.component";
import {LiabilityTransactionOverviewComponent} from "./liability-transaction-overview/liability-transaction-overview.component";

const transactionChildRouters = component => [
  {
    path: '',
    component: component,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      title: 'page.title.transactions.overview',
      quickNavigation: TransactionQuickActionComponent,
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.settings'),
        new Breadcrumb(null, 'page.nav.accounts'),
        new Breadcrumb('$account.path/transactions', '$account.name'),
        new Breadcrumb(null, 'page.nav.transactions')
      ]
    }
  },
  {
    path: ':from/:until',
    component: component,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      title: 'page.title.transactions.overview',
      quickNavigation: TransactionQuickActionComponent,
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.settings'),
        new Breadcrumb(null, 'page.nav.accounts'),
        new Breadcrumb('$account.path/transactions', '$account.name'),
        new Breadcrumb(null, 'page.nav.transactions')
      ]
    }
  }
];

const routes: Routes = [
  {
    path: 'own',
    component: OwnAccountsComponent,
    canActivate: [AuthorizationService, BreadcrumbService, TitleService],
    data: {
      title: 'page.title.accounts.overview',
      breadcrumbs: [
        new Breadcrumb(null, 'page.nav.settings'),
        new Breadcrumb(null, 'page.nav.accounts'),
        new Breadcrumb(null, 'page.nav.accounts.accounts')
      ]
    }
  },
  {
    path: 'liability',
    children: [
      {
        path: '',
        component: LiabilitiesOverviewComponent,
        canActivate: [AuthorizationService, BreadcrumbService, TitleService],
        data: {
          title: 'page.title.accounts.liabilities.overview',
          breadcrumbs: [
            new Breadcrumb(null, 'page.nav.settings'),
            new Breadcrumb(null, 'page.nav.accounts'),
            new Breadcrumb(null, 'page.nav.accounts.liability')
          ]
        }
      },
      {
        path: 'add',
        component: EditLiabilityComponent,
        canActivate: [AuthorizationService, BreadcrumbService, TitleService],
        data: {
          title: 'page.title.accounts.liabilities.add',
          breadcrumbs: [
            new Breadcrumb(null, 'page.nav.settings'),
            new Breadcrumb(null, 'page.nav.accounts'),
            new Breadcrumb(null, 'page.nav.accounts.liability'),
            new Breadcrumb(null, 'page.title.accounts.liabilities.add')
          ]
        }
      },
      {
        path: ':accountId',
        runGuardsAndResolvers: 'always',
        resolve: {
          account: AccountResolverService
        },
        children: [
          {
            path: 'edit',
            component: EditLiabilityComponent,
            canActivate: [AuthorizationService, BreadcrumbService, TitleService],
            data: {
              title: 'page.title.accounts.liabilities.edit',
              breadcrumbs: [
                new Breadcrumb(null, 'page.nav.settings'),
                new Breadcrumb(null, 'page.nav.accounts'),
                new Breadcrumb(null, 'page.nav.accounts.liability'),
                new Breadcrumb(null, 'page.title.accounts.liabilities.edit')
              ]
            }
          },
          {
            path: 'transactions',
            runGuardsAndResolvers: 'always',
            children: [
              {
                path: '',
                component: LiabilityTransactionOverviewComponent,
                canActivate: [AuthorizationService, BreadcrumbService, TitleService],
                data: {
                  title: 'page.title.transactions.overview',
                  breadcrumbs: [
                    new Breadcrumb(null, 'page.nav.settings'),
                    new Breadcrumb(null, 'page.nav.accounts'),
                    new Breadcrumb('$account.path/transactions', '$account.name'),
                    new Breadcrumb(null, 'page.nav.transactions')
                  ]
                }
              }
            ]
          }
        ]
      }
    ]
  },
  {
    path: ':type',
    children: [
      {
        path: '',
        component: AccountOverviewComponent,
        canActivate: [AuthorizationService, BreadcrumbService, TitleService],
        data: {
          breadcrumbs: [
            new Breadcrumb(null, 'page.nav.settings'),
            new Breadcrumb(null, 'page.nav.accounts'),
            new Breadcrumb(null, 'page.nav.accounts.accounts')
          ]
        },
      },
      {
        path: 'add',
        component: EditAccountComponent,
        canActivate: [AuthorizationService, BreadcrumbService, TitleService],
        data: {
          title: 'page.title.accounts.add',
          breadcrumbs: [
            new Breadcrumb(null, 'page.nav.settings'),
            new Breadcrumb(null, 'page.nav.accounts'),
            new Breadcrumb(null, 'page.nav.accounts.accounts'),
            new Breadcrumb(null, 'page.title.accounts.add')
          ]
        }
      },
      {
        path: ':accountId',
        runGuardsAndResolvers: 'always',
        resolve: {
          account: AccountResolverService
        },
        children: [
          {
            path: 'edit',
            component: EditAccountComponent,
            canActivate: [AuthorizationService, BreadcrumbService, TitleService],
            data: {
              title: 'page.title.accounts.edit',
              breadcrumbs: [
                new Breadcrumb(null, 'page.nav.settings'),
                new Breadcrumb(null, 'page.nav.accounts'),
                new Breadcrumb(null, 'page.nav.accounts.accounts'),
                new Breadcrumb(null, 'page.title.accounts.edit')
              ]
            }
          },
          {
            path: 'transaction',
            runGuardsAndResolvers: 'always',
            resolve: {
              transaction: TransactionResolverService
            },
            loadChildren: () => import('../transaction/transaction.routing').then(m => m.TransactionRoutingModule)
          },
          {
            path: 'transactions',
            runGuardsAndResolvers: 'always',
            resolve: {
              dateRange: DateRangeResolverService
            },
            children: [
              {
                path: '',
                component: TransactionOverviewComponent,
                canActivate: [AuthorizationService, BreadcrumbService, TitleService],
                data: {
                  title: 'page.title.transactions.overview',
                  quickNavigation: TransactionQuickActionComponent,
                  breadcrumbs: [
                    new Breadcrumb(null, 'page.nav.settings'),
                    new Breadcrumb(null, 'page.nav.accounts'),
                    new Breadcrumb('$account.path/transactions', '$account.name'),
                    new Breadcrumb(null, 'page.nav.transactions')
                  ]
                }
              },
              {
                path: ':from/:until',
                component: TransactionOverviewComponent,
                canActivate: [AuthorizationService, BreadcrumbService, TitleService],
                data: {
                  title: 'page.title.transactions.overview',
                  quickNavigation: TransactionQuickActionComponent,
                  breadcrumbs: [
                    new Breadcrumb(null, 'page.nav.settings'),
                    new Breadcrumb(null, 'page.nav.accounts'),
                    new Breadcrumb('$account.path/transactions', '$account.name'),
                    new Breadcrumb(null, 'page.nav.transactions')
                  ]
                }
              }
            ]
          }
        ]
      }
    ]
  }
];

@NgModule({
  imports: [AccountsModule, RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccountRoutingModule { }
